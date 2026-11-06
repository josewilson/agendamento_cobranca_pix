package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.adapter.out.evento.PublicadorDeEventosEmMemoria;
import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryAgendamentoRepository;
import br.com.josewilson.agendamento.application.port.in.WebhookPagamentoCommand;
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

class ProcessarWebhookPagamentoServiceTest {

    private static final Instant AGORA = Instant.parse("2026-09-30T12:00:00Z");

    private InMemoryAgendamentoRepository agendamentoRepository;
    private ProcessarWebhookPagamentoService service;

    @BeforeEach
    void setUp() {
        agendamentoRepository = new InMemoryAgendamentoRepository();
        PublicadorDeEventosEmMemoria publicadorDeEventos = new PublicadorDeEventosEmMemoria();
        ClockFixo clock = new ClockFixo(AGORA);
        ConfirmarAgendamentoService confirmarAgendamentoService =
                new ConfirmarAgendamentoService(agendamentoRepository, publicadorDeEventos, clock);
        service = new ProcessarWebhookPagamentoService(confirmarAgendamentoService);
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
    void deveConfirmarAgendamentoQuandoPagamentoConfirmado() {
        Agendamento agendamento = agendamentoPendente();

        service.executar(new WebhookPagamentoCommand(agendamento.id(), true));

        assertThat(agendamentoRepository.buscarPorId(agendamento.id()).orElseThrow().status())
                .isEqualTo(StatusAgendamento.CONFIRMADO);
    }

    @Test
    void naoDeveAlterarStatusQuandoPagamentoNaoConfirmado() {
        Agendamento agendamento = agendamentoPendente();

        service.executar(new WebhookPagamentoCommand(agendamento.id(), false));

        assertThat(agendamentoRepository.buscarPorId(agendamento.id()).orElseThrow().status())
                .isEqualTo(StatusAgendamento.PENDENTE_PAGAMENTO);
    }
}
