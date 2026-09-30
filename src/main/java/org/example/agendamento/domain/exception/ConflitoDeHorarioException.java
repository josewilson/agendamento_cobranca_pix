package org.example.agendamento.domain.exception;

public class ConflitoDeHorarioException extends RuntimeException {
    public ConflitoDeHorarioException(String message) {
        super(message);
    }
}
