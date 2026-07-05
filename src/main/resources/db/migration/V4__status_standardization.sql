-- Padronização dos status de negócio (v1.2).
-- Livro: UNAVAILABLE deixa de ser status; vira AVAILABLE + is_public = FALSE (visibilidade via isPublic).
UPDATE books SET status = 'AVAILABLE', is_public = FALSE WHERE status = 'UNAVAILABLE';

-- Aluguel: LATE renomeado para OVERDUE.
UPDATE rentals SET status = 'OVERDUE' WHERE status = 'LATE';
