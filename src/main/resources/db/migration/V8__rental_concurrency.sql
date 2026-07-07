-- Item 6: concorrência na solicitação de aluguel.

-- 1) Lock otimista no livro (protege transições de estado sob concorrência).
ALTER TABLE books ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- 2) Garantia no banco: no máximo 1 aluguel "ativo" por livro.
--    Índice único parcial sobre os estados que ocupam o livro.
--    Observação: o item 2 (devolução handshake) estenderá esta lista com
--    'RETURN_REQUESTED' quando aquele status existir.
--    Pré-requisito: nenhum livro pode ter 2+ aluguéis nesses estados hoje
--    (o seed atual só tem aluguéis RETURNED, então é seguro).
CREATE UNIQUE INDEX uq_rentals_active_book
    ON rentals (book_id)
    WHERE status IN ('PENDING', 'APPROVED', 'ACTIVE');
