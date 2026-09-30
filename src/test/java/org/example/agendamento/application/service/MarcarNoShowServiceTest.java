package org.example.agendamento.application.service;

import org.example.agendamento.adapter.out.persistence.memory.InMemoryAgendamentoRepository;
import org.example.agendamento.adapter.out.persistence.memory.InMemoryClienteRepository;
import org.example.agendamento.application.exception.RecursoNaoEncontradoException;
import org.example.agendamento.application.port.in.MarcarNoShowCommand;
import org.example.agendamento.domain.model.agendamento.Agendamento;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.agendamento.StatusAgendamento;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.ServicoId;
import org.example.agendamento.domain.model.shared.Contato;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.example.agendamento.domain.model.shared.Periodo;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarcarNoShowServiceTest {

    private static final Instant AGORA = Instant.parse("2026-09-30T12:00:00Z");

    private InMemoryAgendamentoRepository agendamentoRepository;
    private InMemoryClienteRepository clienteRepository;
    private MarcarNoShowService service;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        agendamentoRepository = new InMemoryAgendamentoRepository();
        clienteRepository = new InMemoryClienteRepository();
        ClockFixo clock = new ClockFixo(AGORA);
        service = new MarcarNoShowService(agendamentoRepository, clienteRepository, clock);

        cliente = new Cliente(ClienteId.novo(), "Maria Silva",
                new Contato("maria@exemplo.com", "11987654321"),
                DocumentoFiscal.cpf("111.444.777-35"));
        clienteRepository.salvar(cliente);
    }

    private Agendamento agendamentoJaIniciadoDoCliente() {
        Periodo periodo = new Periodo(AGORA.minus(Duration.ofHours(1)), AGORA.minus(Duration.ofMinutes(30)));
        Agendamento agendamento = Agendamento.criar(AgendamentoId.novo(), PrestadorId.novo(), cliente.id(),
                ServicoId.novo(), periodo, Dinheiro.de("100.00"), Dinheiro.ZERO,
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100)), AGORA.minus(Duration.ofDays(1)));
        agendamentoRepository.salvar(agendamento);
        return agendamento;
    }

    @Test
    void deveMarcarNoShowEIncrementarContadorDoCliente() {
        Agendamento agendamento = agendamentoJaIniciadoDoCliente();

        service.executar(new MarcarNoShowCommand(agendamento.id()));

        assertThat(agendamentoRepository.buscarPorId(agendamento.id()).orElseThrow().status())
                .isEqualTo(StatusAgendamento.NO_SHOW);
        assertThat(clienteRepository.buscarPorId(cliente.id()).orElseThrow().quantidadeNoShow())
                .isEqualTo(1);
    }

    @Test
    void deveLancarExcecaoQuandoAgendamentoNaoEncontrado() {
        assertThatThrownBy(() -> service.executar(new MarcarNoShowCommand(AgendamentoId.novo())))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecaoQuandoClienteDoAgendamentoNaoEncontrado() {
        Periodo periodo = new Periodo(AGORA.minus(Duration.ofHours(1)), AGORA.minus(Duration.ofMinutes(30)));
        Agendamento agendamento = Agendamento.criar(AgendamentoId.novo(), PrestadorId.novo(), ClienteId.novo(),
                ServicoId.novo(), periodo, Dinheiro.de("100.00"), Dinheiro.ZERO,
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100)), AGORA.minus(Duration.ofDays(1)));
        agendamentoRepository.salvar(agendamento);

        assertThatThrownBy(() -> service.executar(new MarcarNoShowCommand(agendamento.id())))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
