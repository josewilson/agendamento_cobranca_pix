package br.com.josewilson.agendamento.adapter.in.evento;

import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryAgendamentoRepository;
import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryFilaDeEstornos;
import br.com.josewilson.agendamento.application.port.out.GatewayDePagamento;
import br.com.josewilson.agendamento.domain.event.AgendamentoCancelado;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.agendamento.ResultadoCancelamento;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.servico.ServicoId;
import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;
import br.com.josewilson.agendamento.domain.model.shared.PoliticaCancelamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EstornoEventListenerTest {

    private InMemoryAgendamentoRepository agendamentoRepository;
    private Map<String, Dinheiro> estornosChamados;
    private InMemoryFilaDeEstornos fila;
    private EstornoEventListener listener;
    private Agendamento agendamentoComReferencia;
    private Agendamento agendamentoSemReferencia;

    @BeforeEach
    void setUp() {
        agendamentoRepository = new InMemoryAgendamentoRepository();

        Instant agora = Instant.now();
        Periodo periodo = new Periodo(agora.plus(Duration.ofDays(1)), agora.plus(Duration.ofDays(1)).plus(Duration.ofMinutes(30)));
        PoliticaCancelamento politica = PoliticaCancelamento.padrao();

        agendamentoComReferencia = Agendamento.criar(AgendamentoId.novo(), PrestadorId.novo(), ClienteId.novo(),
                ServicoId.novo(), periodo, Dinheiro.de("100.00"), Dinheiro.de("30.00"), politica, agora);
        agendamentoComReferencia.vincularReferenciaPagamento("pay_123");
        agendamentoRepository.salvar(agendamentoComReferencia);

        agendamentoSemReferencia = Agendamento.criar(AgendamentoId.novo(), PrestadorId.novo(), ClienteId.novo(),
                ServicoId.novo(), periodo, Dinheiro.de("100.00"), Dinheiro.ZERO, politica, agora);
        agendamentoRepository.salvar(agendamentoSemReferencia);

        estornosChamados = new HashMap<>();
        GatewayDePagamento gatewayDePagamento = new GatewayDePagamento() {
            @Override
            public br.com.josewilson.agendamento.application.port.out.CobrancaPix gerarCobrancaPix(
                    AgendamentoId agendamentoId, br.com.josewilson.agendamento.domain.model.cliente.Cliente cliente, Dinheiro valor) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void estornar(String referenciaExterna, Dinheiro valor) {
                estornosChamados.put(referenciaExterna, valor);
            }
        };

        fila = new InMemoryFilaDeEstornos();
        listener = new EstornoEventListener(gatewayDePagamento, agendamentoRepository, fila, Instant::now);
    }

    @Test
    void deveEnfileirarQuandoGatewayFalha() {
        GatewayDePagamento quebrado = new GatewayDePagamento() {
            @Override
            public br.com.josewilson.agendamento.application.port.out.CobrancaPix gerarCobrancaPix(
                    AgendamentoId agendamentoId, br.com.josewilson.agendamento.domain.model.cliente.Cliente cliente, Dinheiro valor) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void estornar(String referenciaExterna, Dinheiro valor) {
                throw new IllegalStateException("gateway fora do ar");
            }
        };
        new EstornoEventListener(quebrado, agendamentoRepository, fila, Instant::now).aoCancelarAgendamento(
                new AgendamentoCancelado(agendamentoComReferencia.id(), agendamentoComReferencia.clienteId(),
                        new ResultadoCancelamento(Dinheiro.ZERO, Dinheiro.de("30.00")), Instant.now()));

        assertThat(fila.buscarPendentes(5)).singleElement()
                .satisfies(p -> {
                    assertThat(p.referencia()).isEqualTo("pay_123");
                    assertThat(p.tentativas()).isEqualTo(1);
                });
    }

    @Test
    void deveEstornarQuandoHaValorReembolsadoEReferenciaDePagamento() {
        ResultadoCancelamento resultado = new ResultadoCancelamento(Dinheiro.ZERO, Dinheiro.de("30.00"));
        listener.aoCancelarAgendamento(new AgendamentoCancelado(agendamentoComReferencia.id(),
                agendamentoComReferencia.clienteId(), resultado, Instant.now()));

        assertThat(estornosChamados).containsEntry("pay_123", Dinheiro.de("30.00"));
    }

    @Test
    void naoDeveEstornarQuandoValorReembolsadoEZero() {
        ResultadoCancelamento resultado = new ResultadoCancelamento(Dinheiro.de("30.00"), Dinheiro.ZERO);
        listener.aoCancelarAgendamento(new AgendamentoCancelado(agendamentoComReferencia.id(),
                agendamentoComReferencia.clienteId(), resultado, Instant.now()));

        assertThat(estornosChamados).isEmpty();
    }

    @Test
    void naoDeveEstornarNemLancarExcecaoQuandoAgendamentoNaoTemReferenciaDePagamento() {
        ResultadoCancelamento resultado = new ResultadoCancelamento(Dinheiro.ZERO, Dinheiro.de("10.00"));
        listener.aoCancelarAgendamento(new AgendamentoCancelado(agendamentoSemReferencia.id(),
                agendamentoSemReferencia.clienteId(), resultado, Instant.now()));

        assertThat(estornosChamados).isEmpty();
    }

    @Test
    void naoDeveEstornarNemLancarExcecaoQuandoAgendamentoNaoEncontrado() {
        ResultadoCancelamento resultado = new ResultadoCancelamento(Dinheiro.ZERO, Dinheiro.de("10.00"));
        listener.aoCancelarAgendamento(new AgendamentoCancelado(AgendamentoId.novo(), ClienteId.novo(), resultado, Instant.now()));

        assertThat(estornosChamados).isEmpty();
    }
}
