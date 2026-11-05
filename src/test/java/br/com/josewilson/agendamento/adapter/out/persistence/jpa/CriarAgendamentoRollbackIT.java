package br.com.josewilson.agendamento.adapter.out.persistence.jpa;

import br.com.josewilson.agendamento.application.port.in.CriarAgendamentoCommand;
import br.com.josewilson.agendamento.application.port.in.CriarAgendamentoUseCase;
import br.com.josewilson.agendamento.application.port.out.AgendamentoRepository;
import br.com.josewilson.agendamento.application.port.out.ClienteRepository;
import br.com.josewilson.agendamento.application.port.out.GatewayDePagamento;
import br.com.josewilson.agendamento.application.port.out.PrestadorRepository;
import br.com.josewilson.agendamento.application.port.out.ServicoRepository;
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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Deliberadamente SEM @Transactional no teste (diferente de AbstractPersistenceIT): o que se
 * verifica aqui e justamente o rollback da transacao do proprio caso de uso.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CriarAgendamentoRollbackIT {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @MockitoBean
    private GatewayDePagamento gatewayDePagamento;
    @Autowired
    private CriarAgendamentoUseCase criarAgendamento;
    @Autowired
    private AgendamentoRepository agendamentoRepository;
    @Autowired
    private PrestadorRepository prestadorRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ServicoRepository servicoRepository;

    @Test
    void falhaDoGatewayDesfazOAgendamentoJaSalvo() {
        Prestador prestador = prestadorRepository.salvar(new Prestador(PrestadorId.novo(), "Clinica Rollback",
                "11987654321", "rollback@exemplo.com", "hash-fake", DocumentoFiscal.cnpj("11.222.333/0001-81"),
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100))));
        Cliente cliente = clienteRepository.salvar(new Cliente(ClienteId.novo(), "Maria Silva",
                new Contato("maria@exemplo.com", "11987654321"), DocumentoFiscal.cpf("111.444.777-35")));
        Servico servico = servicoRepository.salvar(new Servico(ServicoId.novo(), prestador.id(), "Consulta",
                Duration.ofMinutes(30), Dinheiro.de("100.00"), BigDecimal.valueOf(30)));

        RuntimeException falha = new RuntimeException("gateway fora do ar");
        when(gatewayDePagamento.gerarCobrancaPix(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenThrow(falha);

        Instant inicio = Instant.now().plus(Duration.ofDays(2));
        var command = new CriarAgendamentoCommand(prestador.id(), cliente.id(), servico.id(),
                new Periodo(inicio, inicio.plus(Duration.ofMinutes(30))));

        assertThatThrownBy(() -> criarAgendamento.executar(command)).isSameAs(falha);
        assertThat(agendamentoRepository.buscarPorPrestador(prestador.id())).isEmpty();
    }
}
