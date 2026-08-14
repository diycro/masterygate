-- V1 — Sprint 0 baseline schema.
-- Includes the "keep-options-open" seams: org_id (multi-user later),
-- user_settings (BYOK), and feature_flags. pgvector is enabled now so
-- embeddings tables can be added in a later migration with no fuss.

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id        UUID NULL,                       -- multi-tenant seam (unused in single-user)
    email         TEXT NOT NULL UNIQUE,
    name          TEXT,
    auth_provider TEXT NOT NULL DEFAULT 'local',
    auth_subject  TEXT,
    role          TEXT NOT NULL DEFAULT 'user',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE profiles (
    user_id      UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    target_role  TEXT,
    experience   TEXT,
    budget_cap   NUMERIC,
    currency     TEXT DEFAULT 'INR',
    weekly_hours INT,
    timezone     TEXT,
    deadline     DATE
);

-- BYOK: each user can bring their own LLM key so hosting cost stays near zero.
CREATE TABLE user_settings (
    user_id        UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    llm_provider   TEXT DEFAULT 'openai',
    llm_api_key_enc TEXT NULL,                     -- store ENCRYPTED, never plaintext
    daily_call_cap INT DEFAULT 200
);

CREATE TABLE feature_flags (
    key      TEXT PRIMARY KEY,
    enabled  BOOLEAN NOT NULL DEFAULT false,
    rollout  JSONB
);

INSERT INTO feature_flags (key, enabled) VALUES
    ('path_generation', false),
    ('agents_mode',     false),
    ('mock_interviews', false)
ON CONFLICT (key) DO NOTHING;

-- A dev user so the app has something to reference in Sprint 0.
INSERT INTO users (id, email, name, auth_provider)
VALUES ('00000000-0000-0000-0000-000000000001', 'dev@studio.local', 'Dev User', 'local')
ON CONFLICT (email) DO NOTHING;
