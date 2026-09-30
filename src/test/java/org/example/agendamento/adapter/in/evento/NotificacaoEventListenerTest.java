package org.example.agendamento.adapter.in.evento;

import org.example.agendamento.adapter.out.persistence.memory.InMemoryClienteRepository;
import org.example.agendamento.application.port.out.CanalNotificacao;
import org.example.agendamento.application.port.out.EnviadorDeNotificacao;
import org.example.agendamento.application.port.out.Notificacao;
import org.example.agendamento.application.service.NotificacaoDispatcher;
import org.example.agendamento.domain.event.AgendamentoCancelado;
import org.example.agendamento.domain.event.AgendamentoConfirmado;
import org.example.agendamento.domain.event.AgendamentoCriado;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.agendamento.ResultadoCancelamento;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.shared.Contato;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.example.agendamento.domain.model.shared.Periodo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificacaoEventListenerTest {

    private InMemoryClienteRepository clienteRepository;
    private List<Notificacao> notificacoesCapturadas;
    private NotificacaoEventListener listener;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        clienteRepository = new InMemoryClienteRepository();
        cliente = new Cliente(ClienteId.novo(), "Maria Silva",
                new Contato("maria@exemplo.com", "11987654321"),
                DocumentoFiscal.cpf("111.444.777-35"));
        clienteRepository.salvar(cliente);

        notificacoesCapturadas = new ArrayList<>();
        EnviadorDeNotificacao enviadorEmailCapturador = enviadorCapturador(CanalNotificacao.EMAIL);
        EnviadorDeNotificacao enviadorWhatsAppCapturador = enviadorCapturador(CanalNotificacao.WHATSAPP);
        NotificacaoDispatcher dispatcher = new NotificacaoDispatcher(List.of(enviadorEmailCapturador, enviadorWhatsAppCapturador));
        listener = new NotificacaoEventListener(clienteRepository, dispatcher);
    }

    private EnviadorDeNotificacao enviadorCapturador(CanalNotificacao canal) {
        return new EnviadorDeNotificacao() {
            @Override
            public void enviar(Notificacao notificacao) {
                notificacoesCapturadas.add(notificacao);
            }

            @Override
            public CanalNotificacao canalSuportado() {
                return canal;
            }
        };
    }

    private Periodo periodoExemplo() {
        Instant agora = Instant.now();
        return new Periodo(agora.plus(Duration.ofDays(1)), agora.plus(Duration.ofDays(1)).plus(Duration.ofMinutes(30)));
    }

    @Test
    void deveNotificarClientePorEmailEWhatsAppAoCriarAgendamento() {
        listener.aoCriarAgendamento(new AgendamentoCriado(AgendamentoId.novo(), PrestadorId.novo(), cliente.id(),
                periodoExemplo(), Instant.now()));

        assertThat(notificacoesCapturadas).hasSize(2);
        assertThat(notificacoesCapturadas)
                .anySatisfy(n -> {
                    assertThat(n.canal()).isEqualTo(CanalNotificacao.EMAIL);
                    assertThat(n.destinatario()).isEqualTo("maria@exemplo.com");
                })
                .anySatisfy(n -> {
                    assertThat(n.canal()).isEqualTo(CanalNotificacao.WHATSAPP);
                    assertThat(n.destinatario()).isEqualTo("11987654321");
                });
    }

    @Test
    void deveNotificarClienteAoConfirmarAgendamento() {
        listener.aoConfirmarAgendamento(new AgendamentoConfirmado(AgendamentoId.novo(), cliente.id(), Instant.now()));

        assertThat(notificacoesCapturadas).hasSize(2);
        assertThat(notificacoesCapturadas).allSatisfy(n -> assertThat(n.assunto()).isEqualTo("Agendamento confirmado"));
    }

    @Test
    void deveNotificarClienteAoCancelarAgendamento() {
        ResultadoCancelamento resultado = new ResultadoCancelamento(Dinheiro.ZERO, Dinheiro.de("30.00"));
        listener.aoCancelarAgendamento(new AgendamentoCancelado(AgendamentoId.novo(), cliente.id(), resultado, Instant.now()));

        assertThat(notificacoesCapturadas).hasSize(2);
        assertThat(notificacoesCapturadas).allSatisfy(n -> assertThat(n.assunto()).isEqualTo("Agendamento cancelado"));
    }

    @Test
    void deveContinuarNoOutroCanalQuandoUmCanalFalha() {
        clienteRepository.salvar(cliente);
        NotificacaoDispatcher dispatcherComWhatsAppQuebrado = new NotificacaoDispatcher(List.of(
                enviadorCapturador(CanalNotificacao.EMAIL)));
        NotificacaoEventListener listenerComWhatsAppQuebrado =
                new NotificacaoEventListener(clienteRepository, dispatcherComWhatsAppQuebrado);

        listenerComWhatsAppQuebrado.aoCriarAgendamento(new AgendamentoCriado(AgendamentoId.novo(), PrestadorId.novo(),
                cliente.id(), periodoExemplo(), Instant.now()));

        assertThat(notificacoesCapturadas).hasSize(1);
        assertThat(notificacoesCapturadas.get(0).canal()).isEqualTo(CanalNotificacao.EMAIL);
    }

    @Test
    void naoDeveEnviarNemLancarExcecaoQuandoClienteNaoEncontrado() {
        listener.aoCriarAgendamento(new AgendamentoCriado(AgendamentoId.novo(), PrestadorId.novo(), ClienteId.novo(),
                periodoExemplo(), Instant.now()));

        assertThat(notificacoesCapturadas).isEmpty();
    }
}
