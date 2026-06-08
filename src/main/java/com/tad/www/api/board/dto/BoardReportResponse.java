package com.tad.www.api.board.dto;

import java.time.LocalDateTime;

import com.tad.www.api.board.entity.BoardReport;
import com.tad.www.api.user.entity.User;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardReportResponse {

    private Long id;
    private Long reporterId;
    private String reporterNickname;
    private String reporterEmail;
    private String targetType;
    private Long targetId;
    private Long targetPostId;
    private String targetCategoryKey;
    private String targetTitle;
    private Long reportedUserId;
    private String reportedUserNickname;
    private String reportedUserEmail;
    private String reasonCode;
    private String reasonDetail;
    private String status;
    private Long handledById;
    private String handledByNickname;
    private String handlerMemo;
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;

    public static BoardReportResponse from(BoardReport report, ReportTargetSummary targetSummary) {
        User reporter = report.getReporter();
        User reportedUser = report.getReportedUser();
        User handledBy = report.getHandledBy();

        return BoardReportResponse.builder()
            .id(report.getId())
            .reporterId(reporter == null ? null : reporter.getId())
            .reporterNickname(reporter == null ? null : reporter.getNickname())
            .reporterEmail(reporter == null ? null : reporter.getEmail())
            .targetType(report.getTargetType())
            .targetId(report.getTargetId())
            .targetPostId(targetSummary == null ? null : targetSummary.postId())
            .targetCategoryKey(targetSummary == null ? null : targetSummary.categoryKey())
            .targetTitle(targetSummary == null ? "삭제되었거나 확인할 수 없는 대상" : targetSummary.title())
            .reportedUserId(reportedUser == null ? null : reportedUser.getId())
            .reportedUserNickname(reportedUser == null ? null : reportedUser.getNickname())
            .reportedUserEmail(reportedUser == null ? null : reportedUser.getEmail())
            .reasonCode(report.getReasonCode())
            .reasonDetail(report.getReasonDetail())
            .status(report.getStatus())
            .handledById(handledBy == null ? null : handledBy.getId())
            .handledByNickname(handledBy == null ? null : handledBy.getNickname())
            .handlerMemo(report.getHandlerMemo())
            .createdAt(report.getCreatedAt())
            .handledAt(report.getHandledAt())
            .build();
    }

    public record ReportTargetSummary(Long postId, String categoryKey, String title) {
    }
}
