package org.example.agendamento.adapter.in.web.security;

/** Lancada por {@link LoginRateLimiter} quando um email excedeu o limite de tentativas de login. */
public class LoginBloqueadoException extends RuntimeException {

    public LoginBloqueadoException(String mensagem) {
        super(mensagem);
    }
}
