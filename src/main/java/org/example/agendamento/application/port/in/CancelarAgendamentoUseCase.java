package org.example.agendamento.application.port.in;

import org.example.agendamento.domain.model.agendamento.ResultadoCancelamento;

public interface CancelarAgendamentoUseCase {
    ResultadoCancelamento executar(CancelarAgendamentoCommand command);
}
