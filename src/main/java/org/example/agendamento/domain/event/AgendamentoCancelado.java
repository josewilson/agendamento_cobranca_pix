package org.example.agendamento.domain.event;

import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.agendamento.ResultadoCancelamento;

import java.time.Instant;

public record AgendamentoCancelado(AgendamentoId agendamentoId, ResultadoCancelamento resultado,
                                    Instant ocorridoEm) implements EventoDeDominio {
}
