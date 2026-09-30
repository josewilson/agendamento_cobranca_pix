package org.example.agendamento.domain.model.agendamento;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class StatusAgendamentoTest {

    @Test
    void devePermitirTransicoesValidasAPartirDePendentePagamento() {
        assertThat(StatusAgendamento.PENDENTE_PAGAMENTO.podeTransicionarPara(StatusAgendamento.CONFIRMADO)).isTrue();
        assertThat(StatusAgendamento.PENDENTE_PAGAMENTO.podeTransicionarPara(StatusAgendamento.CANCELADO)).isTrue();
        assertThat(StatusAgendamento.PENDENTE_PAGAMENTO.podeTransicionarPara(StatusAgendamento.EXPIRADO)).isTrue();
    }

    @Test
    void deveNegarTransicaoDePendentePagamentoDiretoParaEmAndamento() {
        assertThat(StatusAgendamento.PENDENTE_PAGAMENTO.podeTransicionarPara(StatusAgendamento.EM_ANDAMENTO)).isFalse();
    }

    @Test
    void devePermitirTransicoesValidasAPartirDeConfirmado() {
        assertThat(StatusAgendamento.CONFIRMADO.podeTransicionarPara(StatusAgendamento.EM_ANDAMENTO)).isTrue();
        assertThat(StatusAgendamento.CONFIRMADO.podeTransicionarPara(StatusAgendamento.CANCELADO)).isTrue();
        assertThat(StatusAgendamento.CONFIRMADO.podeTransicionarPara(StatusAgendamento.NO_SHOW)).isTrue();
    }

    @Test
    void devePermitirTransicaoDeEmAndamentoParaConcluido() {
        assertThat(StatusAgendamento.EM_ANDAMENTO.podeTransicionarPara(StatusAgendamento.CONCLUIDO)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = StatusAgendamento.class, names = {"CONCLUIDO", "CANCELADO", "EXPIRADO", "NO_SHOW"})
    void statusFinaisNaoDevemPermitirNenhumaTransicao(StatusAgendamento status) {
        assertThat(status.isTerminal()).isTrue();
        for (StatusAgendamento possivelDestino : StatusAgendamento.values()) {
            assertThat(status.podeTransicionarPara(possivelDestino)).isFalse();
        }
    }

    @ParameterizedTest
    @EnumSource(value = StatusAgendamento.class, names = {"PENDENTE_PAGAMENTO", "CONFIRMADO", "EM_ANDAMENTO"})
    void statusNaoFinaisNaoDevemSerTerminais(StatusAgendamento status) {
        assertThat(status.isTerminal()).isFalse();
    }
}
