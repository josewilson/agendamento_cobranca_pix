package org.example.agendamento.domain.event;

import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.agendamento.ResultadoCancelamento;
import org.example.agendamento.domain.model.cliente.ClienteId;

import java.time.Instant;

public record AgendamentoCancelado(AgendamentoId agendamentoId, ClienteId clienteId, ResultadoCancelamento resultado,
                                    Instant ocorridoEm) implements EventoDeDominio {
}
