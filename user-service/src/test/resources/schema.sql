CREATE TABLE IF NOT EXISTS user_profiles (
    id          UUID PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255),
    avatar_url  VARCHAR(512)
);
