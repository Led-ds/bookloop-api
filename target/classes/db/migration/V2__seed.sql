-- ============================================================
-- BookLoop :: dados de exemplo (seed)
-- Senha de todos os usuários de exemplo: senha12345
-- ============================================================

-- hash bcrypt de 'senha12345'
-- $2b$10$mofspOKpuVIwxrtHYpPfx.pkiS/nLZb/FM4my2d26rlpjW8WmzU8y

INSERT INTO users (id, name, email, password_hash, bio, location, role, created_at, updated_at) VALUES
('11111111-1111-1111-1111-111111111111', 'Ana Lima',   'ana@bookloop.dev',
 '$2b$10$mofspOKpuVIwxrtHYpPfx.pkiS/nLZb/FM4my2d26rlpjW8WmzU8y',
 'Leitora voraz de ficção e fantasia.', 'Niterói, RJ', 'USER', now(), now()),
('22222222-2222-2222-2222-222222222222', 'Bruno Souza', 'bruno@bookloop.dev',
 '$2b$10$mofspOKpuVIwxrtHYpPfx.pkiS/nLZb/FM4my2d26rlpjW8WmzU8y',
 'Compartilho meus técnicos parados na estante.', 'São Gonçalo, RJ', 'USER', now(), now()),
('33333333-3333-3333-3333-333333333333', 'Admin BookLoop', 'admin@bookloop.dev',
 '$2b$10$mofspOKpuVIwxrtHYpPfx.pkiS/nLZb/FM4my2d26rlpjW8WmzU8y',
 'Suporte e moderação.', 'Rio de Janeiro, RJ', 'ADMIN', now(), now());

INSERT INTO books (id, title, author, isbn, genre, description, book_condition, cover_url, is_public, status, owner_id, created_at, updated_at) VALUES
('aaaaaaaa-0000-0000-0000-000000000001', 'O Hobbit', 'J.R.R. Tolkien', '9788595084742', 'FANTASIA',
 'Edição em ótimo estado, capa dura.', 'OTIMO', NULL, TRUE, 'AVAILABLE',
 '11111111-1111-1111-1111-111111111111', now(), now()),
('aaaaaaaa-0000-0000-0000-000000000002', 'Clean Code', 'Robert C. Martin', '9788576082675', 'TECNICO',
 'Clássico de engenharia de software. Algumas marcações a lápis.', 'BOM', NULL, TRUE, 'AVAILABLE',
 '22222222-2222-2222-2222-222222222222', now(), now()),
('aaaaaaaa-0000-0000-0000-000000000003', '1984', 'George Orwell', '9788535914849', 'FICCAO',
 'Distopia atemporal. Páginas levemente amareladas.', 'REGULAR', NULL, TRUE, 'AVAILABLE',
 '11111111-1111-1111-1111-111111111111', now(), now()),
('aaaaaaaa-0000-0000-0000-000000000004', 'Domain-Driven Design', 'Eric Evans', '9788550800653', 'TECNICO',
 'Referência sobre modelagem de domínio.', 'OTIMO', NULL, TRUE, 'AVAILABLE',
 '22222222-2222-2222-2222-222222222222', now(), now()),
('aaaaaaaa-0000-0000-0000-000000000005', 'A Revolução dos Bichos', 'George Orwell', '9788535909555', 'FICCAO',
 'Fábula política. Em bom estado.', 'BOM', NULL, FALSE, 'UNAVAILABLE',
 '11111111-1111-1111-1111-111111111111', now(), now());
