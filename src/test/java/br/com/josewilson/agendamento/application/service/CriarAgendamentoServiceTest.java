package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.adapter.out.evento.PublicadorDeEventosEmMemoria;
import br.com.josewilson.agendamento.adapter.out.pagamento.memory.GatewayDePagamentoFake;
import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryAgendamentoRepository;
import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryClienteRepository;
import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryBloqueioRepository;
import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryProfissionalRepository;
import br.com.josewilson.agendamento.domain.model.profissional.BloqueioDeAgenda;
import br.com.josewilson.agendamento.domain.model.profissional.BloqueioId;
import br.com.josewilson.agendamento.domain.model.profissional.Profissional;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;
import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryPrestadorRepository;
import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryServicoRepository;
import br.com.josewilson.agendamento.application.exception.RecursoNaoEncontradoException;
import br.com.josewilson.agendamento.application.port.in.CriarAgendamentoCommand;
import br.com.josewilson.agendamento.application.port.in.ResultadoCriacaoAgendamento;
import br.com.josewilson.agendamento.domain.event.AgendamentoCriado;
import br.com.josewilson.agendamento.domain.exception.ConflitoDeHorarioException;
import br.com.josewilson.agendamento.domain.model.agendamento.StatusAgendamento;
import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;
import br.com.josewilson.agendamento.domain.model.prestador.Prestador;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.servico.Servico;
import br.com.josewilson.agendamento.domain.model.servico.ServicoId;
import br.com.josewilson.agendamento.domain.model.shared.Contato;
import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;
import br.com.josewilson.agendamento.domain.model.shared.DocumentoFiscal;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;
import br.com.josewilson.agendamento.domain.model.shared.PoliticaCancelamento;
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
    private Profissional profissional;
    private InMemoryProfissionalRepository profissionalRepository;
    private InMemoryBloqueioRepository bloqueioRepository;
    private Cliente cliente;
    private Servico servicoComSinal;
    private Servico servicoSemSinal;

    @BeforeEach
    void setUp() {
        agendamentoRepository = new InMemoryAgendamentoRepository();
        clienteRepository = new InMemoryClienteRepository();
        InMemoryPrestadorRepository prestadorRepository = new InMemoryPrestadorRepository();
        InMemoryServicoRepository servicoRepository = new InMemoryServicoRepository();
        profissionalRepository = new InMemoryProfissionalRepository();
        bloqueioRepository = new InMemoryBloqueioRepository();
        GatewayDePagamentoFake gatewayDePagamento = new GatewayDePagamentoFake();
        publicadorDeEventos = new PublicadorDeEventosEmMemoria();
        ClockFixo clock = new ClockFixo(AGORA);
        service = new CriarAgendamentoService(agendamentoRepository, clienteRepository, prestadorRepository,
                profissionalRepository, bloqueioRepository, servicoRepository, gatewayDePagamento, publicadorDeEventos, clock);

        prestador = new Prestador(PrestadorId.novo(), "Clinica Bem Estar", "11987654321",
                "clinica@exemplo.com", "hash-fake-de-teste", DocumentoFiscal.cnpj("11.222.333/0001-81"),
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100)));
        prestadorRepository.salvar(prestador);
        profissional = profissionalRepository.salvar(new Profissional(ProfissionalId.novo(), prestador.id(), "Dra. Ana"));

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
    private CriarAgendamentoCommand comando(ProfissionalId profissionalId, Periodo periodo) {
        return new CriarAgendamentoCommand(prestador.id(), cliente.id(), servicoSemSinal.id(), periodo, profissionalId);
    }

    private Periodo amanha(int horaUtc) {
        Instant inicio = AGORA.plus(Duration.ofDays(1)).truncatedTo(java.time.temporal.ChronoUnit.DAYS).plus(Duration.ofHours(horaUtc));
        return new Periodo(inicio, inicio.plus(Duration.ofMinutes(30)));
    }

    @Test
    void usaOUnicoProfissionalQuandoNaoInformado() {
        var resultado = service.executar(comando(null, amanha(10)));

        assertThat(resultado.agendamento().profissionalId()).isEqualTo(profissional.id());
    }

    @Test
    void exigeInformarProfissionalQuandoHaMaisDeUm() {
        profissionalRepository.salvar(new Profissional(ProfissionalId.novo(), prestador.id(), "Dr. Beto"));

        assertThatThrownBy(() -> service.executar(comando(null, amanha(10))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Informe o profissional");
    }

    @Test
    void profissionaisDiferentesPodemAtenderNoMesmoHorario() {
        Profissional outro = profissionalRepository.salvar(new Profissional(ProfissionalId.novo(), prestador.id(), "Dr. Beto"));

        service.executar(comando(profissional.id(), amanha(10)));
        var resultado = service.executar(comando(outro.id(), amanha(10)));

        assertThat(resultado.agendamento().profissionalId()).isEqualTo(outro.id());
    }

    @Test
    void mesmoProfissionalNoMesmoHorarioConflita() {
        service.executar(comando(profissional.id(), amanha(10)));

        assertThatThrownBy(() -> service.executar(comando(profissional.id(), amanha(10))))
                .isInstanceOf(br.com.josewilson.agendamento.domain.exception.ConflitoDeHorarioException.class);
    }

    @Test
    void horarioBloqueadoDoProfissionalConflita() {
        bloqueioRepository.salvar(new BloqueioDeAgenda(BloqueioId.novo(), profissional.id(), amanha(12), "Almoco"));

        assertThatThrownBy(() -> service.executar(comando(profissional.id(), amanha(12))))
                .isInstanceOf(br.com.josewilson.agendamento.domain.exception.ConflitoDeHorarioException.class)
                .hasMessageContaining("bloqueado");
    }

    @Test
    void recusaProfissionalDeOutroPrestador() {
        Profissional alheio = profissionalRepository.salvar(new Profissional(ProfissionalId.novo(), PrestadorId.novo(), "Alheio"));

        assertThatThrownBy(() -> service.executar(comando(alheio.id(), amanha(10))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("nao pertence");
    }
}
