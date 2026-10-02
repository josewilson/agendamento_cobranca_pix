package org.example.agendamento.application.exception;

/**
 * Lancada quando um prestador autenticado tenta editar/excluir um recurso (servico, perfil) que
 * pertence a outro prestador. Mapeada para 403 pelo GlobalExceptionHandler.
 */
public class AcessoNaoAutorizadoException extends RuntimeException {
    public AcessoNaoAutorizadoException(String message) {
        super(message);
    }
}
