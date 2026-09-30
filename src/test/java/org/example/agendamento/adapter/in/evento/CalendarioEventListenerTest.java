package org.example.agendamento.adapter.in.evento;

import org.example.agendamento.adapter.out.persistence.memory.InMemoryAgendamentoRepository;
import org.example.agendamento.adapter.out.persistence.memory.InMemoryClienteRepository;
import org.example.agendamento.adapter.out.persistence.memory.InMemoryServicoRepository;
import org.example.agendamento.application.port.out.CalendarioExternoPort;
import org.example.agendamento.domain.event.AgendamentoCancelado;
import org.example.agendamento.domain.event.AgendamentoConfirmado;
import org.example.agendamento.domain.model.agendamento.Agendamento;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.agendamento.ResultadoCancelamento;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.cliente.ClienteId;
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
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CalendarioEventListenerTest {

    private InMemoryAgendamentoRepository agendamentoRepository;
    private InMemoryServicoRepository servicoRepository;
    private InMemoryClienteRepository clienteRepository;
    private Map<AgendamentoId, String> eventosSincronizados;
    private Map<AgendamentoId, Boolean> eventosRemovidos;
    private CalendarioEventListener listener;
    private Cliente cliente;
    private Servico servico;
    private Agendamento agendamento;

    @BeforeEach
    void setUp() {
        agendamentoRepository = new InMemoryAgendamentoRepository();
        servicoRepository = new InMemoryServicoRepository();
        clienteRepository = new InMemoryClienteRepository();

        cliente = new Cliente(ClienteId.novo(), "Maria Silva",
                new Contato("maria@exemplo.com", "11987654321"), DocumentoFiscal.cpf("111.444.777-35"));
        clienteRepository.salvar(cliente);

        PrestadorId prestadorId = PrestadorId.novo();
        servico = new Servico(ServicoId.novo(), prestadorId, "Massagem", Duration.ofMinutes(60),
                Dinheiro.de("150.00"), BigDecimal.valueOf(30));
        servicoRepository.salvar(servico);

        Instant agora = Instant.now();
        Periodo periodo = new Periodo(agora.plus(Duration.ofDays(1)), agora.plus(Duration.ofDays(1)).plus(Duration.ofMinutes(60)));
        agendamento = Agendamento.criar(AgendamentoId.novo(), prestadorId, cliente.id(), servico.id(), periodo,
                servico.preco(), Dinheiro.de("45.00"), PoliticaCancelamento.padrao(), agora);
        agendamentoRepository.salvar(agendamento);

        eventosSincronizados = new HashMap<>();
        eventosRemovidos = new HashMap<>();
        CalendarioExternoPort calendarioExternoPort = new CalendarioExternoPort() {
            @Override
            public void sincronizarEvento(AgendamentoId agendamentoId, String titulo, String descricao, Periodo periodo) {
                eventosSincronizados.put(agendamentoId, titulo);
            }

            @Override
            public void removerEvento(AgendamentoId agendamentoId) {
                eventosRemovidos.put(agendamentoId, true);
            }
        };

        listener = new CalendarioEventListener(calendarioExternoPort, agendamentoRepository, servicoRepository, clienteRepository);
    }

    @Test
    void deveSincronizarEventoAoConfirmarAgendamento() {
        listener.aoConfirmarAgendamento(new AgendamentoConfirmado(agendamento.id(), cliente.id(), Instant.now()));

        assertThat(eventosSincronizados).containsKey(agendamento.id());
        assertThat(eventosSincronizados.get(agendamento.id())).isEqualTo("Massagem - Maria Silva");
    }

    @Test
    void naoDeveSincronizarNemLancarExcecaoQuandoAgendamentoNaoEncontrado() {
        listener.aoConfirmarAgendamento(new AgendamentoConfirmado(AgendamentoId.novo(), cliente.id(), Instant.now()));

        assertThat(eventosSincronizados).isEmpty();
    }

    @Test
    void deveRemoverEventoAoCancelarAgendamento() {
        ResultadoCancelamento resultado = new ResultadoCancelamento(Dinheiro.ZERO, Dinheiro.de("45.00"));
        listener.aoCancelarAgendamento(new AgendamentoCancelado(agendamento.id(), cliente.id(), resultado, Instant.now()));

        assertThat(eventosRemovidos).containsKey(agendamento.id());
    }
}
