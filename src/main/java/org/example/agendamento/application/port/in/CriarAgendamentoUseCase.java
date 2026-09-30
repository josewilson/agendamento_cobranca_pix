package org.example.agendamento.application.port.in;

public interface CriarAgendamentoUseCase {
    ResultadoCriacaoAgendamento executar(CriarAgendamentoCommand command);
}
