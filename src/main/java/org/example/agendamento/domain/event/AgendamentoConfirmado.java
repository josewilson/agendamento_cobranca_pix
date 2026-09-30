package org.example.agendamento.domain.event;

import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.cliente.ClienteId;

import java.time.Instant;

public record AgendamentoConfirmado(AgendamentoId agendamentoId, ClienteId clienteId,
                                     Instant ocorridoEm) implements EventoDeDominio {
}
