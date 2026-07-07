-- Item 2: devolução com handshake. RETURN_REQUESTED ainda ocupa o livro
-- (o leitor mantém a posse até o dono confirmar), então entra na garantia de
-- "no máximo 1 aluguel ativo por livro".
DROP INDEX IF EXISTS uq_rentals_active_book;

CREATE UNIQUE INDEX uq_rentals_active_book
    ON rentals (book_id)
    WHERE status IN ('PENDING', 'APPROVED', 'ACTIVE', 'RETURN_REQUESTED');
