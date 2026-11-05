package br.com.josewilson.agendamento.adapter.out.persistence.jpa;

import br.com.josewilson.agendamento.adapter.in.web.security.LoginRateLimiter;
import br.com.josewilson.agendamento.application.port.out.Clock;
import br.com.josewilson.agendamento.application.port.out.TentativasDeLogin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TentativasDeLoginAdapterIT extends AbstractPersistenceIT {

    @Autowired
    private TentativasDeLogin tentativas;
    @Autowired
    private Clock clock;

    @Test
    void deveBloquearSoNaQuintaFalhaELimparNoSucesso() {
        LoginRateLimiter limiter = new LoginRateLimiter(clock, tentativas);
        for (int i = 0; i < 4; i++) {
            limiter.registrarFalha("A@Exemplo.com");
        }
        assertThat(limiter.bloqueado("a@exemplo.com")).isFalse();

        limiter.registrarFalha("a@exemplo.com");
        assertThat(limiter.bloqueado("a@exemplo.com")).isTrue();
        assertThat(limiter.bloqueado("outro@exemplo.com")).isFalse();

        limiter.registrarSucesso("a@exemplo.com");
        assertThat(limiter.bloqueado("a@exemplo.com")).isFalse();
    }

    @Test
    void bloqueioExpiradoELimpoNaConsulta() {
        // relogio "do futuro": o bloqueio gravado (agora+5min) ja expirou quando consultado
        Clock futuro = () -> Instant.now().plusSeconds(3600);
        tentativas.registrarFalha("b@exemplo.com", 1, Instant.now().plusSeconds(300));
        assertThat(tentativas.bloqueadoAte("b@exemplo.com")).isPresent();

        assertThat(new LoginRateLimiter(futuro, tentativas).bloqueado("b@exemplo.com")).isFalse();
        assertThat(tentativas.bloqueadoAte("b@exemplo.com")).isEmpty();
    }
}
