package org.example.agendamento.adapter.in.scheduler;

import org.example.agendamento.application.port.in.ExpirarReservasPendentesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExpiracaoReservaScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExpiracaoReservaScheduler.class);

    private final ExpirarReservasPendentesUseCase expirarReservasPendentesUseCase;

    public ExpiracaoReservaScheduler(ExpirarReservasPendentesUseCase expirarReservasPendentesUseCase) {
        this.expirarReservasPendentesUseCase = expirarReservasPendentesUseCase;
    }

    @Scheduled(initialDelayString = "${agendamento.expiracao.intervalo-ms:60000}",
            fixedDelayString = "${agendamento.expiracao.intervalo-ms:60000}")
    public void expirarReservasPendentes() {
        int quantidadeExpirada = expirarReservasPendentesUseCase.executar();
        if (quantidadeExpirada > 0) {
            log.info("Expirou {} agendamento(s) pendente(s) de pagamento", quantidadeExpirada);
        }
    }
}
