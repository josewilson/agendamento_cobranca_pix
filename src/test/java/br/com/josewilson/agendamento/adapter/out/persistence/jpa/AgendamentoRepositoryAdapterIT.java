package br.com.josewilson.agendamento.adapter.out.persistence.jpa;

import br.com.josewilson.agendamento.application.port.out.AgendamentoRepository;
import br.com.josewilson.agendamento.application.port.out.ClienteRepository;
import br.com.josewilson.agendamento.application.port.out.PrestadorRepository;
import br.com.josewilson.agendamento.application.port.out.ServicoRepository;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.agendamento.StatusAgendamento;
import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;
import br.com.josewilson.agendamento.domain.model.prestador.Prestador;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;
import br.com.josewilson.agendamento.domain.model.profissional.Profissional;
import br.com.josewilson.agendamento.domain.model.servico.Servico;
import br.com.josewilson.agendamento.domain.model.servico.ServicoId;
import br.com.josewilson.agendamento.domain.model.shared.Contato;
import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;
import br.com.josewilson.agendamento.domain.model.shared.DocumentoFiscal;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;
import br.com.josewilson.agendamento.domain.model.shared.PoliticaCancelamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class AgendamentoRepositoryAdapterIT extends AbstractPersistenceIT {

    @Autowired
    private AgendamentoRepository agendamentoRepository;
    @Autowired
    private PrestadorRepository prestadorRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ServicoRepository servicoRepository;
    @Autowired
    private br.com.josewilson.agendamento.application.port.out.ProfissionalRepository profissionalRepository;

    private Prestador prestador;
    private Cliente cliente;
    private Servico servico;
    private Profissional profissional;

    @BeforeEach
    void setUp() {
        prestador = prestadorRepository.salvar(new Prestador(PrestadorId.novo(), "Clinica Bem Estar", "11987654321",
                "clinica@exemplo.com", "hash-fake-de-teste", DocumentoFiscal.cnpj("11.222.333/0001-81"),
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100))));
        cliente = clienteRepository.salvar(new Cliente(ClienteId.novo(), "Maria Silva",
                new Contato("maria@exemplo.com", "11987654321"),
                DocumentoFiscal.cpf("111.444.777-35")));
        profissional = profissionalRepository.salvar(new Profissional(ProfissionalId.novo(), prestador.id(), "Dra. Ana"));
        servico = servicoRepository.salvar(new Servico(ServicoId.novo(), prestador.id(), "Consulta",
                Duration.ofMinutes(30), Dinheiro.de("100.00"), BigDecimal.valueOf(30)));
    }

    private Agendamento novoAgendamento(Instant agora, Periodo periodo, Dinheiro valorSinal) {
        return Agendamento.criar(AgendamentoId.novo(), prestador.id(), cliente.id(), servico.id(),
                profissional.id(),
                periodo, servico.preco(), valorSinal, prestador.politicaCancelamentoPadrao(), agora);
    }

    @Test
    void deveSalvarEBuscarAgendamentoPorId() {
        Instant agora = Instant.now();
        Periodo periodo = new Periodo(agora.plus(Duration.ofDays(2)), agora.plus(Duration.ofDays(2)).plus(Duration.ofMinutes(30)));
        Agendamento agendamento = novoAgendamento(agora, periodo, Dinheiro.de("30.00"));

        agendamentoRepository.salvar(agendamento);
        Optional<Agendamento> encontrado = agendamentoRepository.buscarPorId(agendamento.id());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().status()).isEqualTo(StatusAgendamento.PENDENTE_PAGAMENTO);
        // O round-trip por Postgres "timestamp" nao trunca de forma exata pro microssegundo (o driver
        // introduz um residuo sub-microssegundo as vezes) — comparar com tolerancia em vez de igualdade exata.
        assertThat(encontrado.get().periodo().inicio()).isCloseTo(periodo.inicio(), within(1, ChronoUnit.MILLIS));
        assertThat(encontrado.get().periodo().fim()).isCloseTo(periodo.fim(), within(1, ChronoUnit.MILLIS));
        assertThat(encontrado.get().valorSinal()).isEqualTo(Dinheiro.de("30.00"));
    }

    @Test
    void deveBuscarAtivosPorProfissional() {
        Instant agora = Instant.now();
        Periodo periodo1 = new Periodo(agora.plus(Duration.ofDays(1)), agora.plus(Duration.ofDays(1)).plus(Duration.ofMinutes(30)));
        Periodo periodo2 = new Periodo(agora.plus(Duration.ofDays(2)), agora.plus(Duration.ofDays(2)).plus(Duration.ofMinutes(30)));
        Agendamento pendente = novoAgendamento(agora, periodo1, Dinheiro.de("30.00"));
        Agendamento confirmado = novoAgendamento(agora, periodo2, Dinheiro.ZERO);
        agendamentoRepository.salvar(pendente);
        agendamentoRepository.salvar(confirmado);

        List<Agendamento> ativos = agendamentoRepository.buscarAtivosPorProfissional(profissional.id());

        assertThat(ativos).extracting(Agendamento::id).containsExactlyInAnyOrder(pendente.id(), confirmado.id());
    }

    @Test
    void bancoDeveRecusarAgendamentoAtivoSobrepostoDoMesmoProfissional() {
        Instant agora = Instant.now();
        Instant inicio = agora.plus(Duration.ofDays(1));
        agendamentoRepository.salvar(novoAgendamento(agora, new Periodo(inicio, inicio.plus(Duration.ofMinutes(30))), Dinheiro.ZERO));

        Agendamento sobreposto = novoAgendamento(agora,
                new Periodo(inicio.plus(Duration.ofMinutes(10)), inicio.plus(Duration.ofMinutes(40))), Dinheiro.ZERO);
        assertThatThrownBy(() -> agendamentoRepository.salvar(sobreposto))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void bancoDevePermitirAgendamentosAdjacentesECanceladosSobrepostos() {
        Instant agora = Instant.now();
        Instant inicio = agora.plus(Duration.ofDays(1));
        Instant meio = inicio.plus(Duration.ofMinutes(30));
        agendamentoRepository.salvar(novoAgendamento(agora, new Periodo(inicio, meio), Dinheiro.ZERO));
        agendamentoRepository.salvar(novoAgendamento(agora, new Periodo(meio, meio.plus(Duration.ofMinutes(30))), Dinheiro.ZERO));

        Agendamento cancelado = novoAgendamento(agora, new Periodo(inicio, meio), Dinheiro.ZERO);
        cancelado.cancelar(agora);
        agendamentoRepository.salvar(cancelado);

        assertThat(agendamentoRepository.buscarAtivosPorProfissional(profissional.id())).hasSize(2);
    }

    @Test
    void naoDeveIncluirAgendamentoCanceladoEmAtivosPorProfissional() {
        Instant agora = Instant.now();
        Periodo periodo = new Periodo(agora.plus(Duration.ofDays(1)), agora.plus(Duration.ofDays(1)).plus(Duration.ofMinutes(30)));
        Agendamento agendamento = novoAgendamento(agora, periodo, Dinheiro.ZERO);
        agendamento.cancelar(agora);
        agendamentoRepository.salvar(agendamento);

        assertThat(agendamentoRepository.buscarAtivosPorProfissional(profissional.id())).isEmpty();
    }

    @Test
    void deveBuscarTodosPendentesPagamento() {
        Instant agora = Instant.now();
        Periodo periodo = new Periodo(agora.plus(Duration.ofDays(1)), agora.plus(Duration.ofDays(1)).plus(Duration.ofMinutes(30)));
        Agendamento pendente = novoAgendamento(agora, periodo, Dinheiro.de("30.00"));
        agendamentoRepository.salvar(pendente);

        List<Agendamento> pendentes = agendamentoRepository.buscarTodosPendentesPagamento();

        assertThat(pendentes).extracting(Agendamento::id).contains(pendente.id());
    }

    @Test
    void deveBuscarPorPrestadorEmOrdemCronologicaIncluindoCancelados() {
        Instant agora = Instant.now();
        Periodo periodoTarde = new Periodo(agora.plus(Duration.ofDays(3)), agora.plus(Duration.ofDays(3)).plus(Duration.ofMinutes(30)));
        Periodo periodoCedo = new Periodo(agora.plus(Duration.ofDays(1)), agora.plus(Duration.ofDays(1)).plus(Duration.ofMinutes(30)));
        Agendamento maisTarde = novoAgendamento(agora, periodoTarde, Dinheiro.ZERO);
        Agendamento maisCedo = novoAgendamento(agora, periodoCedo, Dinheiro.ZERO);
        maisCedo.cancelar(agora);
        agendamentoRepository.salvar(maisTarde);
        agendamentoRepository.salvar(maisCedo);

        List<Agendamento> doPrestador = agendamentoRepository.buscarPorPrestador(prestador.id());

        assertThat(doPrestador).extracting(Agendamento::id).containsExactly(maisCedo.id(), maisTarde.id());
    }
    @Test
    void doisProfissionaisDoMesmoPrestadorPodemTerAgendamentoNoMesmoHorario() {
        Profissional outro = profissionalRepository.salvar(new Profissional(ProfissionalId.novo(), prestador.id(), "Dr. Beto"));
        Instant agora = Instant.now();
        Instant inicio = agora.plus(Duration.ofDays(1));
        Periodo periodo = new Periodo(inicio, inicio.plus(Duration.ofMinutes(30)));
        agendamentoRepository.salvar(novoAgendamento(agora, periodo, Dinheiro.ZERO));

        Agendamento doOutro = Agendamento.criar(AgendamentoId.novo(), prestador.id(), cliente.id(), servico.id(),
                outro.id(), periodo, servico.preco(), Dinheiro.ZERO, prestador.politicaCancelamentoPadrao(), agora);
        agendamentoRepository.salvar(doOutro);

        assertThat(agendamentoRepository.buscarAtivosPorProfissional(outro.id())).hasSize(1);
        assertThat(agendamentoRepository.buscarAtivosPorProfissional(profissional.id())).hasSize(1);
    }
    @Test
    void atendimentoEmAndamentoTambemOcupaOHorario() {
        Instant agora = Instant.now();
        Instant inicio = agora.plus(Duration.ofDays(1));
        Agendamento emAndamento = novoAgendamento(agora, new Periodo(inicio, inicio.plus(Duration.ofMinutes(30))), Dinheiro.ZERO);
        emAndamento.iniciar();
        agendamentoRepository.salvar(emAndamento);

        assertThat(agendamentoRepository.buscarAtivosPorProfissional(profissional.id())).hasSize(1);
        Agendamento sobreposto = novoAgendamento(agora,
                new Periodo(inicio.plus(Duration.ofMinutes(10)), inicio.plus(Duration.ofMinutes(40))), Dinheiro.ZERO);
        assertThatThrownBy(() -> agendamentoRepository.salvar(sobreposto))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
