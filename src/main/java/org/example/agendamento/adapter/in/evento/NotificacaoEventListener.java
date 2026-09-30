package org.example.agendamento.adapter.in.evento;

import org.example.agendamento.application.port.out.CanalNotificacao;
import org.example.agendamento.application.port.out.ClienteRepository;
import org.example.agendamento.application.port.out.Notificacao;
import org.example.agendamento.application.service.NotificacaoDispatcher;
import org.example.agendamento.domain.event.AgendamentoCancelado;
import org.example.agendamento.domain.event.AgendamentoConfirmado;
import org.example.agendamento.domain.event.AgendamentoCriado;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Reage aos eventos de dominio publicados via PublicadorDeEventos (ver
 * adapter/out/evento/PublicadorDeEventosSpring) disparando notificacoes ao cliente.
 * Falhas de envio sao logadas, nunca propagadas: um e-mail que falha nao pode derrubar
 * o caso de uso que o originou (criar/confirmar/cancelar agendamento ja foi persistido).
 */
@Component
public class NotificacaoEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoEventListener.class);

    private final ClienteRepository clienteRepository;
    private final NotificacaoDispatcher notificacaoDispatcher;

    public NotificacaoEventListener(ClienteRepository clienteRepository, NotificacaoDispatcher notificacaoDispatcher) {
        this.clienteRepository = clienteRepository;
        this.notificacaoDispatcher = notificacaoDispatcher;
    }

    @EventListener
    public void aoCriarAgendamento(AgendamentoCriado evento) {
        notificar(evento.clienteId(), "Agendamento criado",
                "Seu agendamento foi registrado. Se houver sinal a pagar, finalize o pagamento para confirmar.");
    }

    @EventListener
    public void aoConfirmarAgendamento(AgendamentoConfirmado evento) {
        notificar(evento.clienteId(), "Agendamento confirmado", "Seu agendamento foi confirmado!");
    }

    @EventListener
    public void aoCancelarAgendamento(AgendamentoCancelado evento) {
        notificar(evento.clienteId(), "Agendamento cancelado", "Seu agendamento foi cancelado.");
    }

    private void notificar(ClienteId clienteId, String assunto, String mensagem) {
        try {
            clienteRepository.buscarPorId(clienteId).ifPresent(cliente -> enviar(cliente, assunto, mensagem));
        } catch (RuntimeException ex) {
            log.warn("Falha ao notificar cliente {} ({}): {}", clienteId, assunto, ex.getMessage());
        }
    }

    private void enviar(Cliente cliente, String assunto, String mensagem) {
        Notificacao notificacao = new Notificacao(CanalNotificacao.EMAIL, cliente.contato().email(), assunto, mensagem);
        notificacaoDispatcher.enviar(notificacao);
    }
}
