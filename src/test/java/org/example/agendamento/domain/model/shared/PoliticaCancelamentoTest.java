package org.example.agendamento.domain.model.shared;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PoliticaCancelamentoTest {

    private static final Instant INICIO_AGENDAMENTO = Instant.parse("2026-10-01T10:00:00Z");
    private final PoliticaCancelamento politica = new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100));

    @Test
    void deveConsiderarDentroDaJanelaLivreQuandoAntecedenciaSuficiente() {
        Instant agora = INICIO_AGENDAMENTO.minus(Duration.ofHours(48));

        assertThat(politica.dentroDaJanelaLivre(INICIO_AGENDAMENTO, agora)).isTrue();
    }

    @Test
    void deveConsiderarDentroDaJanelaLivreNoLimiteExato() {
        Instant agora = INICIO_AGENDAMENTO.minus(Duration.ofHours(24));

        assertThat(politica.dentroDaJanelaLivre(INICIO_AGENDAMENTO, agora)).isTrue();
    }

    @Test
    void deveConsiderarForaDaJanelaLivreQuandoAntecedenciaInsuficiente() {
        Instant agora = INICIO_AGENDAMENTO.minus(Duration.ofHours(23).minusMinutes(59));

        assertThat(politica.dentroDaJanelaLivre(INICIO_AGENDAMENTO, agora)).isFalse();
    }

    @Test
    void deveConsiderarForaDaJanelaQuandoAgendamentoJaComecou() {
        Instant agora = INICIO_AGENDAMENTO.plus(Duration.ofMinutes(10));

        assertThat(politica.dentroDaJanelaLivre(INICIO_AGENDAMENTO, agora)).isFalse();
    }

    @Test
    void deveLancarExcecaoParaPercentualAbaixoDeZero() {
        assertThatThrownBy(() -> new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveLancarExcecaoParaPercentualAcimaDeCem() {
        assertThatThrownBy(() -> new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(101)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveLancarExcecaoParaAntecedenciaNegativa() {
        assertThatThrownBy(() -> new PoliticaCancelamento(Duration.ofHours(-1), BigDecimal.valueOf(50)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
