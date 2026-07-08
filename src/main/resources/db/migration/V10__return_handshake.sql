-- Item 2: devolução com handshake. RETURN_REQUESTED ainda ocupa o livro
-- (o leitor mantém a posse até o dono confirmar), então entra na garantia de
-- "no máximo 1 aluguel ativo por livro".
DROP INDEX IF EXISTS uq_rentals_active_book;

-- Saneamento defensivo também aqui, agora considerando RETURN_REQUESTED.
WITH ativos AS (
    SELECT id,
           row_number() OVER (PARTITION BY book_id ORDER BY created_at DESC) AS rn
    FROM rentals
    WHERE status IN ('PENDING', 'APPROVED', 'ACTIVE', 'RETURN_REQUESTED')
)
UPDATE rentals
   SET status = 'CANCELLED'
 WHERE id IN (SELECT id FROM ativos WHERE rn > 1);

CREATE UNIQUE INDEX uq_rentals_active_book
    ON rentals (book_id)
    WHERE status IN ('PENDING', 'APPROVED', 'ACTIVE', 'RETURN_REQUESTED');
