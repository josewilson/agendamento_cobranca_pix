-- Login de Cliente. senha_hash NULL = cliente legado sem login (nao ha senha a "adivinhar").
ALTER TABLE cliente ADD COLUMN senha_hash VARCHAR(255);
-- Email unico so entre quem tem login (nao quebra clientes legados com email repetido).
CREATE UNIQUE INDEX uk_cliente_email_login ON cliente (lower(email)) WHERE senha_hash IS NOT NULL;
