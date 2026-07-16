BEGIN;

ALTER TABLE board.tb_post_attachment
    ADD COLUMN IF NOT EXISTS bucket VARCHAR(100),
    ADD COLUMN IF NOT EXISTS object_key VARCHAR(500);

ALTER TABLE board.tb_comment_attachment
    ADD COLUMN IF NOT EXISTS bucket VARCHAR(100),
    ADD COLUMN IF NOT EXISTS object_key VARCHAR(500);

UPDATE board.tb_post_attachment
SET
    bucket = 'tad',
    object_key = 'board/posts/' || post_id || '/' || stored_name
WHERE (bucket IS NULL OR object_key IS NULL)
  AND stored_name IS NOT NULL
  AND stored_name <> '';

UPDATE board.tb_comment_attachment
SET
    bucket = 'tad',
    object_key = 'board/comments/' || comment_id || '/' || stored_name
WHERE (bucket IS NULL OR object_key IS NULL)
  AND stored_name IS NOT NULL
  AND stored_name <> '';

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM board.tb_post_attachment
        WHERE bucket IS NULL OR object_key IS NULL
    ) OR EXISTS (
        SELECT 1
        FROM board.tb_comment_attachment
        WHERE bucket IS NULL OR object_key IS NULL
    ) THEN
        RAISE EXCEPTION '첨부 객체 경로를 복원할 수 없는 행이 있습니다.';
    END IF;
END
$$;

ALTER TABLE board.tb_post_attachment
    ALTER COLUMN bucket SET NOT NULL,
    ALTER COLUMN object_key SET NOT NULL;

ALTER TABLE board.tb_comment_attachment
    ALTER COLUMN bucket SET NOT NULL,
    ALTER COLUMN object_key SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_post_attachment_object_locator
    ON board.tb_post_attachment (bucket, object_key);

CREATE INDEX IF NOT EXISTS idx_comment_attachment_object_locator
    ON board.tb_comment_attachment (bucket, object_key);

COMMIT;
