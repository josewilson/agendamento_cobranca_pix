package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.adapter.out.evento.PublicadorDeEventosEmMemoria;
import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryAgendamentoRepository;
import br.com.josewilson.agendamento.application.exception.RecursoNaoEncontradoException;
import br.com.josewilson.agendamento.application.port.in.ConfirmarAgendamentoCommand;
import br.com.josewilson.agendamento.domain.event.AgendamentoConfirmado;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.agendamento.StatusAgendamento;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;
import br.com.josewilson.agendamento.domain.model.servico.ServicoId;
import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;
import br.com.josewilson.agendamento.domain.model.shared.PoliticaCancelamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfirmarAgendamentoServiceTest {

    private static final Instant AGORA = Instant.parse("2026-09-30T12:00:00Z");

    private InMemoryAgendamentoRepository agendamentoRepository;
    private PublicadorDeEventosEmMemoria publicadorDeEventos;
    private ConfirmarAgendamentoService service;

    @BeforeEach
    void setUp() {
        agendamentoRepository = new InMemoryAgendamentoRepository();
        publicadorDeEventos = new PublicadorDeEventosEmMemoria();
        ClockFixo clock = new ClockFixo(AGORA);
        service = new ConfirmarAgendamentoService(agendamentoRepository, publicadorDeEventos, clock);
    }

    private Agendamento agendamentoPendente() {
        Periodo periodo = new Periodo(AGORA.plus(Duration.ofDays(2)), AGORA.plus(Duration.ofDays(2)).plus(Duration.ofMinutes(30)));
        Agendamento agendamento = Agendamento.criar(AgendamentoId.novo(), PrestadorId.novo(), ClienteId.novo(),
                ServicoId.novo(), ProfissionalId.novo(), periodo, Dinheiro.de("100.00"), Dinheiro.de("30.00"),
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100)), AGORA);
        agendamentoRepository.salvar(agendamento);
        return agendamento;
    }

    @Test
    void deveConfirmarAgendamentoPendente() {
        Agendamento agendamento = agendamentoPendente();

        Agendamento confirmado = service.executar(new ConfirmarAgendamentoCommand(agendamento.id()));

        assertThat(confirmado.status()).isEqualTo(StatusAgendamento.CONFIRMADO);
        assertThat(agendamentoRepository.buscarPorId(agendamento.id()).orElseThrow().status())
                .isEqualTo(StatusAgendamento.CONFIRMADO);
    }

    @Test
    void deveLancarExcecaoQuandoAgendamentoNaoEncontrado() {
        assertThatThrownBy(() -> service.executar(new ConfirmarAgendamentoCommand(AgendamentoId.novo())))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void devePublicarEventoAgendamentoConfirmado() {
        Agendamento agendamento = agendamentoPendente();

        service.executar(new ConfirmarAgendamentoCommand(agendamento.id()));

        assertThat(publicadorDeEventos.eventosPublicados())
                .hasSize(1)
                .first()
                .isInstanceOf(AgendamentoConfirmado.class);
    }
}
