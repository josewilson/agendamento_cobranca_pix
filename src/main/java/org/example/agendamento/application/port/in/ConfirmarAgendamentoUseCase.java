package org.example.agendamento.application.port.in;

import org.example.agendamento.domain.model.agendamento.Agendamento;

public interface ConfirmarAgendamentoUseCase {
    Agendamento executar(ConfirmarAgendamentoCommand command);
}
