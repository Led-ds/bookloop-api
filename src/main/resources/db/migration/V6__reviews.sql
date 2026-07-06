-- ============================================================
-- BookLoop :: avaliações (livros e pessoas) + reputação denormalizada
-- ============================================================

-- Médias denormalizadas para leitura rápida (home/cards/perfil).
ALTER TABLE books ADD COLUMN rating_avg   DOUBLE PRECISION NOT NULL DEFAULT 0;
ALTER TABLE books ADD COLUMN rating_count INTEGER      NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN rating_avg   DOUBLE PRECISION NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN rating_count INTEGER      NOT NULL DEFAULT 0;

CREATE TABLE reviews (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rental_id      UUID NOT NULL,
    author_id      UUID NOT NULL,
    review_type    VARCHAR(10) NOT NULL,          -- BOOK | USER
    target_book_id UUID,
    target_user_id UUID,
    rating         INTEGER NOT NULL,
    comment        VARCHAR(150),
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_reviews_rental FOREIGN KEY (rental_id)      REFERENCES rentals (id),
    CONSTRAINT fk_reviews_author FOREIGN KEY (author_id)      REFERENCES users (id),
    CONSTRAINT fk_reviews_book   FOREIGN KEY (target_book_id) REFERENCES books (id),
    CONSTRAINT fk_reviews_user   FOREIGN KEY (target_user_id) REFERENCES users (id),
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    -- exatamente um alvo, coerente com o tipo
    CONSTRAINT chk_reviews_target CHECK (
        (review_type = 'BOOK' AND target_book_id IS NOT NULL AND target_user_id IS NULL) OR
        (review_type = 'USER' AND target_user_id IS NOT NULL AND target_book_id IS NULL)
    ),
    -- sem auto-avaliação
    CONSTRAINT chk_reviews_no_self CHECK (target_user_id IS NULL OR target_user_id <> author_id)
);

-- 1 avaliação por aluguel por direção:
--  - de livro: uma por (aluguel, autor)
CREATE UNIQUE INDEX uq_reviews_book ON reviews (rental_id, author_id) WHERE review_type = 'BOOK';
--  - de pessoa: uma por (aluguel, autor, alvo)
CREATE UNIQUE INDEX uq_reviews_user ON reviews (rental_id, author_id, target_user_id) WHERE review_type = 'USER';

CREATE INDEX idx_reviews_target_book ON reviews (target_book_id);
CREATE INDEX idx_reviews_target_user ON reviews (target_user_id);
CREATE INDEX idx_reviews_author      ON reviews (author_id);
