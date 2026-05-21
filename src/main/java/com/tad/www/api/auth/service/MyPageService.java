package com.tad.www.api.auth.service;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.tad.www.api.analysis.repository.AnalysisGameRepository;
import com.tad.www.api.auth.dto.response.MyPageSummaryResponse;
import com.tad.www.api.auth.entity.LoginHistory;
import com.tad.www.api.auth.repository.LoginHistoryRepository;
import com.tad.www.api.board.repository.BoardCommentRepository;
import com.tad.www.api.board.repository.BoardPostRepository;
import com.tad.www.api.user.entity.User;
import com.tad.www.api.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private static final int RECENT_LIMIT = 5;

    private final UserRepository userRepository;
    private final BoardPostRepository boardPostRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final AnalysisGameRepository analysisGameRepository;
    private final LoginHistoryRepository loginHistoryRepository;

    public MyPageSummaryResponse getSummary(User currentUser) {
        User user = userRepository.findById(currentUser.getId())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Long userId = user.getId();
        Pageable recent = PageRequest.of(0, RECENT_LIMIT);
        Pageable latest = PageRequest.of(0, 1);

        long postCount = boardPostRepository.countVisibleByAuthorId(userId);
        long commentCount = boardCommentRepository.countVisibleByAuthorId(userId);
        long analysisCount = safeValue(() -> analysisGameRepository.countByUploaderId(userId), 0L);
        long successfulLoginCount = loginHistoryRepository.countByUserIdAndLoginResult(userId, "SUCCESS");
        long failedLoginCount = loginHistoryRepository.countByUserIdAndLoginResult(userId, "FAILURE");

        return MyPageSummaryResponse.builder()
            .stats(MyPageSummaryResponse.ActivityStats.builder()
                .postCount(postCount)
                .commentCount(commentCount)
                .analysisRecordCount(analysisCount)
                .loginCount(successfulLoginCount)
                .build())
            .security(MyPageSummaryResponse.SecuritySummary.builder()
                .failedLoginCount(failedLoginCount)
                .lastSuccessfulLogin(findLatestLogin(userId, "SUCCESS", latest))
                .lastFailedLogin(findLatestLogin(userId, "FAILURE", latest))
                .build())
            .recentPosts(boardPostRepository.findRecentVisibleByAuthorId(userId, recent).stream()
                .map(MyPageSummaryResponse.RecentPost::from)
                .toList())
            .recentComments(boardCommentRepository.findRecentVisibleByAuthorId(userId, recent).stream()
                .map(MyPageSummaryResponse.RecentComment::from)
                .toList())
            .recentAnalysisRecords(safeList(() -> analysisGameRepository.findTop5ByUploaderIdOrderByCreatedAtDesc(userId)).stream()
                .map(MyPageSummaryResponse.RecentAnalysisRecord::from)
                .toList())
            .recentLogins(loginHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, recent).stream()
                .map(history -> MyPageSummaryResponse.RecentLogin.from(history, summarizeDevice(history)))
                .toList())
            .build();
    }

    private MyPageSummaryResponse.RecentLogin findLatestLogin(Long userId, String loginResult, Pageable pageable) {
        return loginHistoryRepository.findByUserIdAndLoginResultOrderByCreatedAtDesc(userId, loginResult, pageable)
            .stream()
            .findFirst()
            .map(history -> MyPageSummaryResponse.RecentLogin.from(history, summarizeDevice(history)))
            .orElse(null);
    }

    private String summarizeDevice(LoginHistory history) {
        String userAgent = history.getUserAgent();
        if (userAgent == null || userAgent.isBlank()) {
            return "알 수 없음";
        }

        String browser = "브라우저";
        if (userAgent.contains("Edg/")) {
            browser = "Edge";
        } else if (userAgent.contains("Chrome/")) {
            browser = "Chrome";
        } else if (userAgent.contains("Firefox/")) {
            browser = "Firefox";
        } else if (userAgent.contains("Safari/")) {
            browser = "Safari";
        }

        String os = "기기";
        if (userAgent.contains("Windows")) {
            os = "Windows";
        } else if (userAgent.contains("Android")) {
            os = "Android";
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            os = "iOS";
        } else if (userAgent.contains("Mac OS")) {
            os = "macOS";
        }

        return browser + " / " + os;
    }

    private <T> T safeValue(Supplier<T> supplier, T fallback) {
        try {
            return supplier.get();
        } catch (DataAccessException e) {
            return fallback;
        }
    }

    private <T> List<T> safeList(Supplier<List<T>> supplier) {
        return safeValue(supplier, List.of());
    }
}
