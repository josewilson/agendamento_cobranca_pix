CREATE TABLE servico (
    id UUID PRIMARY KEY,
    prestador_id UUID NOT NULL REFERENCES prestador (id),
    nome VARCHAR(255) NOT NULL,
    duracao_minutos BIGINT NOT NULL,
    preco NUMERIC(10, 2) NOT NULL,
    percentual_sinal NUMERIC(5, 2) NOT NULL
);

CREATE INDEX idx_servico_prestador_id ON servico (prestador_id);
