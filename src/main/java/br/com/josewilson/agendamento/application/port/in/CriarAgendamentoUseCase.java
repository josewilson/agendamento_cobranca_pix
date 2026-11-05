package br.com.josewilson.agendamento.application.port.in;

public interface CriarAgendamentoUseCase {
    ResultadoCriacaoAgendamento executar(CriarAgendamentoCommand command);
}
