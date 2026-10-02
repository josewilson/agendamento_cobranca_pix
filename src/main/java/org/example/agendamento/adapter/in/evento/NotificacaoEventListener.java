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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Reage aos eventos de dominio publicados via PublicadorDeEventos (ver
 * adapter/out/evento/PublicadorDeEventosSpring) disparando notificacoes ao cliente.
 * Envia por dois canais — EMAIL (registro formal) e WHATSAPP (canal que o cliente brasileiro
 * de fato confere para esse tipo de aviso; SMS fica disponivel mas nao e usado aqui, ver
 * specs/04-roadmap.md) — cada um isolado: a falha de um canal nunca impede o outro nem
 * propaga para o caso de uso que originou o evento (criar/confirmar/cancelar ja foi
 * persistido antes deste efeito colateral).
 * {@code @Async} (pool dedicado, ver config/AsyncConfig) despacha cada metodo numa thread
 * separada da requisicao HTTP que publicou o evento — SMTP/Twilio nao devem bloquear a
 * resposta ao usuario (achado de auditoria de performance).
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

    @Async("eventosExecutor")
    @EventListener
    public void aoCriarAgendamento(AgendamentoCriado evento) {
        notificar(evento.clienteId(), "Agendamento criado",
                "Seu agendamento foi registrado. Se houver sinal a pagar, finalize o pagamento para confirmar.");
    }

    @Async("eventosExecutor")
    @EventListener
    public void aoConfirmarAgendamento(AgendamentoConfirmado evento) {
        notificar(evento.clienteId(), "Agendamento confirmado", "Seu agendamento foi confirmado!");
    }

    @Async("eventosExecutor")
    @EventListener
    public void aoCancelarAgendamento(AgendamentoCancelado evento) {
        notificar(evento.clienteId(), "Agendamento cancelado", "Seu agendamento foi cancelado.");
    }

    private void notificar(ClienteId clienteId, String assunto, String mensagem) {
        try {
            clienteRepository.buscarPorId(clienteId).ifPresent(cliente -> enviar(cliente, assunto, mensagem));
        } catch (RuntimeException ex) {
            log.warn("Falha ao buscar cliente {} para notificar ({}): {}", clienteId, assunto, ex.getMessage());
        }
    }

    private void enviar(Cliente cliente, String assunto, String mensagem) {
        enviarPorCanal(new Notificacao(CanalNotificacao.EMAIL, cliente.contato().email(), assunto, mensagem));
        enviarPorCanal(new Notificacao(CanalNotificacao.WHATSAPP, cliente.contato().telefone(), assunto, mensagem));
    }

    private void enviarPorCanal(Notificacao notificacao) {
        try {
            notificacaoDispatcher.enviar(notificacao);
        } catch (RuntimeException ex) {
            log.warn("Falha ao enviar notificacao via {} para {}: {}",
                    notificacao.canal(), notificacao.destinatario(), ex.getMessage());
        }
    }
}
