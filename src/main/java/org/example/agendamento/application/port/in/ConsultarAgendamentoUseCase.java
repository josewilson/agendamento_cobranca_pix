package org.example.agendamento.application.port.in;

import org.example.agendamento.domain.model.agendamento.Agendamento;

public interface ConsultarAgendamentoUseCase {
    Agendamento executar(ConsultarAgendamentoQuery query);
}
