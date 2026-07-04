-- Sistema de notificações v1 — tabela de notificações internas, persistidas e consultáveis.
CREATE TABLE notifications (
    id                UUID PRIMARY KEY,
    recipient_user_id UUID         NOT NULL,
    actor_user_id     UUID,
    type              VARCHAR(40)  NOT NULL,
    title             VARCHAR(160) NOT NULL,
    message           VARCHAR(500) NOT NULL,
    target_type       VARCHAR(40),
    target_id         UUID,
    action_url        VARCHAR(300),
    is_read           BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at           TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_notifications_recipient      ON notifications (recipient_user_id);
CREATE INDEX idx_notifications_read           ON notifications (is_read);
CREATE INDEX idx_notifications_created_at     ON notifications (created_at);
CREATE INDEX idx_notifications_recipient_read ON notifications (recipient_user_id, is_read);
