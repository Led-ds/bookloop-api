-- Fatia 1 (fundação multi-tenant) — parte 3: BACKFILL.
-- Move todos os dados existentes para uma "comunidade original", de forma
-- idempotente e independente de IDs específicos do seed. Funciona tanto num
-- banco com dados (dev/seed) quanto num banco recém-criado (sem usuários).

-- 1) Cria a comunidade original — SOMENTE se houver ao menos um usuário e ainda
--    não existir. O owner é o usuário mais antigo (o primeiro cadastrado).
INSERT INTO organizations (id, code, name, description, plan, member_limit, status, owner_user_id, created_at, updated_at)
SELECT gen_random_uuid(),
       'BOOKLOOP-ORIGINAL',
       'BookLoop (comunidade original)',
       'Comunidade criada automaticamente na migração para o modelo multi-tenant. Reúne todos os livros e membros que já existiam.',
       'UNLIMITED',
       100000,
       'ACTIVE',
       (SELECT id FROM users ORDER BY created_at ASC, id ASC LIMIT 1),
       now(),
       now()
WHERE EXISTS (SELECT 1 FROM users)
  AND NOT EXISTS (SELECT 1 FROM organizations WHERE code = 'BOOKLOOP-ORIGINAL');

-- 2) Cria memberships para TODOS os usuários existentes.
--    O dono da comunidade vira OWNER; os demais viram MEMBER.
--    Idempotente: o UNIQUE (user_id, organization_id) + WHERE evita duplicar.
INSERT INTO memberships (id, user_id, organization_id, role, status, created_at, updated_at)
SELECT gen_random_uuid(),
       u.id,
       o.id,
       CASE WHEN u.id = o.owner_user_id THEN 'OWNER' ELSE 'MEMBER' END,
       'ACTIVE',
       now(),
       now()
FROM users u
CROSS JOIN organizations o
WHERE o.code = 'BOOKLOOP-ORIGINAL'
  AND NOT EXISTS (
      SELECT 1 FROM memberships m
      WHERE m.user_id = u.id AND m.organization_id = o.id
  );

-- 3) Preenche organization_id nas tabelas existentes, apontando para a
--    comunidade original. Só as linhas ainda nulas (idempotente).
UPDATE books b
   SET organization_id = o.id
  FROM organizations o
 WHERE o.code = 'BOOKLOOP-ORIGINAL' AND b.organization_id IS NULL;

UPDATE rentals r
   SET organization_id = o.id
  FROM organizations o
 WHERE o.code = 'BOOKLOOP-ORIGINAL' AND r.organization_id IS NULL;

UPDATE reservations rs
   SET organization_id = o.id
  FROM organizations o
 WHERE o.code = 'BOOKLOOP-ORIGINAL' AND rs.organization_id IS NULL;

UPDATE reviews rv
   SET organization_id = o.id
  FROM organizations o
 WHERE o.code = 'BOOKLOOP-ORIGINAL' AND rv.organization_id IS NULL;

UPDATE notifications n
   SET organization_id = o.id
  FROM organizations o
 WHERE o.code = 'BOOKLOOP-ORIGINAL' AND n.organization_id IS NULL;
