package org.example.agendamento.adapter.in.evento;

import org.example.agendamento.application.port.out.AgendamentoRepository;
import org.example.agendamento.application.port.out.GatewayDePagamento;
import org.example.agendamento.domain.event.AgendamentoCancelado;
import org.example.agendamento.domain.model.agendamento.Agendamento;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Reage a AgendamentoCancelado para executar o estorno real no gateway de pagamento, mesmo
 * padrao do NotificacaoEventListener/CalendarioEventListener: falhas sao logadas e nunca
 * propagadas, ja que o cancelamento em si ja foi persistido antes deste efeito colateral.
 * So age quando ha valor a reembolsar E o agendamento tem uma referencia de pagamento
 * vinculada (agendamentos sem sinal nunca geraram cobranca, logo nao ha o que estornar).
 */
@Component
public class EstornoEventListener {

    private static final Logger log = LoggerFactory.getLogger(EstornoEventListener.class);

    private final GatewayDePagamento gatewayDePagamento;
    private final AgendamentoRepository agendamentoRepository;

    public EstornoEventListener(GatewayDePagamento gatewayDePagamento, AgendamentoRepository agendamentoRepository) {
        this.gatewayDePagamento = gatewayDePagamento;
        this.agendamentoRepository = agendamentoRepository;
    }

    @EventListener
    public void aoCancelarAgendamento(AgendamentoCancelado evento) {
        Dinheiro valorReembolsado = evento.resultado().valorReembolsado();
        if (!valorReembolsado.maiorQue(Dinheiro.ZERO)) {
            return;
        }
        try {
            agendamentoRepository.buscarPorId(evento.agendamentoId())
                    .flatMap(Agendamento::referenciaPagamento)
                    .ifPresent(referencia -> gatewayDePagamento.estornar(referencia, valorReembolsado));
        } catch (RuntimeException ex) {
            log.warn("Falha ao estornar pagamento do agendamento {}: {}", evento.agendamentoId(), ex.getMessage());
        }
    }
}
