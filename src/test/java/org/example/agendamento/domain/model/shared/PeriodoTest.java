package org.example.agendamento.domain.model.shared;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeriodoTest {

    private static final Instant BASE = Instant.parse("2026-09-30T10:00:00Z");

    @Test
    void deveCriarPeriodoValido() {
        Periodo periodo = new Periodo(BASE, BASE.plus(Duration.ofHours(1)));

        assertThat(periodo.inicio()).isEqualTo(BASE);
        assertThat(periodo.fim()).isEqualTo(BASE.plus(Duration.ofHours(1)));
    }

    @Test
    void deveLancarExcecaoQuandoFimIgualAoInicio() {
        assertThatThrownBy(() -> new Periodo(BASE, BASE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveLancarExcecaoQuandoFimAnteriorAoInicio() {
        assertThatThrownBy(() -> new Periodo(BASE, BASE.minusSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveLancarExcecaoQuandoInicioOuFimNulo() {
        assertThatThrownBy(() -> new Periodo(null, BASE)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Periodo(BASE, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void deveDetectarSobreposicaoQuandoPeriodosSeCruzam() {
        Periodo primeiro = new Periodo(BASE, BASE.plus(Duration.ofHours(2)));
        Periodo segundo = new Periodo(BASE.plus(Duration.ofHours(1)), BASE.plus(Duration.ofHours(3)));

        assertThat(primeiro.sobrepoe(segundo)).isTrue();
        assertThat(segundo.sobrepoe(primeiro)).isTrue();
    }

    @Test
    void deveDetectarSobreposicaoQuandoUmPeriodoContemOutro() {
        Periodo maior = new Periodo(BASE, BASE.plus(Duration.ofHours(4)));
        Periodo menor = new Periodo(BASE.plus(Duration.ofHours(1)), BASE.plus(Duration.ofHours(2)));

        assertThat(maior.sobrepoe(menor)).isTrue();
    }

    @Test
    void naoDeveDetectarSobreposicaoQuandoPeriodosApenasSeTocam() {
        Periodo primeiro = new Periodo(BASE, BASE.plus(Duration.ofHours(1)));
        Periodo segundo = new Periodo(BASE.plus(Duration.ofHours(1)), BASE.plus(Duration.ofHours(2)));

        assertThat(primeiro.sobrepoe(segundo)).isFalse();
        assertThat(segundo.sobrepoe(primeiro)).isFalse();
    }

    @Test
    void naoDeveDetectarSobreposicaoQuandoPeriodosDistintos() {
        Periodo primeiro = new Periodo(BASE, BASE.plus(Duration.ofHours(1)));
        Periodo segundo = new Periodo(BASE.plus(Duration.ofHours(2)), BASE.plus(Duration.ofHours(3)));

        assertThat(primeiro.sobrepoe(segundo)).isFalse();
    }

    @Test
    void deveCalcularDuracaoCorretamente() {
        Periodo periodo = new Periodo(BASE, BASE.plus(Duration.ofMinutes(90)));

        assertThat(periodo.duracao()).isEqualTo(Duration.ofMinutes(90));
    }

    @Test
    void deveIndicarQuandoEstaNoPassado() {
        Periodo periodo = new Periodo(BASE, BASE.plus(Duration.ofHours(1)));

        assertThat(periodo.estaNoPassado(BASE.plus(Duration.ofMinutes(30)))).isTrue();
        assertThat(periodo.estaNoPassado(BASE.minus(Duration.ofMinutes(30)))).isFalse();
    }
}
