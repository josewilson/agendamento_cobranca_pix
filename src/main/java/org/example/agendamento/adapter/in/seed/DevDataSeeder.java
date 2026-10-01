package org.example.agendamento.adapter.in.seed;

import org.example.agendamento.application.port.out.ClienteRepository;
import org.example.agendamento.application.port.out.PrestadorRepository;
import org.example.agendamento.application.port.out.ServicoRepository;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.prestador.Prestador;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.Servico;
import org.example.agendamento.domain.model.servico.ServicoId;
import org.example.agendamento.domain.model.shared.Contato;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Popula os repositorios em memoria do perfil dev com um prestador, um cliente e um
 * servico de IDs fixos, so para dar um atalho rapido de dados prontos (Swagger UI, testes)
 * sem precisar passar pelos endpoints de cadastro (POST /api/prestadores, /api/clientes,
 * /api/servicos) toda vez. Esses endpoints existem e sao o caminho real de cadastro — este
 * seeder e so conveniencia para o perfil dev, nunca a unica forma de ter dados.
 */
@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    /** Hash bcrypt publico e amplamente usado em exemplos (nao corresponde a nenhuma senha real) — o
     * prestador do perfil dev nunca e usado para login de verdade (ver CLAUDE.md: dev e so para teste). */
    private static final String SENHA_HASH_PLACEHOLDER = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    public static final PrestadorId PRESTADOR_ID = PrestadorId.de("00000000-0000-0000-0000-000000000001");
    public static final ClienteId CLIENTE_ID = ClienteId.de("00000000-0000-0000-0000-000000000002");
    public static final ServicoId SERVICO_ID = ServicoId.de("00000000-0000-0000-0000-000000000003");

    private final PrestadorRepository prestadorRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;

    public DevDataSeeder(PrestadorRepository prestadorRepository, ClienteRepository clienteRepository,
                          ServicoRepository servicoRepository) {
        this.prestadorRepository = prestadorRepository;
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
    }

    @Override
    public void run(String... args) {
        prestadorRepository.salvar(new Prestador(PRESTADOR_ID, "Clinica Bem-Estar", "11987654321",
                "clinica@exemplo.com", SENHA_HASH_PLACEHOLDER,
                DocumentoFiscal.cnpj("11444777000161"), PoliticaCancelamento.padrao()));

        clienteRepository.salvar(new Cliente(CLIENTE_ID, "Maria Silva",
                new Contato("maria.silva@example.com", "11987654321"), DocumentoFiscal.cpf("52998224725")));

        servicoRepository.salvar(new Servico(SERVICO_ID, PRESTADOR_ID, "Massagem relaxante",
                Duration.ofMinutes(60), Dinheiro.de(BigDecimal.valueOf(150)), BigDecimal.valueOf(30)));

        log.info("Dados de demonstracao carregados (perfil dev) — prestadorId={}, clienteId={}, servicoId={}",
                PRESTADOR_ID.valor(), CLIENTE_ID.valor(), SERVICO_ID.valor());
    }
}
