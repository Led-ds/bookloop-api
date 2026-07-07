-- Item 1: edição de avaliação. Marca avaliações que foram editadas pelo autor.
ALTER TABLE reviews ADD COLUMN edited BOOLEAN NOT NULL DEFAULT FALSE;
