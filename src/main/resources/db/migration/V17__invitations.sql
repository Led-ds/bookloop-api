-- Fatia 3 — Convites. Entrada em comunidades privadas: só por convite/código.
CREATE TABLE invitations (
    id              UUID PRIMARY KEY,
    organization_id UUID         NOT NULL REFERENCES organizations (id),
    email           VARCHAR(180),                       -- nulo = convite por código aberto
    token           VARCHAR(64)  NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'MEMBER',
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING', -- PENDING|ACCEPTED|CANCELLED|EXPIRED
    invited_by      UUID         NOT NULL REFERENCES users (id),
    expires_at      TIMESTAMPTZ  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_invitations_token UNIQUE (token)
);

CREATE INDEX idx_invitations_org    ON invitations (organization_id);
CREATE INDEX idx_invitations_status ON invitations (organization_id, status);
