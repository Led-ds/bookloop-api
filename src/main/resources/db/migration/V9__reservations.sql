-- Item 4a: fila de reserva de livros.
CREATE TABLE reservations (
    id               UUID PRIMARY KEY,
    book_id          UUID NOT NULL REFERENCES books (id),
    user_id          UUID NOT NULL REFERENCES users (id),
    status           VARCHAR(15) NOT NULL,
    offer_expires_at TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_reservations_book   ON reservations (book_id);
CREATE INDEX idx_reservations_user   ON reservations (user_id);
CREATE INDEX idx_reservations_status ON reservations (status);
