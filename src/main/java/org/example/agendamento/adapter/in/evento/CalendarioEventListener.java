package org.example.agendamento.adapter.in.evento;

import org.example.agendamento.application.port.out.AgendamentoRepository;
import org.example.agendamento.application.port.out.CalendarioExternoPort;
import org.example.agendamento.application.port.out.ClienteRepository;
import org.example.agendamento.application.port.out.ServicoRepository;
import org.example.agendamento.domain.event.AgendamentoCancelado;
import org.example.agendamento.domain.event.AgendamentoConfirmado;
import org.example.agendamento.domain.model.agendamento.Agendamento;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.servico.Servico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Reage aos mesmos eventos de dominio que NotificacaoEventListener, mas para manter a
 * agenda do prestador sincronizada num calendario externo (Google Calendar). So cria o
 * evento externo na confirmacao (pagamento recebido) — um agendamento PENDENTE_PAGAMENTO
 * pode expirar sem nunca ocupar a agenda de verdade, entao nao faz sentido reagir a
 * AgendamentoCriado aqui. Falhas sao logadas e nunca propagadas, pelo mesmo motivo do
 * NotificacaoEventListener: um calendario indisponivel nao pode derrubar um caso de uso
 * que ja persistiu o agendamento.
 */
@Component
public class CalendarioEventListener {

    private static final Logger log = LoggerFactory.getLogger(CalendarioEventListener.class);

    private final CalendarioExternoPort calendarioExternoPort;
    private final AgendamentoRepository agendamentoRepository;
    private final ServicoRepository servicoRepository;
    private final ClienteRepository clienteRepository;

    public CalendarioEventListener(CalendarioExternoPort calendarioExternoPort,
                                    AgendamentoRepository agendamentoRepository,
                                    ServicoRepository servicoRepository,
                                    ClienteRepository clienteRepository) {
        this.calendarioExternoPort = calendarioExternoPort;
        this.agendamentoRepository = agendamentoRepository;
        this.servicoRepository = servicoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Async("eventosExecutor")
    @EventListener
    public void aoConfirmarAgendamento(AgendamentoConfirmado evento) {
        try {
            agendamentoRepository.buscarPorId(evento.agendamentoId()).ifPresent(this::sincronizar);
        } catch (RuntimeException ex) {
            log.warn("Falha ao sincronizar agendamento {} com o calendario externo: {}",
                    evento.agendamentoId(), ex.getMessage());
        }
    }

    @Async("eventosExecutor")
    @EventListener
    public void aoCancelarAgendamento(AgendamentoCancelado evento) {
        try {
            calendarioExternoPort.removerEvento(evento.agendamentoId());
        } catch (RuntimeException ex) {
            log.warn("Falha ao remover agendamento {} do calendario externo: {}",
                    evento.agendamentoId(), ex.getMessage());
        }
    }

    private void sincronizar(Agendamento agendamento) {
        Servico servico = servicoRepository.buscarPorId(agendamento.servicoId())
                .orElseThrow(() -> new IllegalStateException("servico nao encontrado: " + agendamento.servicoId()));
        Cliente cliente = clienteRepository.buscarPorId(agendamento.clienteId())
                .orElseThrow(() -> new IllegalStateException("cliente nao encontrado: " + agendamento.clienteId()));
        String titulo = servico.nome() + " - " + cliente.nome();
        String descricao = "Agendamento confirmado via agendamento-com-cobranca-pix.";
        calendarioExternoPort.sincronizarEvento(agendamento.id(), titulo, descricao, agendamento.periodo());
    }
}
