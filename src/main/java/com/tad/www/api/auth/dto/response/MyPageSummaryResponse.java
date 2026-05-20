package com.tad.www.api.auth.dto.response;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import com.tad.www.api.analysis.entity.AnalysisGame;
import com.tad.www.api.auth.entity.LoginHistory;
import com.tad.www.api.board.entity.BoardComment;
import com.tad.www.api.board.entity.BoardPost;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageSummaryResponse {

    private ActivityStats stats;
    private SecuritySummary security;
    private List<RecentPost> recentPosts;
    private List<RecentComment> recentComments;
    private List<RecentAnalysisRecord> recentAnalysisRecords;
    private List<RecentLogin> recentLogins;

    @Getter
    @Builder
    public static class ActivityStats {
        private long postCount;
        private long commentCount;
        private long analysisRecordCount;
        private long loginCount;
    }

    @Getter
    @Builder
    public static class SecuritySummary {
        private long failedLoginCount;
        private RecentLogin lastSuccessfulLogin;
        private RecentLogin lastFailedLogin;
    }

    @Getter
    @Builder
    public static class RecentPost {
        private Long id;
        private String categoryKey;
        private String categoryName;
        private String title;
        private Integer viewCount;
        private Integer likeCount;
        private Integer replyCount;
        private LocalDateTime createdAt;

        public static RecentPost from(BoardPost post) {
            return RecentPost.builder()
                .id(post.getId())
                .categoryKey(post.getCategory().getCategoryKey())
                .categoryName(post.getCategory().getName())
                .title(post.getTitle())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .replyCount(post.getReplyCount())
                .createdAt(post.getCreatedAt())
                .build();
        }
    }

    @Getter
    @Builder
    public static class RecentComment {
        private Long id;
        private Long postId;
        private String postTitle;
        private String categoryKey;
        private String categoryName;
        private String content;
        private LocalDateTime createdAt;

        public static RecentComment from(BoardComment comment) {
            BoardPost post = comment.getPost();
            return RecentComment.builder()
                .id(comment.getId())
                .postId(post.getId())
                .postTitle(post.getTitle())
                .categoryKey(post.getCategory().getCategoryKey())
                .categoryName(post.getCategory().getName())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
        }
    }

    @Getter
    @Builder
    public static class RecentAnalysisRecord {
        private Long gameNumber;
        private String status;
        private String winner;
        private Boolean reviewRequired;
        private LocalDateTime createdAt;
        private LocalDateTime confirmedAt;

        public static RecentAnalysisRecord from(AnalysisGame game) {
            return RecentAnalysisRecord.builder()
                .gameNumber(game.getId())
                .status(game.getStatus())
                .winner(game.getWinner())
                .reviewRequired("DRAFT".equalsIgnoreCase(game.getStatus()))
                .createdAt(game.getCreatedAt())
                .confirmedAt(game.getConfirmedAt())
                .build();
        }
    }

    @Getter
    @Builder
    public static class RecentLogin {
        private Long id;
        private String loginType;
        private String loginResult;
        private String ipAddress;
        private String device;
        private OffsetDateTime createdAt;

        public static RecentLogin from(LoginHistory history, String device) {
            return RecentLogin.builder()
                .id(history.getId())
                .loginType(history.getLoginType())
                .loginResult(history.getLoginResult())
                .ipAddress(history.getIpAddress())
                .device(device)
                .createdAt(history.getCreatedAt())
                .build();
        }
    }
}
