package br.com.josewilson.agendamento.adapter.in.scheduler;

import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryFilaDeEstornos;
import br.com.josewilson.agendamento.application.port.out.CobrancaPix;
import br.com.josewilson.agendamento.application.port.out.GatewayDePagamento;
import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReprocessamentoEstornoSchedulerTest {

    private static final int MAX = 3;

    private InMemoryFilaDeEstornos fila;
    private boolean gatewayFunciona;
    private final List<String> estornados = new ArrayList<>();
    private ReprocessamentoEstornoScheduler scheduler;

    @BeforeEach
    void setUp() {
        fila = new InMemoryFilaDeEstornos();
        fila.registrar(AgendamentoId.novo(), "pay_1", Dinheiro.de("30.00"), "erro", Instant.now());
        gatewayFunciona = false;
        GatewayDePagamento gateway = new GatewayDePagamento() {
            @Override
            public CobrancaPix gerarCobrancaPix(AgendamentoId id, Cliente cliente, Dinheiro valor) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void estornar(String referenciaExterna, Dinheiro valor) {
                if (!gatewayFunciona) {
                    throw new IllegalStateException("fora do ar");
                }
                estornados.add(referenciaExterna);
            }
        };
        scheduler = new ReprocessamentoEstornoScheduler(fila, gateway, MAX);
    }

    @Test
    void deveRemoverDaFilaQuandoEstornoPassa() {
        gatewayFunciona = true;
        scheduler.reprocessar();

        assertThat(estornados).containsExactly("pay_1");
        assertThat(fila.buscarPendentes(MAX)).isEmpty();
    }

    @Test
    void devePararDeTentarAoEsgotarTentativas() {
        scheduler.reprocessar(); // tentativa 2
        scheduler.reprocessar(); // tentativa 3 = esgotado
        assertThat(fila.buscarPendentes(MAX)).isEmpty();

        gatewayFunciona = true;
        scheduler.reprocessar();
        assertThat(estornados).isEmpty();
    }
}
