package com.tad.www.api.board.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tad.www.api.board.dto.BoardReportCreateRequest;
import com.tad.www.api.board.dto.BoardReportHandleRequest;
import com.tad.www.api.board.dto.BoardReportResponse;
import com.tad.www.api.board.dto.UserSanctionResponse;
import com.tad.www.api.board.entity.BoardComment;
import com.tad.www.api.board.entity.BoardPost;
import com.tad.www.api.board.entity.BoardReport;
import com.tad.www.api.board.repository.BoardCommentRepository;
import com.tad.www.api.board.repository.BoardPostRepository;
import com.tad.www.api.board.repository.BoardReportRepository;
import com.tad.www.api.user.entity.User;
import com.tad.www.core.util.TextUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardReportService {

    private static final String TARGET_POST = "POST";
    private static final String TARGET_COMMENT = "COMMENT";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_ACCEPTED = "ACCEPTED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final int DEFAULT_ADMIN_REPORT_LIMIT = 100;
    private static final int MAX_ADMIN_REPORT_LIMIT = 300;

    private final BoardReportRepository boardReportRepository;
    private final BoardPostRepository boardPostRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final UserSanctionService userSanctionService;

    @Transactional
    public BoardReportResponse createReport(User currentUser, BoardReportCreateRequest request) {
        String targetType = normalizeTargetType(request.getTargetType());
        Long targetId = request.getTargetId();
        if (targetId == null) {
            throw new IllegalArgumentException("신고 대상 ID는 필수입니다.");
        }

        ReportTarget target = resolveReportTarget(targetType, targetId);
        if (target.author().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("본인이 작성한 대상은 신고할 수 없습니다.");
        }

        if (boardReportRepository.existsByReporter_IdAndTargetTypeAndTargetId(
            currentUser.getId(),
            targetType,
            targetId
        )) {
            throw new IllegalArgumentException("이미 신고한 대상입니다.");
        }

        String reasonCode = TextUtils.normalizeNullable(request.getReasonCode());
        if (reasonCode == null) {
            throw new IllegalArgumentException("신고 사유는 필수입니다.");
        }

        BoardReport saved = boardReportRepository.save(BoardReport.builder()
            .reporter(currentUser)
            .targetType(targetType)
            .targetId(targetId)
            .reportedUser(target.author())
            .reasonCode(reasonCode)
            .reasonDetail(TextUtils.normalizeNullable(request.getReasonDetail()))
            .status(STATUS_PENDING)
            .build());

        return BoardReportResponse.from(saved, target.summary());
    }

    @Transactional(readOnly = true)
    public List<BoardReportResponse> getAdminReports(String status, Integer limit) {
        String normalizedStatus = normalizeStatusOrNull(status);
        int normalizedLimit = normalizeLimit(limit);

        List<BoardReport> reports = normalizedStatus == null
            ? boardReportRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, normalizedLimit))
            : boardReportRepository.findByStatusOrderByCreatedAtDesc(normalizedStatus, PageRequest.of(0, normalizedLimit));

        return reports.stream()
            .map(report -> BoardReportResponse.from(report, findTargetSummary(report.getTargetType(), report.getTargetId())))
            .toList();
    }

    @Transactional
    public BoardReportResponse handleReport(Long reportId, User currentUser, BoardReportHandleRequest request) {
        BoardReport report = boardReportRepository.findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("신고 정보를 찾을 수 없습니다."));

        String status = normalizeHandleStatus(request.getStatus());
        report.setStatus(status);
        report.setHandledBy(currentUser);
        report.setHandledAt(LocalDateTime.now());
        report.setHandlerMemo(TextUtils.normalizeNullable(request.getHandlerMemo()));

        if (STATUS_ACCEPTED.equals(status) && TextUtils.normalizeNullable(request.getSanctionType()) != null) {
            String sanctionReason = TextUtils.normalizeNullable(request.getSanctionReason());
            if (sanctionReason == null) {
                sanctionReason = TextUtils.normalizeNullable(request.getHandlerMemo());
            }
            if (sanctionReason == null) {
                sanctionReason = "게시판 신고 처리";
            }

            UserSanctionResponse sanction = userSanctionService.createSanction(
                report.getReportedUser().getId(),
                request.getSanctionType(),
                request.getSanctionDays(),
                sanctionReason,
                currentUser
            );
            report.setHandlerMemo(appendSanctionMemo(report.getHandlerMemo(), sanction));
        }

        return BoardReportResponse.from(report, findTargetSummary(report.getTargetType(), report.getTargetId()));
    }

    private ReportTarget resolveReportTarget(String targetType, Long targetId) {
        if (TARGET_POST.equals(targetType)) {
            BoardPost post = boardPostRepository.findByIdAndIsDeletedFalse(targetId)
                .orElseThrow(() -> new IllegalArgumentException("신고할 게시글을 찾을 수 없습니다."));
            return new ReportTarget(
                post.getAuthor(),
                new BoardReportResponse.ReportTargetSummary(
                    post.getId(),
                    post.getCategory().getCategoryKey(),
                    post.getTitle()
                )
            );
        }

        BoardComment comment = boardCommentRepository.findByIdAndIsDeletedFalse(targetId)
            .orElseThrow(() -> new IllegalArgumentException("신고할 댓글을 찾을 수 없습니다."));
        if (Boolean.TRUE.equals(comment.getPost().getIsDeleted())) {
            throw new IllegalArgumentException("신고할 댓글을 찾을 수 없습니다.");
        }

        return new ReportTarget(
            comment.getAuthor(),
            new BoardReportResponse.ReportTargetSummary(
                comment.getPost().getId(),
                comment.getPost().getCategory().getCategoryKey(),
                comment.getPost().getTitle()
            )
        );
    }

    private BoardReportResponse.ReportTargetSummary findTargetSummary(String targetType, Long targetId) {
        if (TARGET_POST.equals(targetType)) {
            return boardPostRepository.findById(targetId)
                .map(post -> new BoardReportResponse.ReportTargetSummary(
                    post.getId(),
                    post.getCategory().getCategoryKey(),
                    post.getTitle()
                ))
                .orElse(null);
        }

        return boardCommentRepository.findById(targetId)
            .map(comment -> new BoardReportResponse.ReportTargetSummary(
                comment.getPost().getId(),
                comment.getPost().getCategory().getCategoryKey(),
                comment.getPost().getTitle()
            ))
            .orElse(null);
    }

    private String normalizeTargetType(String targetType) {
        String normalized = TextUtils.normalizeNullable(targetType);
        normalized = normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
        if (!TARGET_POST.equals(normalized) && !TARGET_COMMENT.equals(normalized)) {
            throw new IllegalArgumentException("지원하지 않는 신고 대상 유형입니다.");
        }
        return normalized;
    }

    private String normalizeStatusOrNull(String status) {
        String normalized = TextUtils.normalizeNullable(status);
        if (normalized == null || "ALL".equalsIgnoreCase(normalized)) {
            return null;
        }
        normalized = normalized.toUpperCase(Locale.ROOT);
        if (!STATUS_PENDING.equals(normalized) && !STATUS_ACCEPTED.equals(normalized) && !STATUS_REJECTED.equals(normalized)) {
            return null;
        }
        return normalized;
    }

    private String normalizeHandleStatus(String status) {
        String normalized = normalizeStatusOrNull(status);
        if (!STATUS_ACCEPTED.equals(normalized) && !STATUS_REJECTED.equals(normalized)) {
            throw new IllegalArgumentException("신고 처리 상태는 ACCEPTED 또는 REJECTED여야 합니다.");
        }
        return normalized;
    }

    private String appendSanctionMemo(String handlerMemo, UserSanctionResponse sanction) {
        String sanctionMemo = "제재 발급 #" + sanction.getId() + " (" + sanction.getSanctionType() + ")";
        return handlerMemo == null ? sanctionMemo : handlerMemo + "\n" + sanctionMemo;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_ADMIN_REPORT_LIMIT;
        }
        return Math.min(limit, MAX_ADMIN_REPORT_LIMIT);
    }

    private record ReportTarget(User author, BoardReportResponse.ReportTargetSummary summary) {
    }
}
