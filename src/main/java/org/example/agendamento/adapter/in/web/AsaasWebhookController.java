package org.example.agendamento.adapter.in.web;

import org.example.agendamento.adapter.in.web.dto.AsaasWebhookRequest;
import org.example.agendamento.application.port.in.ProcessarWebhookPagamentoUseCase;
import org.example.agendamento.application.port.in.WebhookPagamentoCommand;
import org.example.agendamento.config.AsaasProperties;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Set;

/**
 * Recebe os webhooks reais do Asaas (https://docs.asaas.com/docs/webhook-para-cobrancas).
 * Valida o header "asaas-access-token" contra o token configurado (obrigatorio desde
 * fev/2026 no proprio Asaas) antes de processar qualquer evento.
 */
@RestController
@RequestMapping("/api/webhooks/asaas")
public class AsaasWebhookController {

    private static final Set<String> EVENTOS_DE_CONFIRMACAO = Set.of("PAYMENT_CONFIRMED", "PAYMENT_RECEIVED");

    private final ProcessarWebhookPagamentoUseCase processarWebhookPagamentoUseCase;
    private final AsaasProperties properties;

    public AsaasWebhookController(ProcessarWebhookPagamentoUseCase processarWebhookPagamentoUseCase,
                                   AsaasProperties properties) {
        this.processarWebhookPagamentoUseCase = processarWebhookPagamentoUseCase;
        this.properties = properties;
    }

    @PostMapping
    public ResponseEntity<Void> processar(
            @RequestHeader(value = "asaas-access-token", required = false) String tokenRecebido,
            @RequestBody AsaasWebhookRequest request) {
        if (!Objects.equals(tokenRecebido, properties.webhookToken())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean pago = EVENTOS_DE_CONFIRMACAO.contains(request.event());
        AgendamentoId agendamentoId = AgendamentoId.de(request.payment().externalReference());
        processarWebhookPagamentoUseCase.executar(new WebhookPagamentoCommand(agendamentoId, pago));

        return ResponseEntity.noContent().build();
    }
}
