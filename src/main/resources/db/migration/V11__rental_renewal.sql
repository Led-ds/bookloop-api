-- Item 3: renovação solicitada pelo leitor com aprovação do dono.
-- Modelada como campos no aluguel (sem novo status): o aluguel segue ACTIVE.
ALTER TABLE rentals ADD COLUMN renewal_status          VARCHAR(15);
ALTER TABLE rentals ADD COLUMN renewal_requested_until DATE;
