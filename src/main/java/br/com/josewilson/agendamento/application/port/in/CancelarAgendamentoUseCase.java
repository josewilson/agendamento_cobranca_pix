package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.agendamento.ResultadoCancelamento;

public interface CancelarAgendamentoUseCase {
    ResultadoCancelamento executar(CancelarAgendamentoCommand command);
}
