CREATE SCHEMA IF NOT EXISTS analysis;

CREATE TABLE IF NOT EXISTS analysis.tb_player (
    id BIGSERIAL PRIMARY KEY,
    player_name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS analysis.tb_game (
    id BIGSERIAL PRIMARY KEY,
    uploader_id BIGINT,
    bucket VARCHAR(100) NOT NULL,
    object_key VARCHAR(500) NOT NULL UNIQUE,
    screenshot_url TEXT NOT NULL,
    winner VARCHAR(10),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    confirmed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_game_uploader
        FOREIGN KEY (uploader_id) REFERENCES auth.tb_user(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS analysis.tb_game_player_stat (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL,
    player_id BIGINT,
    player_name_snapshot VARCHAR(50),
    team_key VARCHAR(10) NOT NULL,
    slot_number INT NOT NULL,
    kills INT,
    deaths INT,
    assists INT,
    cs INT,
    gold INT,
    is_winner BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_game_player_stat_game
        FOREIGN KEY (game_id) REFERENCES analysis.tb_game(id) ON DELETE CASCADE,
    CONSTRAINT fk_game_player_stat_player
        FOREIGN KEY (player_id) REFERENCES analysis.tb_player(id) ON DELETE SET NULL,
    CONSTRAINT uq_game_team_slot UNIQUE (game_id, team_key, slot_number)
);

CREATE INDEX IF NOT EXISTS idx_game_player_stat_game
ON analysis.tb_game_player_stat (game_id);

CREATE INDEX IF NOT EXISTS idx_game_player_stat_player
ON analysis.tb_game_player_stat (player_id);

CREATE INDEX IF NOT EXISTS idx_game_uploader_created_at
ON analysis.tb_game (uploader_id, created_at DESC);

ALTER TABLE analysis.tb_game
ADD COLUMN IF NOT EXISTS uploader_id BIGINT;

ALTER TABLE analysis.tb_game
ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

ALTER TABLE analysis.tb_game
ADD COLUMN IF NOT EXISTS confirmed_at TIMESTAMP;

ALTER TABLE analysis.tb_game
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE analysis.tb_game
ALTER COLUMN winner DROP NOT NULL;

CREATE OR REPLACE FUNCTION analysis.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_analysis_player_updated_at
BEFORE UPDATE ON analysis.tb_player
FOR EACH ROW
EXECUTE FUNCTION analysis.set_updated_at();

CREATE TRIGGER trg_analysis_game_updated_at
BEFORE UPDATE ON analysis.tb_game
FOR EACH ROW
EXECUTE FUNCTION analysis.set_updated_at();

-- DROP

-- DROP TRIGGER IF EXISTS trg_analysis_game_updated_at ON analysis.tb_game;
-- DROP TRIGGER IF EXISTS trg_analysis_player_updated_at ON analysis.tb_player;

-- DROP FUNCTION IF EXISTS analysis.set_updated_at();

-- DROP TABLE IF EXISTS analysis.tb_game_player_stat;
-- DROP TABLE IF EXISTS analysis.tb_game;
-- DROP TABLE IF EXISTS analysis.tb_player;

-- DROP SCHEMA IF EXISTS analysis;
