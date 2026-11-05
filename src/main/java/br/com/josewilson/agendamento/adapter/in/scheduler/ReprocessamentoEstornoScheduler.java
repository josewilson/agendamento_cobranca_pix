package br.com.josewilson.agendamento.adapter.in.scheduler;

import br.com.josewilson.agendamento.application.port.out.FilaDeEstornos;
import br.com.josewilson.agendamento.application.port.out.GatewayDePagamento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Reprocessa estornos que falharam no gateway (ver EstornoEventListener). Ao esgotar as tentativas
 * a linha permanece em estorno_pendente e o log ERROR sinaliza que exige acao manual.
 */
@Component
public class ReprocessamentoEstornoScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReprocessamentoEstornoScheduler.class);

    private final FilaDeEstornos fila;
    private final GatewayDePagamento gateway;
    private final int maxTentativas;

    public ReprocessamentoEstornoScheduler(FilaDeEstornos fila, GatewayDePagamento gateway,
                                           @Value("${estorno.max-tentativas:5}") int maxTentativas) {
        this.fila = fila;
        this.gateway = gateway;
        this.maxTentativas = maxTentativas;
    }

    // initialDelay = fixedDelay pelo mesmo motivo do ExpiracaoReservaScheduler (nao rodar ao subir testes)
    @Scheduled(initialDelayString = "${estorno.reprocessamento.intervalo-ms:300000}",
            fixedDelayString = "${estorno.reprocessamento.intervalo-ms:300000}")
    public void reprocessar() {
        for (FilaDeEstornos.EstornoPendente pendente : fila.buscarPendentes(maxTentativas)) {
            try {
                gateway.estornar(pendente.referencia(), pendente.valor());
                fila.concluir(pendente.id());
                log.info("Estorno reprocessado com sucesso (agendamento {})", pendente.agendamentoId());
            } catch (RuntimeException ex) {
                fila.registrarNovaFalha(pendente.id(), ex.getMessage());
                if (pendente.tentativas() + 1 >= maxTentativas) {
                    log.error("Estorno do agendamento {} esgotou {} tentativas e exige acao manual: {}",
                            pendente.agendamentoId(), maxTentativas, ex.getMessage());
                } else {
                    log.warn("Reprocessamento de estorno do agendamento {} falhou (tentativa {}): {}",
                            pendente.agendamentoId(), pendente.tentativas() + 1, ex.getMessage());
                }
            }
        }
    }
}
