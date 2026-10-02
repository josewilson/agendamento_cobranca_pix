package org.example.agendamento.adapter.in.web.security;

import org.example.agendamento.application.port.out.Clock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimiterTest {

    private final RelogioFixo relogio = new RelogioFixo(Instant.parse("2026-01-01T10:00:00Z"));
    private final LoginRateLimiter rateLimiter = new LoginRateLimiter(relogio);

    @Test
    void naoBloqueiaAntesDoLimiteDeTentativas() {
        for (int i = 0; i < 4; i++) {
            rateLimiter.registrarFalha("prestador@exemplo.com");
        }

        assertThat(rateLimiter.bloqueado("prestador@exemplo.com")).isFalse();
    }

    @Test
    void bloqueiaAposCincoFalhasConsecutivas() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.registrarFalha("prestador@exemplo.com");
        }

        assertThat(rateLimiter.bloqueado("prestador@exemplo.com")).isTrue();
    }

    @Test
    void naoBloqueiaEmailDiferente() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.registrarFalha("prestador@exemplo.com");
        }

        assertThat(rateLimiter.bloqueado("outro@exemplo.com")).isFalse();
    }

    @Test
    void sucessoLimpaOContadorDeFalhas() {
        for (int i = 0; i < 4; i++) {
            rateLimiter.registrarFalha("prestador@exemplo.com");
        }
        rateLimiter.registrarSucesso("prestador@exemplo.com");
        rateLimiter.registrarFalha("prestador@exemplo.com");

        assertThat(rateLimiter.bloqueado("prestador@exemplo.com")).isFalse();
    }

    @Test
    void desbloqueiaDepoisQueAJanelaDeBloqueioPassa() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.registrarFalha("prestador@exemplo.com");
        }
        assertThat(rateLimiter.bloqueado("prestador@exemplo.com")).isTrue();

        relogio.avancar(Duration.ofMinutes(6));

        assertThat(rateLimiter.bloqueado("prestador@exemplo.com")).isFalse();
    }

    @Test
    void comparacaoDeEmailIgnoraCaixaEEspacos() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.registrarFalha("Prestador@Exemplo.com ");
        }

        assertThat(rateLimiter.bloqueado(" prestador@exemplo.com")).isTrue();
    }

    private static final class RelogioFixo implements Clock {
        private Instant agora;

        private RelogioFixo(Instant agora) {
            this.agora = agora;
        }

        @Override
        public Instant agora() {
            return agora;
        }

        void avancar(Duration duracao) {
            agora = agora.plus(duracao);
        }
    }
}
