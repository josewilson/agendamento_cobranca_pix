package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryAgendamentoRepository;
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

class ExpirarReservasPendentesServiceTest {

    private static final Instant CRIADO_EM = Instant.parse("2026-09-30T12:00:00Z");

    private InMemoryAgendamentoRepository agendamentoRepository;
    private ClockFixo clock;
    private ExpirarReservasPendentesService service;

    @BeforeEach
    void setUp() {
        agendamentoRepository = new InMemoryAgendamentoRepository();
        clock = new ClockFixo(CRIADO_EM);
        service = new ExpirarReservasPendentesService(agendamentoRepository, clock);
    }

    private Agendamento criarPendente(Instant criadoEm) {
        Periodo periodo = new Periodo(criadoEm.plus(Duration.ofDays(2)), criadoEm.plus(Duration.ofDays(2)).plus(Duration.ofMinutes(30)));
        Agendamento agendamento = Agendamento.criar(AgendamentoId.novo(), PrestadorId.novo(), ClienteId.novo(),
                ServicoId.novo(), ProfissionalId.novo(), periodo, Dinheiro.de("100.00"), Dinheiro.de("30.00"),
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100)), criadoEm);
        agendamentoRepository.salvar(agendamento);
        return agendamento;
    }

    private Agendamento criarConfirmado(Instant criadoEm) {
        Periodo periodo = new Periodo(criadoEm.plus(Duration.ofDays(2)), criadoEm.plus(Duration.ofDays(2)).plus(Duration.ofMinutes(30)));
        Agendamento agendamento = Agendamento.criar(AgendamentoId.novo(), PrestadorId.novo(), ClienteId.novo(),
                ServicoId.novo(), ProfissionalId.novo(), periodo, Dinheiro.de("100.00"), Dinheiro.ZERO,
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100)), criadoEm);
        agendamentoRepository.salvar(agendamento);
        return agendamento;
    }

    @Test
    void deveExpirarAgendamentosPendentesComPrazoEsgotado() {
        Agendamento pendente = criarPendente(CRIADO_EM);
        clock.avancarPara(CRIADO_EM.plus(Duration.ofMinutes(16)));

        int expirados = service.executar();

        assertThat(expirados).isEqualTo(1);
        assertThat(agendamentoRepository.buscarPorId(pendente.id()).orElseThrow().status())
                .isEqualTo(StatusAgendamento.EXPIRADO);
    }

    @Test
    void naoDeveExpirarAgendamentosDentroDoPrazo() {
        Agendamento pendente = criarPendente(CRIADO_EM);
        clock.avancarPara(CRIADO_EM.plus(Duration.ofMinutes(5)));

        int expirados = service.executar();

        assertThat(expirados).isZero();
        assertThat(agendamentoRepository.buscarPorId(pendente.id()).orElseThrow().status())
                .isEqualTo(StatusAgendamento.PENDENTE_PAGAMENTO);
    }

    @Test
    void naoDeveAfetarAgendamentosConfirmados() {
        Agendamento confirmado = criarConfirmado(CRIADO_EM);
        clock.avancarPara(CRIADO_EM.plus(Duration.ofDays(1)));

        int expirados = service.executar();

        assertThat(expirados).isZero();
        assertThat(agendamentoRepository.buscarPorId(confirmado.id()).orElseThrow().status())
                .isEqualTo(StatusAgendamento.CONFIRMADO);
    }
}
