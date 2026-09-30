CREATE TABLE prestador (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    documento_numero VARCHAR(20) NOT NULL,
    documento_tipo VARCHAR(10) NOT NULL,
    politica_antecedencia_minima_segundos BIGINT NOT NULL,
    politica_percentual_retido NUMERIC(5, 2) NOT NULL,
    CONSTRAINT uk_prestador_documento UNIQUE (documento_numero, documento_tipo)
);
