package org.example.agendamento.domain.model.shared;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DinheiroTest {

    @Test
    void deveNormalizarEscalaParaDuasCasas() {
        Dinheiro dinheiro = Dinheiro.de("10");

        assertThat(dinheiro.valor()).isEqualByComparingTo("10.00");
    }

    @Test
    void deveLancarExcecaoParaValorNegativo() {
        assertThatThrownBy(() -> Dinheiro.de("-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveSomarValoresCorretamente() {
        Dinheiro resultado = Dinheiro.de("10.50").somar(Dinheiro.de("5.30"));

        assertThat(resultado).isEqualTo(Dinheiro.de("15.80"));
    }

    @Test
    void deveSubtrairValoresCorretamente() {
        Dinheiro resultado = Dinheiro.de("10.50").subtrair(Dinheiro.de("5.30"));

        assertThat(resultado).isEqualTo(Dinheiro.de("5.20"));
    }

    @Test
    void deveLancarExcecaoQuandoSubtracaoResultaEmNegativo() {
        assertThatThrownBy(() -> Dinheiro.de("5.00").subtrair(Dinheiro.de("10.00")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveCalcularPercentualCorretamente() {
        Dinheiro resultado = Dinheiro.de("100.00").percentual(BigDecimal.valueOf(30));

        assertThat(resultado).isEqualTo(Dinheiro.de("30.00"));
    }

    @Test
    void deveArredondarPercentualCorretamente() {
        Dinheiro resultado = Dinheiro.de("10.00").percentual(BigDecimal.valueOf(33));

        assertThat(resultado).isEqualTo(Dinheiro.de("3.30"));
    }

    @Test
    void doisDinheirosComMesmoValorDevemSerIguais() {
        assertThat(Dinheiro.de("10")).isEqualTo(Dinheiro.de("10.00"));
    }

    @Test
    void deveCompararValoresCorretamente() {
        assertThat(Dinheiro.de("10.00").maiorQue(Dinheiro.de("5.00"))).isTrue();
        assertThat(Dinheiro.de("5.00").maiorQue(Dinheiro.de("10.00"))).isFalse();
    }
}
