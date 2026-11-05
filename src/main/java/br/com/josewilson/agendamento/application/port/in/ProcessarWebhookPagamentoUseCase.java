package br.com.josewilson.agendamento.application.port.in;

public interface ProcessarWebhookPagamentoUseCase {
    void executar(WebhookPagamentoCommand command);
}
