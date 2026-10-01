ALTER TABLE prestador ADD COLUMN telefone VARCHAR(20);
UPDATE prestador SET telefone = '00000000000' WHERE telefone IS NULL;
ALTER TABLE prestador ALTER COLUMN telefone SET NOT NULL;
