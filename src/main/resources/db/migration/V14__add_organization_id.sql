-- Fatia 1 (fundação multi-tenant) — parte 2: EXPAND.
-- Adiciona organization_id como NULLABLE nas tabelas existentes. Ainda sem
-- preencher, sem NOT NULL e sem FK. É seguro: o código atual ignora esta coluna,
-- então nada quebra. O backfill (parte 3) preenche; a restrição (parte 4) trava.

ALTER TABLE books         ADD COLUMN organization_id UUID;
ALTER TABLE rentals       ADD COLUMN organization_id UUID;
ALTER TABLE reservations  ADD COLUMN organization_id UUID;
ALTER TABLE reviews       ADD COLUMN organization_id UUID;
ALTER TABLE notifications ADD COLUMN organization_id UUID;
