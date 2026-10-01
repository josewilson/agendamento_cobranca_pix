package org.example.agendamento.application.service;

import org.example.agendamento.adapter.out.persistence.memory.InMemoryAgendamentoRepository;
import org.example.agendamento.application.port.in.ListarAgendamentosPorPrestadorQuery;
import org.example.agendamento.domain.model.agendamento.Agendamento;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
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

class ListarAgendamentosPorPrestadorServiceTest {

    private final InMemoryAgendamentoRepository agendamentoRepository = new InMemoryAgendamentoRepository();
    private final ListarAgendamentosPorPrestadorService service = new ListarAgendamentosPorPrestadorService(agendamentoRepository);

    private static final Instant AGORA = Instant.parse("2026-09-30T12:00:00Z");
    private static final PoliticaCancelamento POLITICA =
            new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100));

    private Agendamento novoAgendamento(PrestadorId prestadorId, Instant inicio) {
        Periodo periodo = new Periodo(inicio, inicio.plus(Duration.ofMinutes(30)));
        return Agendamento.criar(AgendamentoId.novo(), prestadorId, ClienteId.novo(), ServicoId.novo(),
                periodo, Dinheiro.de("100.00"), Dinheiro.ZERO, POLITICA, AGORA);
    }

    @Test
    void deveListarSoOsAgendamentosDoPrestadorPedidoEmOrdemCronologica() {
        PrestadorId prestadorA = PrestadorId.novo();
        PrestadorId prestadorB = PrestadorId.novo();

        Agendamento maisTarde = novoAgendamento(prestadorA, AGORA.plus(Duration.ofDays(3)));
        Agendamento maisCedo = novoAgendamento(prestadorA, AGORA.plus(Duration.ofDays(1)));
        Agendamento deOutroPrestador = novoAgendamento(prestadorB, AGORA.plus(Duration.ofDays(2)));

        agendamentoRepository.salvar(maisTarde);
        agendamentoRepository.salvar(maisCedo);
        agendamentoRepository.salvar(deOutroPrestador);

        assertThat(service.executar(new ListarAgendamentosPorPrestadorQuery(prestadorA)))
                .containsExactly(maisCedo, maisTarde);
    }

    @Test
    void deveRetornarListaVaziaQuandoPrestadorNaoTemAgendamentos() {
        assertThat(service.executar(new ListarAgendamentosPorPrestadorQuery(PrestadorId.novo()))).isEmpty();
    }
}
