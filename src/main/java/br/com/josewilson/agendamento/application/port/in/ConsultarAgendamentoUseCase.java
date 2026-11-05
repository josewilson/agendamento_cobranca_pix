package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;

public interface ConsultarAgendamentoUseCase {
    Agendamento executar(ConsultarAgendamentoQuery query);
}
