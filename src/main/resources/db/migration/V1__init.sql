-- ============================================================
-- BookLoop :: schema inicial
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ---------- users ----------
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(120) NOT NULL,
    email           VARCHAR(180) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    avatar_url      VARCHAR(512),
    bio             VARCHAR(500),
    location        VARCHAR(120),
    penalties_count INTEGER NOT NULL DEFAULT 0,
    role            VARCHAR(20) NOT NULL DEFAULT 'USER',
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email)
);
CREATE UNIQUE INDEX idx_users_email ON users (email);

-- ---------- books ----------
CREATE TABLE books (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title          VARCHAR(200) NOT NULL,
    author         VARCHAR(160) NOT NULL,
    isbn           VARCHAR(20),
    genre          VARCHAR(30) NOT NULL,
    description    VARCHAR(2000),
    book_condition VARCHAR(20) NOT NULL,
    cover_url      VARCHAR(512),
    is_public      BOOLEAN NOT NULL DEFAULT TRUE,
    status         VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    owner_id       UUID NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_books_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);
CREATE INDEX idx_books_owner  ON books (owner_id);
CREATE INDEX idx_books_status ON books (status);
CREATE INDEX idx_books_genre  ON books (genre);
-- acelera buscas textuais por título/autor
CREATE INDEX idx_books_title  ON books (LOWER(title));
CREATE INDEX idx_books_author ON books (LOWER(author));

-- ---------- rentals ----------
CREATE TABLE rentals (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id          UUID NOT NULL,
    renter_id        UUID NOT NULL,
    owner_id         UUID NOT NULL,
    message          VARCHAR(500),
    start_date       DATE NOT NULL,
    end_date         DATE NOT NULL,
    return_date      DATE,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    term_accepted    BOOLEAN NOT NULL DEFAULT FALSE,
    term_signed_at   TIMESTAMPTZ,
    term_signer_name VARCHAR(120),
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_rentals_book   FOREIGN KEY (book_id)   REFERENCES books (id),
    CONSTRAINT fk_rentals_renter FOREIGN KEY (renter_id) REFERENCES users (id),
    CONSTRAINT fk_rentals_owner  FOREIGN KEY (owner_id)  REFERENCES users (id),
    CONSTRAINT chk_rentals_dates CHECK (end_date >= start_date)
);
CREATE INDEX idx_rentals_renter ON rentals (renter_id);
CREATE INDEX idx_rentals_owner  ON rentals (owner_id);
CREATE INDEX idx_rentals_book   ON rentals (book_id);
CREATE INDEX idx_rentals_status ON rentals (status);
