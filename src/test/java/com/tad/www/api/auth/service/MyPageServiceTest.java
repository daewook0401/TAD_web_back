package com.tad.www.api.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Pageable;

import com.tad.www.api.analysis.entity.AnalysisGame;
import com.tad.www.api.analysis.repository.AnalysisGameRepository;
import com.tad.www.api.auth.dto.response.MyPageSummaryResponse;
import com.tad.www.api.auth.entity.LoginHistory;
import com.tad.www.api.auth.repository.LoginHistoryRepository;
import com.tad.www.api.board.entity.BoardCategory;
import com.tad.www.api.board.entity.BoardComment;
import com.tad.www.api.board.entity.BoardPost;
import com.tad.www.api.board.repository.BoardCommentRepository;
import com.tad.www.api.board.repository.BoardPostRepository;
import com.tad.www.api.user.entity.User;
import com.tad.www.api.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BoardPostRepository boardPostRepository;

    @Mock
    private BoardCommentRepository boardCommentRepository;

    @Mock
    private AnalysisGameRepository analysisGameRepository;

    @Mock
    private LoginHistoryRepository loginHistoryRepository;

    @InjectMocks
    private MyPageService myPageService;

    @Test
    void getSummaryReturnsActivitySnapshot() {
        User user = user();
        BoardPost post = post(user);
        BoardComment comment = comment(user, post);
        AnalysisGame game = analysisGame(user);
        LoginHistory login = loginHistory(user.getId());
        LoginHistory failedLogin = failedLoginHistory(user.getId());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(boardPostRepository.countVisibleByAuthorId(1L)).thenReturn(3L);
        when(boardCommentRepository.countVisibleByAuthorId(1L)).thenReturn(4L);
        when(analysisGameRepository.countByUploaderId(1L)).thenReturn(2L);
        when(loginHistoryRepository.countByUserIdAndLoginResult(1L, "SUCCESS")).thenReturn(5L);
        when(loginHistoryRepository.countByUserIdAndLoginResult(1L, "FAILURE")).thenReturn(1L);
        when(boardPostRepository.findRecentVisibleByAuthorId(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(Pageable.class)))
            .thenReturn(List.of(post));
        when(boardCommentRepository.findRecentVisibleByAuthorId(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(Pageable.class)))
            .thenReturn(List.of(comment));
        when(analysisGameRepository.findTop5ByUploaderIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(game));
        when(loginHistoryRepository.findByUserIdOrderByCreatedAtDesc(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(Pageable.class)))
            .thenReturn(List.of(login));
        when(loginHistoryRepository.findByUserIdAndLoginResultOrderByCreatedAtDesc(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq("SUCCESS"), org.mockito.ArgumentMatchers.any(Pageable.class)))
            .thenReturn(List.of(login));
        when(loginHistoryRepository.findByUserIdAndLoginResultOrderByCreatedAtDesc(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq("FAILURE"), org.mockito.ArgumentMatchers.any(Pageable.class)))
            .thenReturn(List.of(failedLogin));

        MyPageSummaryResponse response = myPageService.getSummary(user);

        assertThat(response.getStats().getPostCount()).isEqualTo(3L);
        assertThat(response.getStats().getCommentCount()).isEqualTo(4L);
        assertThat(response.getStats().getAnalysisRecordCount()).isEqualTo(2L);
        assertThat(response.getStats().getLoginCount()).isEqualTo(5L);
        assertThat(response.getSecurity().getFailedLoginCount()).isEqualTo(1L);
        assertThat(response.getSecurity().getLastSuccessfulLogin().getLoginResult()).isEqualTo("SUCCESS");
        assertThat(response.getSecurity().getLastFailedLogin().getLoginResult()).isEqualTo("FAILURE");
        assertThat(response.getRecentPosts()).singleElement().extracting(MyPageSummaryResponse.RecentPost::getTitle).isEqualTo("post");
        assertThat(response.getRecentComments()).singleElement().extracting(MyPageSummaryResponse.RecentComment::getPostTitle).isEqualTo("post");
        assertThat(response.getRecentAnalysisRecords()).singleElement().extracting(MyPageSummaryResponse.RecentAnalysisRecord::getReviewRequired).isEqualTo(true);
        assertThat(response.getRecentLogins()).singleElement().extracting(MyPageSummaryResponse.RecentLogin::getDevice).isEqualTo("Chrome / Windows");
    }

    @Test
    void getSummaryIgnoresMissingAnalysisStorage() {
        User user = user();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(boardPostRepository.countVisibleByAuthorId(1L)).thenReturn(0L);
        when(boardCommentRepository.countVisibleByAuthorId(1L)).thenReturn(0L);
        when(analysisGameRepository.countByUploaderId(1L)).thenThrow(new DataAccessResourceFailureException("missing analysis"));
        when(loginHistoryRepository.countByUserIdAndLoginResult(1L, "SUCCESS")).thenReturn(0L);
        when(loginHistoryRepository.countByUserIdAndLoginResult(1L, "FAILURE")).thenReturn(0L);
        when(boardPostRepository.findRecentVisibleByAuthorId(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(Pageable.class)))
            .thenReturn(List.of());
        when(boardCommentRepository.findRecentVisibleByAuthorId(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(Pageable.class)))
            .thenReturn(List.of());
        when(analysisGameRepository.findTop5ByUploaderIdOrderByCreatedAtDesc(1L)).thenThrow(new DataAccessResourceFailureException("missing analysis"));
        when(loginHistoryRepository.findByUserIdOrderByCreatedAtDesc(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(Pageable.class)))
            .thenReturn(List.of());
        when(loginHistoryRepository.findByUserIdAndLoginResultOrderByCreatedAtDesc(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq("SUCCESS"), org.mockito.ArgumentMatchers.any(Pageable.class)))
            .thenReturn(List.of());
        when(loginHistoryRepository.findByUserIdAndLoginResultOrderByCreatedAtDesc(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq("FAILURE"), org.mockito.ArgumentMatchers.any(Pageable.class)))
            .thenReturn(List.of());

        MyPageSummaryResponse response = myPageService.getSummary(user);

        assertThat(response.getStats().getAnalysisRecordCount()).isZero();
        assertThat(response.getRecentAnalysisRecords()).isEmpty();
    }

    private User user() {
        return User.builder()
            .id(1L)
            .email("user@example.com")
            .nickname("user")
            .emailVerified(true)
            .status("ACTIVE")
            .build();
    }

    private BoardPost post(User user) {
        return BoardPost.builder()
            .id(10L)
            .category(BoardCategory.builder().id(1L).categoryKey("free").name("자유").build())
            .author(user)
            .title("post")
            .content("content")
            .postType("free")
            .viewCount(7)
            .likeCount(2)
            .replyCount(1)
            .isNotice(false)
            .isDeleted(false)
            .createdAt(LocalDateTime.of(2026, 5, 1, 12, 0))
            .build();
    }

    private BoardComment comment(User user, BoardPost post) {
        return BoardComment.builder()
            .id(20L)
            .post(post)
            .author(user)
            .content("comment")
            .isDeleted(false)
            .createdAt(LocalDateTime.of(2026, 5, 1, 12, 30))
            .build();
    }

    private AnalysisGame analysisGame(User user) {
        return AnalysisGame.builder()
            .id(30L)
            .uploader(user)
            .bucket("tad")
            .objectKey("analysis/test.png")
            .screenshotUrl("https://example.com/test.png")
            .status("DRAFT")
            .createdAt(LocalDateTime.of(2026, 5, 1, 13, 0))
            .build();
    }

    private LoginHistory loginHistory(Long userId) {
        return LoginHistory.builder()
            .id(40L)
            .userId(userId)
            .ipAddress("127.0.0.1")
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/123.0.0.0 Safari/537.36")
            .loginType("NORMAL")
            .loginResult("SUCCESS")
            .createdAt(OffsetDateTime.parse("2026-05-01T14:00:00+09:00"))
            .build();
    }

    private LoginHistory failedLoginHistory(Long userId) {
        return LoginHistory.builder()
            .id(41L)
            .userId(userId)
            .ipAddress("127.0.0.1")
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/123.0.0.0 Safari/537.36")
            .loginType("NORMAL")
            .loginResult("FAILURE")
            .createdAt(OffsetDateTime.parse("2026-05-01T13:50:00+09:00"))
            .build();
    }
}
