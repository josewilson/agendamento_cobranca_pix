package org.example.agendamento.domain.event;

import org.example.agendamento.domain.model.agendamento.AgendamentoId;

import java.time.Instant;

public record AgendamentoConfirmado(AgendamentoId agendamentoId, Instant ocorridoEm) implements EventoDeDominio {
}
