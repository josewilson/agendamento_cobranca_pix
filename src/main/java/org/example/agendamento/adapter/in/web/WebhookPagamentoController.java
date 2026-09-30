package org.example.agendamento.adapter.in.web;

import jakarta.validation.Valid;
import org.example.agendamento.adapter.in.web.dto.WebhookPagamentoRequest;
import org.example.agendamento.application.port.in.ProcessarWebhookPagamentoUseCase;
import org.example.agendamento.application.port.in.WebhookPagamentoCommand;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/pagamento")
public class WebhookPagamentoController {

    private final ProcessarWebhookPagamentoUseCase processarWebhookPagamentoUseCase;

    public WebhookPagamentoController(ProcessarWebhookPagamentoUseCase processarWebhookPagamentoUseCase) {
        this.processarWebhookPagamentoUseCase = processarWebhookPagamentoUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void processar(@Valid @RequestBody WebhookPagamentoRequest request) {
        processarWebhookPagamentoUseCase.executar(
                new WebhookPagamentoCommand(new AgendamentoId(request.agendamentoId()), request.pago()));
    }
}
