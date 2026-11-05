package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;

public record MarcarNoShowCommand(AgendamentoId agendamentoId) {
}
