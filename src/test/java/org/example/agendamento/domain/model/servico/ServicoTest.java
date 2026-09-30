package org.example.agendamento.domain.model.servico;

import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicoTest {

    @Test
    void deveCriarServicoValido() {
        Servico servico = new Servico(ServicoId.novo(), PrestadorId.novo(), "Corte de cabelo",
                Duration.ofMinutes(45), Dinheiro.de("80.00"), BigDecimal.valueOf(30));

        assertThat(servico.nome()).isEqualTo("Corte de cabelo");
        assertThat(servico.duracao()).isEqualTo(Duration.ofMinutes(45));
    }

    @Test
    void deveLancarExcecaoParaNomeVazio() {
        assertThatThrownBy(() -> new Servico(ServicoId.novo(), PrestadorId.novo(), " ",
                Duration.ofMinutes(45), Dinheiro.de("80.00"), BigDecimal.valueOf(30)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveLancarExcecaoParaDuracaoZero() {
        assertThatThrownBy(() -> new Servico(ServicoId.novo(), PrestadorId.novo(), "Corte",
                Duration.ZERO, Dinheiro.de("80.00"), BigDecimal.valueOf(30)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveLancarExcecaoParaPercentualSinalForaDoIntervalo() {
        assertThatThrownBy(() -> new Servico(ServicoId.novo(), PrestadorId.novo(), "Corte",
                Duration.ofMinutes(45), Dinheiro.de("80.00"), BigDecimal.valueOf(150)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveIndicarQueExigeSinalQuandoPercentualMaiorQueZero() {
        Servico servico = new Servico(ServicoId.novo(), PrestadorId.novo(), "Corte",
                Duration.ofMinutes(45), Dinheiro.de("80.00"), BigDecimal.valueOf(30));

        assertThat(servico.exigeSinal()).isTrue();
    }

    @Test
    void naoDeveExigirSinalQuandoPercentualZero() {
        Servico servico = new Servico(ServicoId.novo(), PrestadorId.novo(), "Corte",
                Duration.ofMinutes(45), Dinheiro.de("80.00"), BigDecimal.ZERO);

        assertThat(servico.exigeSinal()).isFalse();
    }

    @Test
    void deveCalcularValorDoSinalCorretamente() {
        Servico servico = new Servico(ServicoId.novo(), PrestadorId.novo(), "Corte",
                Duration.ofMinutes(45), Dinheiro.de("80.00"), BigDecimal.valueOf(25));

        assertThat(servico.calcularSinal()).isEqualTo(Dinheiro.de("20.00"));
    }
}
