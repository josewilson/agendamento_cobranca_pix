package org.example.agendamento.domain.model.agendamento;

import org.example.agendamento.domain.exception.TransicaoDeStatusInvalidaException;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.ServicoId;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.example.agendamento.domain.model.shared.Periodo;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.agendamento.domain.model.agendamento.AgendamentoTestDataBuilder.umAgendamento;

class AgendamentoTest {

    @Test
    void deveCriarComoConfirmadoQuandoNaoExigeSinal() {
        Agendamento agendamento = umAgendamento().semSinal().build();

        assertThat(agendamento.status()).isEqualTo(StatusAgendamento.CONFIRMADO);
    }

    @Test
    void deveCriarComoPendentePagamentoQuandoExigeSinal() {
        Agendamento agendamento = umAgendamento().comValorSinal(Dinheiro.de("30.00")).build();

        assertThat(agendamento.status()).isEqualTo(StatusAgendamento.PENDENTE_PAGAMENTO);
    }

    @Test
    void deveLancarExcecaoAoCriarComPeriodoNoPassado() {
        Instant agora = Instant.parse("2026-09-30T12:00:00Z");
        Periodo periodoNoPassado = new Periodo(agora.minus(Duration.ofDays(1)), agora.minus(Duration.ofHours(23)));

        assertThatThrownBy(() -> umAgendamento().criadoEm(agora).comPeriodo(periodoNoPassado).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveConfirmarAgendamentoPendente() {
        Agendamento agendamento = umAgendamento().comValorSinal(Dinheiro.de("30.00")).build();

        agendamento.confirmar();

        assertThat(agendamento.status()).isEqualTo(StatusAgendamento.CONFIRMADO);
    }

    @Test
    void deveLancarExcecaoAoConfirmarAgendamentoJaConfirmado() {
        Agendamento agendamento = umAgendamento().semSinal().build();

        assertThatThrownBy(agendamento::confirmar)
                .isInstanceOf(TransicaoDeStatusInvalidaException.class);
    }

    @Test
    void deveIniciarAtendimentoAPartirDeConfirmado() {
        Agendamento agendamento = umAgendamento().semSinal().build();

        agendamento.iniciar();

        assertThat(agendamento.status()).isEqualTo(StatusAgendamento.EM_ANDAMENTO);
    }

    @Test
    void deveConcluirAtendimentoAPartirDeEmAndamento() {
        Agendamento agendamento = umAgendamento().semSinal().build();
        agendamento.iniciar();

        agendamento.concluir();

        assertThat(agendamento.status()).isEqualTo(StatusAgendamento.CONCLUIDO);
    }

    @Test
    void deveCancelarSemRetencaoQuandoDentroDaJanelaLivre() {
        Instant agora = Instant.parse("2026-09-30T12:00:00Z");
        Periodo periodo = new Periodo(agora.plus(Duration.ofDays(5)), agora.plus(Duration.ofDays(5)).plus(Duration.ofHours(1)));
        Agendamento agendamento = umAgendamento()
                .criadoEm(agora)
                .comPeriodo(periodo)
                .comValorSinal(Dinheiro.de("30.00"))
                .build();

        ResultadoCancelamento resultado = agendamento.cancelar(agora);

        assertThat(resultado.valorRetido()).isEqualTo(Dinheiro.ZERO);
        assertThat(resultado.valorReembolsado()).isEqualTo(Dinheiro.de("30.00"));
        assertThat(agendamento.status()).isEqualTo(StatusAgendamento.CANCELADO);
    }

    @Test
    void deveCancelarComRetencaoTotalQuandoForaDaJanela() {
        Instant agora = Instant.parse("2026-09-30T12:00:00Z");
        Periodo periodo = new Periodo(agora.plus(Duration.ofHours(2)), agora.plus(Duration.ofHours(3)));
        Agendamento agendamento = umAgendamento()
                .criadoEm(agora)
                .comPeriodo(periodo)
                .comValorSinal(Dinheiro.de("30.00"))
                .comPolitica(new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100)))
                .build();

        ResultadoCancelamento resultado = agendamento.cancelar(agora);

        assertThat(resultado.valorRetido()).isEqualTo(Dinheiro.de("30.00"));
        assertThat(resultado.valorReembolsado()).isEqualTo(Dinheiro.ZERO);
    }

    @Test
    void deveCancelarComRetencaoParcialConformePolitica() {
        Instant agora = Instant.parse("2026-09-30T12:00:00Z");
        Periodo periodo = new Periodo(agora.plus(Duration.ofHours(2)), agora.plus(Duration.ofHours(3)));
        Agendamento agendamento = umAgendamento()
                .criadoEm(agora)
                .comPeriodo(periodo)
                .comValorSinal(Dinheiro.de("30.00"))
                .comPolitica(new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(50)))
                .build();

        ResultadoCancelamento resultado = agendamento.cancelar(agora);

        assertThat(resultado.valorRetido()).isEqualTo(Dinheiro.de("15.00"));
        assertThat(resultado.valorReembolsado()).isEqualTo(Dinheiro.de("15.00"));
    }

    @Test
    void deveMarcarNoShowAposHorarioDoAgendamento() {
        Instant agora = Instant.parse("2026-09-30T12:00:00Z");
        Periodo periodo = new Periodo(agora.minus(Duration.ofHours(1)), agora.minus(Duration.ofMinutes(30)));
        Agendamento agendamento = umAgendamento()
                .criadoEm(agora.minus(Duration.ofDays(1)))
                .comPeriodo(periodo)
                .semSinal()
                .build();

        agendamento.marcarNoShow(agora);

        assertThat(agendamento.status()).isEqualTo(StatusAgendamento.NO_SHOW);
    }

    @Test
    void deveLancarExcecaoAoMarcarNoShowAntesDoHorario() {
        Agendamento agendamento = umAgendamento().semSinal().build();

        assertThatThrownBy(() -> agendamento.marcarNoShow(agendamento.criadoEm()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deveExpirarQuandoPendentePagamentoEPrazoEsgotado() {
        Instant criadoEm = Instant.parse("2026-09-30T12:00:00Z");
        Agendamento agendamento = umAgendamento()
                .criadoEm(criadoEm)
                .comValorSinal(Dinheiro.de("30.00"))
                .build();

        boolean expirou = agendamento.expirarSeNecessario(criadoEm.plus(Duration.ofMinutes(16)), Duration.ofMinutes(15));

        assertThat(expirou).isTrue();
        assertThat(agendamento.status()).isEqualTo(StatusAgendamento.EXPIRADO);
    }

    @Test
    void naoDeveExpirarAntesDoPrazo() {
        Instant criadoEm = Instant.parse("2026-09-30T12:00:00Z");
        Agendamento agendamento = umAgendamento()
                .criadoEm(criadoEm)
                .comValorSinal(Dinheiro.de("30.00"))
                .build();

        boolean expirou = agendamento.expirarSeNecessario(criadoEm.plus(Duration.ofMinutes(5)), Duration.ofMinutes(15));

        assertThat(expirou).isFalse();
        assertThat(agendamento.status()).isEqualTo(StatusAgendamento.PENDENTE_PAGAMENTO);
    }

    @Test
    void naoDeveExpirarQuandoNaoEstaPendentePagamento() {
        Agendamento agendamento = umAgendamento().semSinal().build();

        boolean expirou = agendamento.expirarSeNecessario(agendamento.criadoEm().plus(Duration.ofDays(1)), Duration.ofMinutes(15));

        assertThat(expirou).isFalse();
    }

    @Test
    void deveLancarExcecaoAoCancelarAgendamentoJaConcluido() {
        Agendamento agendamento = umAgendamento().semSinal().build();
        agendamento.iniciar();
        agendamento.concluir();

        assertThatThrownBy(() -> agendamento.cancelar(agendamento.criadoEm()))
                .isInstanceOf(TransicaoDeStatusInvalidaException.class);
    }

    @Test
    void deveReconstituirAgendamentoComStatusArbitrarioSemRevalidarRegrasDeCriacao() {
        Instant criadoEmNoPassadoDistante = Instant.parse("2020-01-01T10:00:00Z");
        Periodo periodoNoPassado = new Periodo(criadoEmNoPassadoDistante.plus(Duration.ofDays(1)),
                criadoEmNoPassadoDistante.plus(Duration.ofDays(1)).plus(Duration.ofHours(1)));

        Agendamento agendamento = Agendamento.reconstituir(AgendamentoId.novo(), PrestadorId.novo(), ClienteId.novo(),
                ServicoId.novo(), periodoNoPassado, Dinheiro.de("100.00"), Dinheiro.de("30.00"),
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100)),
                criadoEmNoPassadoDistante, StatusAgendamento.CONCLUIDO);

        assertThat(agendamento.status()).isEqualTo(StatusAgendamento.CONCLUIDO);
        assertThat(agendamento.periodo()).isEqualTo(periodoNoPassado);
    }
}
