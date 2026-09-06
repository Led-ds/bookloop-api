-- Fatia 1 (fundação multi-tenant) — parte 1: comunidades e vínculos.
-- Cria apenas as tabelas novas. A adição de organization_id às tabelas
-- existentes e o backfill dos dados vêm em migrations seguintes (expand/contract).

CREATE TABLE organizations (
    id             UUID PRIMARY KEY,
    code           VARCHAR(20)  NOT NULL,
    name           VARCHAR(120) NOT NULL,
    description    VARCHAR(500) NOT NULL,
    avatar_url     VARCHAR(512),
    plan           VARCHAR(20)  NOT NULL DEFAULT 'STARTER',
    member_limit   INTEGER      NOT NULL DEFAULT 20,
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    owner_user_id  UUID         NOT NULL REFERENCES users (id),
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_organizations_code UNIQUE (code)
);

CREATE INDEX idx_organizations_owner ON organizations (owner_user_id);

CREATE TABLE memberships (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users (id),
    organization_id UUID NOT NULL REFERENCES organizations (id),
    role            VARCHAR(20) NOT NULL,   -- OWNER | ADMIN | MEMBER
    status          VARCHAR(20) NOT NULL,   -- ACTIVE | REMOVED
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_membership_user_org UNIQUE (user_id, organization_id)
);

CREATE INDEX idx_memberships_org  ON memberships (organization_id);
CREATE INDEX idx_memberships_user ON memberships (user_id);
