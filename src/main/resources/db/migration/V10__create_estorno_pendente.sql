-- Fila de estornos que falharam no gateway, reprocessada por scheduler.
-- Linha com tentativas >= limite fica aqui como rastro de estorno que exige acao manual.
CREATE TABLE estorno_pendente (
    id UUID PRIMARY KEY,
    agendamento_id UUID NOT NULL,
    referencia VARCHAR(255) NOT NULL,
    valor NUMERIC(12, 2) NOT NULL,
    tentativas INTEGER NOT NULL,
    ultimo_erro TEXT,
    criado_em TIMESTAMPTZ NOT NULL
);
