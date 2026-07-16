BEGIN;

UPDATE board.tb_post_categories
SET icon_url = split_part(icon_url, '/', 1)
    || '//' || split_part(icon_url, '/', 3)
    || '/public/tad/' || substring(icon_url FROM '/tad/(.*)$')
WHERE icon_url ~ '^https?://[^/]+/tad/';

COMMIT;
