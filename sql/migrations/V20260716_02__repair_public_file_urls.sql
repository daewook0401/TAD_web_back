BEGIN;

UPDATE analysis.tb_game
SET screenshot_url = split_part(screenshot_url, '/', 1)
    || '//' || split_part(screenshot_url, '/', 3)
    || '/public/' || bucket || '/' || object_key
WHERE bucket = 'tad'
  AND screenshot_url !~ '^https?://[^/]+/public/tad/';

UPDATE board.tb_post_attachment
SET file_url = split_part(file_url, '/', 1)
    || '//' || split_part(file_url, '/', 3)
    || '/public/' || bucket || '/' || object_key
WHERE bucket = 'tad'
  AND file_url !~ '^https?://[^/]+/public/tad/';

UPDATE board.tb_comment_attachment
SET file_url = split_part(file_url, '/', 1)
    || '//' || split_part(file_url, '/', 3)
    || '/public/' || bucket || '/' || object_key
WHERE bucket = 'tad'
  AND file_url !~ '^https?://[^/]+/public/tad/';

COMMIT;
