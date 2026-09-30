package org.example.agendamento.application.port.in;

import org.example.agendamento.domain.model.agendamento.AgendamentoId;

public record ConsultarAgendamentoQuery(AgendamentoId agendamentoId) {
}
