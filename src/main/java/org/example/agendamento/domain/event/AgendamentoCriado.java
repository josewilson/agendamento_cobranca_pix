package org.example.agendamento.domain.event;

import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.shared.Periodo;

import java.time.Instant;

public record AgendamentoCriado(AgendamentoId agendamentoId, PrestadorId prestadorId, ClienteId clienteId,
                                 Periodo periodo, Instant ocorridoEm) implements EventoDeDominio {
}
