package br.com.josewilson.agendamento.adapter.in.web.security;

import br.com.josewilson.agendamento.application.port.out.Clock;
import br.com.josewilson.agendamento.application.port.out.TentativasDeLogin;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Bloqueia tentativas de login apos falhas consecutivas, por email (nao por IP — mais simples
 * e suficiente: um atacante trocando de IP ainda esbarra no limite da conta-alvo). O contador
 * vive na porta {@link TentativasDeLogin} (tabela login_tentativa no Postgres), entao e
 * compartilhado entre replicas e sobrevive a reinicios.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_TENTATIVAS = 5;
    private static final Duration JANELA_BLOQUEIO = Duration.ofMinutes(5);

    private final Clock clock;
    private final TentativasDeLogin tentativas;

    public LoginRateLimiter(Clock clock, TentativasDeLogin tentativas) {
        this.clock = clock;
        this.tentativas = tentativas;
    }

    public boolean bloqueado(String email) {
        String chave = normalizar(email);
        return tentativas.bloqueadoAte(chave).map(ate -> {
            if (clock.agora().isAfter(ate)) {
                tentativas.limpar(chave);
                return false;
            }
            return true;
        }).orElse(false);
    }

    public void registrarFalha(String email) {
        tentativas.registrarFalha(normalizar(email), MAX_TENTATIVAS, clock.agora().plus(JANELA_BLOQUEIO));
    }

    public void registrarSucesso(String email) {
        tentativas.limpar(normalizar(email));
    }

    private static String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
