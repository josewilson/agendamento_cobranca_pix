package br.com.josewilson.agendamento.adapter.out.persistence.jpa;

import br.com.josewilson.agendamento.application.port.in.CriarAgendamentoCommand;
import br.com.josewilson.agendamento.application.port.in.CriarAgendamentoUseCase;
import br.com.josewilson.agendamento.application.port.out.AgendamentoRepository;
import br.com.josewilson.agendamento.application.port.out.ClienteRepository;
import br.com.josewilson.agendamento.application.port.out.PrestadorRepository;
import br.com.josewilson.agendamento.application.port.out.ProfissionalRepository;
import br.com.josewilson.agendamento.application.port.out.ServicoRepository;
import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;
import br.com.josewilson.agendamento.domain.model.prestador.Prestador;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.profissional.Profissional;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;
import br.com.josewilson.agendamento.domain.model.servico.Servico;
import br.com.josewilson.agendamento.domain.model.servico.ServicoId;
import br.com.josewilson.agendamento.domain.model.shared.Contato;
import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;
import br.com.josewilson.agendamento.domain.model.shared.DocumentoFiscal;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;
import br.com.josewilson.agendamento.domain.model.shared.PoliticaCancelamento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova a garantia do banco sob concorrencia: varias requisicoes simultaneas para o MESMO horario do
 * mesmo profissional resultam em exatamente um agendamento (a checagem em VerificadorDeConflito sozinha
 * tem race; a constraint de exclusao e quem fecha a porta). Sem @Transactional no teste, de proposito:
 * cada thread precisa da propria transacao, commitada de verdade.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CriarAgendamentoConcorrenteIT {

    private static final int TENTATIVAS_SIMULTANEAS = 5;

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private CriarAgendamentoUseCase criarAgendamento;
    @Autowired
    private AgendamentoRepository agendamentoRepository;
    @Autowired
    private PrestadorRepository prestadorRepository;
    @Autowired
    private ProfissionalRepository profissionalRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ServicoRepository servicoRepository;

    @Test
    void soUmPedidoSimultaneoParaOMesmoHorarioViraAgendamento() throws Exception {
        Prestador prestador = prestadorRepository.salvar(new Prestador(PrestadorId.novo(), "Clinica Concorrencia",
                "11987654321", "concorrencia@exemplo.com", "hash-fake", DocumentoFiscal.cnpj("11.222.333/0001-81"),
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100))));
        Profissional profissional = profissionalRepository.salvar(
                new Profissional(ProfissionalId.novo(), prestador.id(), "Dra. Ana"));
        Cliente cliente = clienteRepository.salvar(new Cliente(ClienteId.novo(), "Maria Silva",
                new Contato("maria@exemplo.com", "11987654321"), DocumentoFiscal.cpf("111.444.777-35")));
        // sem sinal: o agendamento nasce CONFIRMADO e nenhum gateway de pagamento entra na historia
        Servico servico = servicoRepository.salvar(new Servico(ServicoId.novo(), prestador.id(), "Consulta",
                Duration.ofMinutes(30), Dinheiro.de("100.00"), BigDecimal.ZERO));

        Instant inicio = Instant.now().plus(Duration.ofDays(2));
        var command = new CriarAgendamentoCommand(prestador.id(), cliente.id(), servico.id(),
                new Periodo(inicio, inicio.plus(Duration.ofMinutes(30))), profissional.id());

        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(TENTATIVAS_SIMULTANEAS);
        List<Future<Boolean>> resultados = new ArrayList<>();
        for (int i = 0; i < TENTATIVAS_SIMULTANEAS; i++) {
            Callable<Boolean> tentativa = () -> {
                largada.await();
                try {
                    criarAgendamento.executar(command);
                    return true;
                } catch (RuntimeException conflito) {
                    return false; // ConflitoDeHorarioException ou DataIntegrityViolationException (constraint)
                }
            };
            resultados.add(pool.submit(tentativa));
        }
        largada.countDown();

        int sucessos = 0;
        for (Future<Boolean> resultado : resultados) {
            if (resultado.get()) {
                sucessos++;
            }
        }
        pool.shutdown();

        assertThat(sucessos).isEqualTo(1);
        assertThat(agendamentoRepository.buscarAtivosPorProfissional(profissional.id())).hasSize(1);
    }
}
