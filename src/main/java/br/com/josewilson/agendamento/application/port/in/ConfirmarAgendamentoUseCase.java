package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;

public interface ConfirmarAgendamentoUseCase {
    Agendamento executar(ConfirmarAgendamentoCommand command);
}
