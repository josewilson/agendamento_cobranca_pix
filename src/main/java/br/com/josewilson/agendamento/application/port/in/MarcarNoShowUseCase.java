package br.com.josewilson.agendamento.application.port.in;

public interface MarcarNoShowUseCase {
    void executar(MarcarNoShowCommand command);
}
