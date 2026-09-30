package org.example.agendamento.adapter.in.evento;

import org.example.agendamento.adapter.out.persistence.memory.InMemoryAgendamentoRepository;
import org.example.agendamento.application.port.out.GatewayDePagamento;
import org.example.agendamento.domain.event.AgendamentoCancelado;
import org.example.agendamento.domain.model.agendamento.Agendamento;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.agendamento.ResultadoCancelamento;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.ServicoId;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.example.agendamento.domain.model.shared.Periodo;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;
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
            public org.example.agendamento.application.port.out.CobrancaPix gerarCobrancaPix(
                    AgendamentoId agendamentoId, org.example.agendamento.domain.model.cliente.Cliente cliente, Dinheiro valor) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void estornar(String referenciaExterna, Dinheiro valor) {
                estornosChamados.put(referenciaExterna, valor);
            }
        };

        listener = new EstornoEventListener(gatewayDePagamento, agendamentoRepository);
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
