package org.example.agendamento.domain.exception;

import org.example.agendamento.domain.model.agendamento.StatusAgendamento;

public class TransicaoDeStatusInvalidaException extends RuntimeException {
    public TransicaoDeStatusInvalidaException(StatusAgendamento statusAtual, StatusAgendamento statusDesejado) {
        super("Nao e possivel transicionar de %s para %s".formatted(statusAtual, statusDesejado));
    }
}
