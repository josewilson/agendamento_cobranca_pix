package br.com.josewilson.agendamento.domain.exception;

import br.com.josewilson.agendamento.domain.model.agendamento.StatusAgendamento;

public class TransicaoDeStatusInvalidaException extends RuntimeException {
    public TransicaoDeStatusInvalidaException(StatusAgendamento statusAtual, StatusAgendamento statusDesejado) {
        super("Nao e possivel transicionar de %s para %s".formatted(statusAtual, statusDesejado));
    }
}
