-- Perfil do usuário (v1.2): endereço estruturado + flag de perfil completo.
ALTER TABLE users ADD COLUMN city              VARCHAR(80);
ALTER TABLE users ADD COLUMN state             VARCHAR(40);
ALTER TABLE users ADD COLUMN address_line      VARCHAR(160);
ALTER TABLE users ADD COLUMN neighborhood      VARCHAR(80);
ALTER TABLE users ADD COLUMN postal_code       VARCHAR(20);
ALTER TABLE users ADD COLUMN profile_completed BOOLEAN NOT NULL DEFAULT FALSE;
