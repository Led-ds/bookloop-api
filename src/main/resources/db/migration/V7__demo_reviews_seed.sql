-- ============================================================
-- BookLoop :: seed de demonstração (perfis, aluguéis devolvidos e avaliações)
-- Objetivo: a Home e os perfis já nascem com conteúdo real (sem mock no front).
-- Referencia os usuários/livros do V2__seed.sql. Idempotência via UUIDs fixos.
-- ============================================================

-- Perfis completos para os usuários de exemplo (avatar, cidade/estado).
UPDATE users SET
    avatar_url        = 'https://i.pravatar.cc/150?img=47',
    city              = 'Niterói',
    state             = 'RJ',
    profile_completed = TRUE
WHERE id = '11111111-1111-1111-1111-111111111111';   -- Ana Lima

UPDATE users SET
    avatar_url        = 'https://i.pravatar.cc/150?img=12',
    city              = 'São Gonçalo',
    state             = 'RJ',
    profile_completed = TRUE
WHERE id = '22222222-2222-2222-2222-222222222222';   -- Bruno Souza

-- Capas para os livros de exemplo (vitrine com imagens reais).
UPDATE books SET cover_url = 'https://m.media-amazon.com/images/I/71jcVMNlaIL._AC_UF1000,1000_QL80_.jpg'
    WHERE id = 'aaaaaaaa-0000-0000-0000-000000000001';  -- O Hobbit
UPDATE books SET cover_url = 'https://m.media-amazon.com/images/I/41xShlnTZTL._SY445_SX342_.jpg'
    WHERE id = 'aaaaaaaa-0000-0000-0000-000000000002';  -- Clean Code
UPDATE books SET cover_url = 'https://m.media-amazon.com/images/I/71LqtAEMMiL._SL1500_.jpg'
    WHERE id = 'aaaaaaaa-0000-0000-0000-000000000003';  -- 1984
UPDATE books SET cover_url = 'https://m.media-amazon.com/images/I/91X0mDbJExL._SL1500_.jpg'
    WHERE id = 'aaaaaaaa-0000-0000-0000-000000000004';  -- Domain-Driven Design

-- Aluguéis devolvidos (base para as avaliações). Datas no passado.
INSERT INTO rentals (id, book_id, renter_id, owner_id, message, start_date, end_date, return_date,
                     status, term_accepted, term_signed_at, term_signer_name, created_at, updated_at) VALUES
('cccccccc-0000-0000-0000-000000000001',
 'aaaaaaaa-0000-0000-0000-000000000001',   -- O Hobbit (dona: Ana)
 '22222222-2222-2222-2222-222222222222',   -- leitor: Bruno
 '11111111-1111-1111-1111-111111111111',   -- dona:   Ana
 'Posso pegar emprestado?', (now() - INTERVAL '40 days')::date, (now() - INTERVAL '20 days')::date,
 (now() - INTERVAL '19 days')::date, 'RETURNED', TRUE, now() - INTERVAL '40 days', 'Bruno Souza',
 now() - INTERVAL '40 days', now() - INTERVAL '19 days'),
('cccccccc-0000-0000-0000-000000000002',
 'aaaaaaaa-0000-0000-0000-000000000002',   -- Clean Code (dono: Bruno)
 '11111111-1111-1111-1111-111111111111',   -- leitora: Ana
 '22222222-2222-2222-2222-222222222222',   -- dono:    Bruno
 'Tenho interesse!', (now() - INTERVAL '35 days')::date, (now() - INTERVAL '15 days')::date,
 (now() - INTERVAL '16 days')::date, 'RETURNED', TRUE, now() - INTERVAL '35 days', 'Ana Lima',
 now() - INTERVAL '35 days', now() - INTERVAL '16 days');

-- Avaliações (BOOK: quem alugou avalia o livro; USER: contrapartes se avaliam).
INSERT INTO reviews (id, rental_id, author_id, review_type, target_book_id, target_user_id,
                     rating, comment, created_at, updated_at) VALUES
-- Bruno -> O Hobbit (livro)
('dddddddd-0000-0000-0000-000000000001', 'cccccccc-0000-0000-0000-000000000001',
 '22222222-2222-2222-2222-222222222222', 'BOOK', 'aaaaaaaa-0000-0000-0000-000000000001', NULL,
 5, 'Edição impecável, adorei reler. Capa dura ótima.', now() - INTERVAL '18 days', now() - INTERVAL '18 days'),
-- Bruno -> Ana (pessoa)
('dddddddd-0000-0000-0000-000000000002', 'cccccccc-0000-0000-0000-000000000001',
 '22222222-2222-2222-2222-222222222222', 'USER', NULL, '11111111-1111-1111-1111-111111111111',
 5, 'Ana é super atenciosa, combinou tudo pelo chat e entregou pontualmente.', now() - INTERVAL '18 days', now() - INTERVAL '18 days'),
-- Ana -> Bruno (pessoa)
('dddddddd-0000-0000-0000-000000000003', 'cccccccc-0000-0000-0000-000000000001',
 '11111111-1111-1111-1111-111111111111', 'USER', NULL, '22222222-2222-2222-2222-222222222222',
 5, 'Bruno cuidou super bem do livro e devolveu antes do prazo.', now() - INTERVAL '17 days', now() - INTERVAL '17 days'),
-- Ana -> Clean Code (livro)
('dddddddd-0000-0000-0000-000000000004', 'cccccccc-0000-0000-0000-000000000002',
 '11111111-1111-1111-1111-111111111111', 'BOOK', 'aaaaaaaa-0000-0000-0000-000000000002', NULL,
 4, 'Clássico. Umas marcações a lápis, mas em bom estado.', now() - INTERVAL '14 days', now() - INTERVAL '14 days');

-- Reputação denormalizada coerente com as avaliações acima.
UPDATE books SET rating_avg = 5.00, rating_count = 1 WHERE id = 'aaaaaaaa-0000-0000-0000-000000000001'; -- O Hobbit
UPDATE books SET rating_avg = 4.00, rating_count = 1 WHERE id = 'aaaaaaaa-0000-0000-0000-000000000002'; -- Clean Code
UPDATE users SET rating_avg = 5.00, rating_count = 1 WHERE id = '11111111-1111-1111-1111-111111111111'; -- Ana
UPDATE users SET rating_avg = 5.00, rating_count = 1 WHERE id = '22222222-2222-2222-2222-222222222222'; -- Bruno
