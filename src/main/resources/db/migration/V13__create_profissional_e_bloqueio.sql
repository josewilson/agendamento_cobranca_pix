-- Estabelecimento (a tabela prestador, que e quem tem login) passa a ter varios profissionais, cada um
-- com agenda propria; a nao-sobreposicao de agendamentos deixa de ser por prestador e passa a ser por profissional.
CREATE TABLE profissional (
    id UUID PRIMARY KEY,
    estabelecimento_id UUID NOT NULL REFERENCES prestador (id),
    nome VARCHAR(255) NOT NULL
);
CREATE INDEX idx_profissional_estabelecimento ON profissional (estabelecimento_id);

-- Todo prestador existente ganha um profissional padrao com o mesmo nome (agenda atual preservada).
INSERT INTO profissional (id, estabelecimento_id, nome) SELECT gen_random_uuid(), id, nome FROM prestador;

ALTER TABLE agendamento ADD COLUMN profissional_id UUID REFERENCES profissional (id);
UPDATE agendamento a SET profissional_id = p.id FROM profissional p WHERE p.estabelecimento_id = a.prestador_id;
ALTER TABLE agendamento ALTER COLUMN profissional_id SET NOT NULL;
CREATE INDEX idx_agendamento_profissional_status ON agendamento (profissional_id, status);

ALTER TABLE agendamento DROP CONSTRAINT agendamento_sem_sobreposicao;
ALTER TABLE agendamento
    ADD CONSTRAINT agendamento_sem_sobreposicao
    EXCLUDE USING gist (profissional_id WITH =, tstzrange(periodo_inicio, periodo_fim) WITH &&)
    WHERE (status IN ('PENDENTE_PAGAMENTO', 'CONFIRMADO'));

-- Horario bloqueado: ocupa a agenda do profissional sem ter cliente/agendamento por tras.
-- A exclusao aqui so cobre bloqueio x bloqueio; bloqueio x agendamento e checado na aplicacao.
CREATE TABLE bloqueio_agenda (
    id UUID PRIMARY KEY,
    profissional_id UUID NOT NULL REFERENCES profissional (id) ON DELETE CASCADE,
    periodo_inicio TIMESTAMPTZ NOT NULL,
    periodo_fim TIMESTAMPTZ NOT NULL,
    motivo VARCHAR(255),
    CONSTRAINT bloqueio_sem_sobreposicao
        EXCLUDE USING gist (profissional_id WITH =, tstzrange(periodo_inicio, periodo_fim) WITH &&)
);
