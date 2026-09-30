CREATE TABLE cliente (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    documento_numero VARCHAR(20) NOT NULL,
    documento_tipo VARCHAR(10) NOT NULL,
    quantidade_no_show INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uk_cliente_documento UNIQUE (documento_numero, documento_tipo)
);
