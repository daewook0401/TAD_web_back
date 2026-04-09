CREATE TABLE auth.tb_user (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    nickname VARCHAR(50) UNIQUE NOT NULL,
    profile_image_url TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auth.tb_role (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);

INSERT INTO auth.tb_role (role_name, description)
VALUES
('ROLE_USER', '일반 사용자'),
('ROLE_ADMIN', '관리자');


CREATE TABLE auth.tb_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES auth.tb_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id) REFERENCES auth.tb_role(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_role UNIQUE (user_id, role_id)
);

-- drop table auth.tb_oauth_account ;

CREATE TABLE auth.tb_oauth_account (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    provider_email VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_oauth_account_user
        FOREIGN KEY (user_id) REFERENCES auth.tb_user(id) ON DELETE CASCADE,
    CONSTRAINT uq_oauth_provider_user UNIQUE (provider, provider_user_id)
);

-- drop table auth.tb_email_auth ;

CREATE TABLE auth.tb_email_auth (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    auth_code VARCHAR(100) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_email_auth_user
        FOREIGN KEY (user_id) REFERENCES auth.tb_user(id) ON DELETE CASCADE
);

-- drop table auth.tb_login_history ;

CREATE TABLE auth.tb_login_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    login_type VARCHAR(30) NOT NULL,
    login_result VARCHAR(20) NOT NULL,
    ip_address VARCHAR(100),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_login_history_user
        FOREIGN KEY (user_id) REFERENCES auth.tb_user(id) ON DELETE CASCADE
);

-- drop table auth.tb_token_history ;

CREATE TABLE auth.tb_token_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_type VARCHAR(20) NOT NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired_at TIMESTAMP,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_token_history_user
        FOREIGN KEY (user_id) REFERENCES auth.tb_user(id) ON DELETE CASCADE
);