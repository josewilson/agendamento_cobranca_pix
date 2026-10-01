package org.example.agendamento.application.service;

import org.example.agendamento.adapter.out.evento.PublicadorDeEventosEmMemoria;
import org.example.agendamento.adapter.out.pagamento.memory.GatewayDePagamentoFake;
import org.example.agendamento.adapter.out.persistence.memory.InMemoryAgendamentoRepository;
import org.example.agendamento.adapter.out.persistence.memory.InMemoryClienteRepository;
import org.example.agendamento.adapter.out.persistence.memory.InMemoryPrestadorRepository;
import org.example.agendamento.adapter.out.persistence.memory.InMemoryServicoRepository;
import org.example.agendamento.application.exception.RecursoNaoEncontradoException;
import org.example.agendamento.application.port.in.CriarAgendamentoCommand;
import org.example.agendamento.application.port.in.ResultadoCriacaoAgendamento;
import org.example.agendamento.domain.event.AgendamentoCriado;
import org.example.agendamento.domain.exception.ConflitoDeHorarioException;
import org.example.agendamento.domain.model.agendamento.StatusAgendamento;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.prestador.Prestador;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.Servico;
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

class CriarAgendamentoServiceTest {

    private static final Instant AGORA = Instant.parse("2026-09-30T12:00:00Z");

    private InMemoryAgendamentoRepository agendamentoRepository;
    private InMemoryClienteRepository clienteRepository;
    private PublicadorDeEventosEmMemoria publicadorDeEventos;
    private CriarAgendamentoService service;

    private Prestador prestador;
    private Cliente cliente;
    private Servico servicoComSinal;
    private Servico servicoSemSinal;

    @BeforeEach
    void setUp() {
        agendamentoRepository = new InMemoryAgendamentoRepository();
        clienteRepository = new InMemoryClienteRepository();
        InMemoryPrestadorRepository prestadorRepository = new InMemoryPrestadorRepository();
        InMemoryServicoRepository servicoRepository = new InMemoryServicoRepository();
        GatewayDePagamentoFake gatewayDePagamento = new GatewayDePagamentoFake();
        publicadorDeEventos = new PublicadorDeEventosEmMemoria();
        ClockFixo clock = new ClockFixo(AGORA);
        service = new CriarAgendamentoService(agendamentoRepository, clienteRepository, prestadorRepository,
                servicoRepository, gatewayDePagamento, publicadorDeEventos, clock);

        prestador = new Prestador(PrestadorId.novo(), "Clinica Bem Estar", "11987654321",
                DocumentoFiscal.cnpj("11.222.333/0001-81"),
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100)));
        prestadorRepository.salvar(prestador);

        cliente = new Cliente(ClienteId.novo(), "Maria Silva",
                new Contato("maria@exemplo.com", "11987654321"),
                DocumentoFiscal.cpf("111.444.777-35"));
        clienteRepository.salvar(cliente);

        servicoComSinal = new Servico(ServicoId.novo(), prestador.id(), "Consulta",
                Duration.ofMinutes(30), Dinheiro.de("100.00"), BigDecimal.valueOf(30));
        servicoRepository.salvar(servicoComSinal);

        servicoSemSinal = new Servico(ServicoId.novo(), prestador.id(), "Avaliacao",
                Duration.ofMinutes(30), Dinheiro.de("50.00"), BigDecimal.ZERO);
        servicoRepository.salvar(servicoSemSinal);
    }

    private Periodo periodoFuturo() {
        return new Periodo(AGORA.plus(Duration.ofDays(2)), AGORA.plus(Duration.ofDays(2)).plus(Duration.ofMinutes(30)));
    }

    @Test
    void deveCriarAgendamentoConfirmadoQuandoServicoNaoExigeSinal() {
        CriarAgendamentoCommand command = new CriarAgendamentoCommand(prestador.id(), cliente.id(),
                servicoSemSinal.id(), periodoFuturo());

        ResultadoCriacaoAgendamento resultado = service.executar(command);

        assertThat(resultado.agendamento().status()).isEqualTo(StatusAgendamento.CONFIRMADO);
        assertThat(resultado.cobranca()).isEmpty();
    }

    @Test
    void deveCriarAgendamentoPendenteEGerarCobrancaQuandoServicoExigeSinal() {
        CriarAgendamentoCommand command = new CriarAgendamentoCommand(prestador.id(), cliente.id(),
                servicoComSinal.id(), periodoFuturo());

        ResultadoCriacaoAgendamento resultado = service.executar(command);

        assertThat(resultado.agendamento().status()).isEqualTo(StatusAgendamento.PENDENTE_PAGAMENTO);
        assertThat(resultado.agendamento().valorSinal()).isEqualTo(Dinheiro.de("30.00"));
        assertThat(resultado.cobranca()).isPresent();
        assertThat(resultado.agendamento().referenciaPagamento())
                .contains(resultado.cobranca().orElseThrow().referenciaExterna());
    }

    @Test
    void deveExigirSinalQuandoClienteTemHistoricoDeNoShow() {
        cliente.registrarNoShow();
        cliente.registrarNoShow();
        clienteRepository.salvar(cliente);

        CriarAgendamentoCommand command = new CriarAgendamentoCommand(prestador.id(), cliente.id(),
                servicoSemSinal.id(), periodoFuturo());

        ResultadoCriacaoAgendamento resultado = service.executar(command);

        assertThat(resultado.agendamento().status()).isEqualTo(StatusAgendamento.PENDENTE_PAGAMENTO);
        assertThat(resultado.cobranca()).isPresent();
    }

    @Test
    void deveLancarExcecaoQuandoPrestadorNaoEncontrado() {
        CriarAgendamentoCommand command = new CriarAgendamentoCommand(PrestadorId.novo(), cliente.id(),
                servicoSemSinal.id(), periodoFuturo());

        assertThatThrownBy(() -> service.executar(command))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoEncontrado() {
        CriarAgendamentoCommand command = new CriarAgendamentoCommand(prestador.id(), ClienteId.novo(),
                servicoSemSinal.id(), periodoFuturo());

        assertThatThrownBy(() -> service.executar(command))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecaoQuandoServicoNaoEncontrado() {
        CriarAgendamentoCommand command = new CriarAgendamentoCommand(prestador.id(), cliente.id(),
                ServicoId.novo(), periodoFuturo());

        assertThatThrownBy(() -> service.executar(command))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecaoQuandoHorarioConflitaComAgendamentoExistente() {
        Periodo periodo = periodoFuturo();
        service.executar(new CriarAgendamentoCommand(prestador.id(), cliente.id(), servicoSemSinal.id(), periodo));

        CriarAgendamentoCommand comandoConflitante = new CriarAgendamentoCommand(prestador.id(), cliente.id(),
                servicoSemSinal.id(), periodo);

        assertThatThrownBy(() -> service.executar(comandoConflitante))
                .isInstanceOf(ConflitoDeHorarioException.class);
    }

    @Test
    void devePublicarEventoAgendamentoCriado() {
        service.executar(new CriarAgendamentoCommand(prestador.id(), cliente.id(), servicoSemSinal.id(), periodoFuturo()));

        assertThat(publicadorDeEventos.eventosPublicados())
                .hasSize(1)
                .first()
                .isInstanceOf(AgendamentoCriado.class);
    }
}
