-- Um atendimento em andamento (EM_ANDAMENTO) tambem ocupa a agenda do profissional: a constraint da V13
-- so cobria PENDENTE_PAGAMENTO e CONFIRMADO, entao dava para reservar por cima de um atendimento em curso.
-- Manter em sincronia com STATUS_ATIVOS (AgendamentoRepositoryAdapter e InMemoryAgendamentoRepository).
ALTER TABLE agendamento DROP CONSTRAINT agendamento_sem_sobreposicao;
ALTER TABLE agendamento
    ADD CONSTRAINT agendamento_sem_sobreposicao
    EXCLUDE USING gist (profissional_id WITH =, tstzrange(periodo_inicio, periodo_fim) WITH &&)
    WHERE (status IN ('PENDENTE_PAGAMENTO', 'CONFIRMADO', 'EM_ANDAMENTO'));
