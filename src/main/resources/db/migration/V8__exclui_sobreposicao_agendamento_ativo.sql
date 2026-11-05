-- Garante no banco que dois agendamentos ativos do mesmo prestador nunca se sobrepoem,
-- mesmo com duas requisicoes concorrentes (a checagem em VerificadorDeConflito sozinha tem race).
-- tstzrange e [) por padrao: um termina exatamente quando o outro comeca nao e conflito (igual Periodo.sobrepoe).
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE agendamento
    ADD CONSTRAINT agendamento_sem_sobreposicao
    EXCLUDE USING gist (prestador_id WITH =, tstzrange(periodo_inicio, periodo_fim) WITH &&)
    WHERE (status IN ('PENDENTE_PAGAMENTO', 'CONFIRMADO'));
