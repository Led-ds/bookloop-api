-- Fatia 1 (fundação multi-tenant) — parte 4: CONTRACT.
-- Agora que o backfill (V15) preencheu organization_id em todas as linhas,
-- trava a coluna: NOT NULL + FK + índice, em cada tabela.
--
-- NÃO remove users.role nesta migration. Esse campo ainda alimenta a
-- autenticação (JWT + Spring Security GrantedAuthority); removê-lo exige
-- refatorar a autorização e será feito num passo dedicado, não aqui.

-- books
ALTER TABLE books ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE books ADD CONSTRAINT fk_books_org
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_books_org ON books (organization_id);

-- rentals
ALTER TABLE rentals ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE rentals ADD CONSTRAINT fk_rentals_org
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_rentals_org ON rentals (organization_id);

-- reservations
ALTER TABLE reservations ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE reservations ADD CONSTRAINT fk_reservations_org
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_reservations_org ON reservations (organization_id);

-- reviews
ALTER TABLE reviews ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE reviews ADD CONSTRAINT fk_reviews_org
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_reviews_org ON reviews (organization_id);

-- notifications
ALTER TABLE notifications ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE notifications ADD CONSTRAINT fk_notifications_org
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_notifications_org ON notifications (organization_id);
