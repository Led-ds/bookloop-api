-- Item 6: concorrência na solicitação de aluguel.

-- 1) Lock otimista no livro (protege transições de estado sob concorrência).
ALTER TABLE books ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- 2) Saneamento defensivo: ambientes reais podem ter dados anteriores à regra,
--    com mais de um aluguel "ativo" para o mesmo livro. Antes de criar o índice
--    único, mantém apenas o mais recente de cada livro e cancela os demais,
--    senão o CREATE UNIQUE INDEX falha (e derruba o boot da aplicação).
WITH ativos AS (
    SELECT id,
           row_number() OVER (PARTITION BY book_id ORDER BY created_at DESC) AS rn
    FROM rentals
    WHERE status IN ('PENDING', 'APPROVED', 'ACTIVE')
)
UPDATE rentals
   SET status = 'CANCELLED'
 WHERE id IN (SELECT id FROM ativos WHERE rn > 1);

-- 3) Garantia no banco: no máximo 1 aluguel "ativo" por livro.
CREATE UNIQUE INDEX uq_rentals_active_book
    ON rentals (book_id)
    WHERE status IN ('PENDING', 'APPROVED', 'ACTIVE');
