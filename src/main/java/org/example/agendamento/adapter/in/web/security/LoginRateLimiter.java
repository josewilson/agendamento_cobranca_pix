package org.example.agendamento.adapter.in.web.security;

import org.example.agendamento.application.port.out.Clock;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bloqueia tentativas de login apos falhas consecutivas, por email (nao por IP — mais simples
 * e suficiente para o escopo deste projeto; um atacante trocando de IP ainda esbarra no limite
 * por conta-alvo). Em memoria, por instancia: reinicia se a aplicacao reiniciar e nao e
 * compartilhado entre instancias num deploy com mais de uma replica — aceitavel para o escopo
 * de portfolio deste projeto (ver specs/04-roadmap.md para um backend compartilhado, ex. Redis,
 * caso isso precise escalar).
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_TENTATIVAS = 5;
    private static final Duration JANELA_BLOQUEIO = Duration.ofMinutes(5);

    private final Clock clock;
    private final ConcurrentHashMap<String, Tentativas> tentativasPorEmail = new ConcurrentHashMap<>();

    public LoginRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public boolean bloqueado(String email) {
        Tentativas tentativas = tentativasPorEmail.get(normalizar(email));
        if (tentativas == null || tentativas.bloqueadoAte() == null) {
            return false;
        }
        if (clock.agora().isAfter(tentativas.bloqueadoAte())) {
            tentativasPorEmail.remove(normalizar(email));
            return false;
        }
        return true;
    }

    public void registrarFalha(String email) {
        tentativasPorEmail.compute(normalizar(email), (chave, atual) -> {
            int novaContagem = (atual == null ? 0 : atual.contagem()) + 1;
            Instant bloqueadoAte = novaContagem >= MAX_TENTATIVAS ? clock.agora().plus(JANELA_BLOQUEIO) : null;
            return new Tentativas(novaContagem, bloqueadoAte);
        });
    }

    public void registrarSucesso(String email) {
        tentativasPorEmail.remove(normalizar(email));
    }

    private static String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private record Tentativas(int contagem, Instant bloqueadoAte) {
    }
}
