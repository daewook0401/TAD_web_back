CREATE TABLE board.tb_post (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    tag VARCHAR(50),
    view_count INT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    reply_count INT NOT NULL DEFAULT 0,
    is_notice BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_post_category
        FOREIGN KEY (category_id) REFERENCES board.tb_post_categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_post_author
        FOREIGN KEY (author_id) REFERENCES auth.tb_user(id) ON DELETE RESTRICT
);


CREATE TABLE board.tb_comment (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    parent_id BIGINT,
    content TEXT NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comment_post
        FOREIGN KEY (post_id) REFERENCES board.tb_post(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_author
        FOREIGN KEY (author_id) REFERENCES auth.tb_user(id) ON DELETE RESTRICT,
    CONSTRAINT fk_comment_parent
        FOREIGN KEY (parent_id) REFERENCES board.tb_comment(id) ON DELETE CASCADE
);

CREATE TABLE board.tb_post_like (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_post_like_post
        FOREIGN KEY (post_id) REFERENCES board.tb_post(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_like_user
        FOREIGN KEY (user_id) REFERENCES auth.tb_user(id) ON DELETE CASCADE,
    CONSTRAINT uq_post_like UNIQUE (post_id, user_id)
);

CREATE TABLE board.tb_post_attachment (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    file_url TEXT NOT NULL,
    file_name VARCHAR(255),
    stored_name VARCHAR(255),
    content_type VARCHAR(100),
    file_size BIGINT,
    file_kind VARCHAR(20) NOT NULL DEFAULT 'file',
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_post_attachment_post
        FOREIGN KEY (post_id) REFERENCES board.tb_post(id) ON DELETE CASCADE
);

CREATE TABLE board.tb_comment_attachment (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    file_url TEXT NOT NULL,
    file_name VARCHAR(255),
    stored_name VARCHAR(255),
    content_type VARCHAR(100),
    file_size BIGINT,
    file_kind VARCHAR(20) NOT NULL DEFAULT 'file',
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comment_attachment_comment
        FOREIGN KEY (comment_id) REFERENCES board.tb_comment(id) ON DELETE CASCADE
);

-- 게시글 목록 조회
CREATE INDEX idx_post_category_created_at
ON board.tb_post (category_id, created_at DESC);

-- 작성자 조회
CREATE INDEX idx_post_author
ON board.tb_post (author_id);

-- 댓글 조회
CREATE INDEX idx_comment_post
ON board.tb_comment (post_id);

-- 대댓글 조회
CREATE INDEX idx_comment_parent
ON board.tb_comment (parent_id);

-- 게시글 첨부 조회
CREATE INDEX idx_post_attachment_post
ON board.tb_post_attachment (post_id, sort_order, id);

-- 댓글 첨부 조회
CREATE INDEX idx_comment_attachment_comment
ON board.tb_comment_attachment (comment_id, sort_order, id);


CREATE OR REPLACE FUNCTION board.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_post_updated_at
BEFORE UPDATE ON board.tb_post
FOR EACH ROW
EXECUTE FUNCTION board.set_updated_at();


CREATE TRIGGER trg_comment_updated_at
BEFORE UPDATE ON board.tb_comment
FOR EACH ROW
EXECUTE FUNCTION board.set_updated_at();

-- 기존 DB 마이그레이션
ALTER TABLE board.tb_post
ADD COLUMN IF NOT EXISTS post_type VARCHAR(20) NOT NULL DEFAULT 'free';

INSERT INTO board.tb_post_categories (category_key, name, icon_url, summary, display_order)
VALUES
('lol', '롤', 'https://drive.towardadiamond.com/tad/category-icons/lol.webp', '롤 게시판', 1),
('maple', '메이플랜드', 'https://drive.towardadiamond.com/tad/category-icons/maple.webp', '메이플랜드 게시판', 2),
('free', '자유', null, '자유 게시판', 3);

CREATE TABLE IF NOT EXISTS board.tb_report (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    reported_user_id BIGINT NOT NULL,
    reason_code VARCHAR(50) NOT NULL,
    reason_detail TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    handled_by BIGINT,
    handled_at TIMESTAMP,
    handler_memo TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_report_reporter
        FOREIGN KEY (reporter_id) REFERENCES auth.tb_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_report_reported_user
        FOREIGN KEY (reported_user_id) REFERENCES auth.tb_user(id) ON DELETE RESTRICT,
    CONSTRAINT fk_report_handled_by
        FOREIGN KEY (handled_by) REFERENCES auth.tb_user(id) ON DELETE SET NULL,
    CONSTRAINT uq_board_report_reporter_target
        UNIQUE (reporter_id, target_type, target_id)
);

CREATE INDEX IF NOT EXISTS idx_board_report_status_created_at
ON board.tb_report (status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_board_report_target
ON board.tb_report (target_type, target_id);

CREATE INDEX IF NOT EXISTS idx_board_report_reported_user
ON board.tb_report (reported_user_id);

CREATE TABLE IF NOT EXISTS board.tb_user_sanction (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    sanction_type VARCHAR(20) NOT NULL,
    reason TEXT NOT NULL,
    starts_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP,
    revoked_by BIGINT,
    revoke_reason TEXT,

    CONSTRAINT fk_user_sanction_user
        FOREIGN KEY (user_id) REFERENCES auth.tb_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_sanction_created_by
        FOREIGN KEY (created_by) REFERENCES auth.tb_user(id) ON DELETE SET NULL,
    CONSTRAINT fk_user_sanction_revoked_by
        FOREIGN KEY (revoked_by) REFERENCES auth.tb_user(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_user_sanction_user_created_at
ON board.tb_user_sanction (user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_user_sanction_active
ON board.tb_user_sanction (user_id, revoked_at, starts_at, expires_at);
