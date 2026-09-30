CREATE TABLE agendamento (
    id UUID PRIMARY KEY,
    prestador_id UUID NOT NULL REFERENCES prestador (id),
    cliente_id UUID NOT NULL REFERENCES cliente (id),
    servico_id UUID NOT NULL REFERENCES servico (id),
    periodo_inicio TIMESTAMPTZ NOT NULL,
    periodo_fim TIMESTAMPTZ NOT NULL,
    valor_servico NUMERIC(10, 2) NOT NULL,
    valor_sinal NUMERIC(10, 2) NOT NULL,
    politica_antecedencia_minima_segundos BIGINT NOT NULL,
    politica_percentual_retido NUMERIC(5, 2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_agendamento_prestador_status ON agendamento (prestador_id, status);
CREATE INDEX idx_agendamento_status ON agendamento (status);
