package org.example.agendamento.application.port.in;

public interface ProcessarWebhookPagamentoUseCase {
    void executar(WebhookPagamentoCommand command);
}
