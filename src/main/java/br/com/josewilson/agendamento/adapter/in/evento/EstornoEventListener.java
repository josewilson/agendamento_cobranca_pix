package br.com.josewilson.agendamento.adapter.in.evento;

import br.com.josewilson.agendamento.application.port.out.AgendamentoRepository;
import br.com.josewilson.agendamento.application.port.out.Clock;
import br.com.josewilson.agendamento.application.port.out.FilaDeEstornos;
import br.com.josewilson.agendamento.application.port.out.GatewayDePagamento;
import br.com.josewilson.agendamento.domain.event.AgendamentoCancelado;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.scheduling.annotation.Async;
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

    private final FilaDeEstornos fila;
    private final Clock clock;

    public EstornoEventListener(GatewayDePagamento gatewayDePagamento, AgendamentoRepository agendamentoRepository,
                                FilaDeEstornos fila, Clock clock) {
        this.gatewayDePagamento = gatewayDePagamento;
        this.agendamentoRepository = agendamentoRepository;
        this.fila = fila;
        this.clock = clock;
    }

    @Async("eventosExecutor")
    @TransactionalEventListener(fallbackExecution = true)
    public void aoCancelarAgendamento(AgendamentoCancelado evento) {
        Dinheiro valorReembolsado = evento.resultado().valorReembolsado();
        if (!valorReembolsado.maiorQue(Dinheiro.ZERO)) {
            return;
        }
        agendamentoRepository.buscarPorId(evento.agendamentoId())
                .flatMap(Agendamento::referenciaPagamento)
                .ifPresent(referencia -> estornarOuEnfileirar(evento, referencia, valorReembolsado));
    }

    /** Falha do gateway vai pra fila persistente (reprocessada por ReprocessamentoEstornoScheduler). */
    private void estornarOuEnfileirar(AgendamentoCancelado evento, String referencia, Dinheiro valor) {
        try {
            gatewayDePagamento.estornar(referencia, valor);
        } catch (RuntimeException ex) {
            log.warn("Falha ao estornar pagamento do agendamento {}, enfileirando para nova tentativa: {}",
                    evento.agendamentoId(), ex.getMessage());
            try {
                fila.registrar(evento.agendamentoId(), referencia, valor, ex.getMessage(), clock.agora());
            } catch (RuntimeException filaEx) {
                log.error("Estorno do agendamento {} NAO enfileirado, exige acao manual: {}",
                        evento.agendamentoId(), filaEx.getMessage());
            }
        }
    }
}
