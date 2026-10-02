package org.example.agendamento.adapter.in.web;

import org.example.agendamento.adapter.in.web.dto.MercadoPagoWebhookRequest;
import org.example.agendamento.adapter.out.pagamento.mercadopago.MercadoPagoGatewayAdapter;
import org.example.agendamento.application.port.in.ProcessarWebhookPagamentoUseCase;
import org.example.agendamento.application.port.in.WebhookPagamentoCommand;
import org.example.agendamento.config.MercadoPagoProperties;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Recebe os webhooks reais do Mercado Pago
 * (mercadopago.com.br/developers/pt/docs/your-integrations/notifications/webhooks).
 * Ao contrario do Asaas, o corpo do webhook so traz o id do pagamento — o status
 * precisa ser consultado de volta via MercadoPagoGatewayAdapter.consultarPagamento.
 * Valida a assinatura HMAC-SHA256 no header x-signature antes de processar qualquer
 * evento (o mesmo principio do token do Asaas: sem validar, qualquer um poderia
 * chamar este endpoint e confirmar pagamentos de graca).
 */
@RestController
@RequestMapping("/api/webhooks/mercadopago")
@ConditionalOnProperty(name = "pagamento.gateway", havingValue = "mercadopago")
public class MercadoPagoWebhookController {

    private static final Set<String> STATUS_DE_CONFIRMACAO = Set.of("approved");
    private static final String ALGORITMO_HMAC = "HmacSHA256";

    private final ProcessarWebhookPagamentoUseCase processarWebhookPagamentoUseCase;
    private final MercadoPagoGatewayAdapter mercadoPagoGatewayAdapter;
    private final MercadoPagoProperties properties;

    public MercadoPagoWebhookController(ProcessarWebhookPagamentoUseCase processarWebhookPagamentoUseCase,
                                         MercadoPagoGatewayAdapter mercadoPagoGatewayAdapter,
                                         MercadoPagoProperties properties) {
        this.processarWebhookPagamentoUseCase = processarWebhookPagamentoUseCase;
        this.mercadoPagoGatewayAdapter = mercadoPagoGatewayAdapter;
        this.properties = properties;
    }

    @PostMapping
    public ResponseEntity<Void> processar(
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId,
            @RequestBody MercadoPagoWebhookRequest request) {
        String idPagamento = request.data().id();
        if (!assinaturaValida(xSignature, xRequestId, idPagamento)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        MercadoPagoGatewayAdapter.StatusConsultado statusConsultado = mercadoPagoGatewayAdapter.consultarPagamento(idPagamento);
        boolean pago = STATUS_DE_CONFIRMACAO.contains(statusConsultado.status());
        AgendamentoId agendamentoId = AgendamentoId.de(statusConsultado.externalReference());
        processarWebhookPagamentoUseCase.executar(new WebhookPagamentoCommand(agendamentoId, pago));

        return ResponseEntity.noContent().build();
    }

    private boolean assinaturaValida(String xSignature, String xRequestId, String idPagamento) {
        if (xSignature == null || xRequestId == null) {
            return false;
        }
        Map<String, String> partes = dividirXSignature(xSignature);
        String ts = partes.get("ts");
        String v1Recebido = partes.get("v1");
        if (ts == null || v1Recebido == null) {
            return false;
        }
        String manifesto = "id:" + idPagamento.toLowerCase() + ";request-id:" + xRequestId + ";ts:" + ts + ";";
        String v1Calculado = calcularHmac(manifesto, properties.webhookSecret());
        // Comparacao em tempo constante: e uma assinatura criptografica, o alvo classico de timing attack.
        return MessageDigest.isEqual(
                v1Calculado.getBytes(StandardCharsets.UTF_8),
                v1Recebido.getBytes(StandardCharsets.UTF_8));
    }

    private static Map<String, String> dividirXSignature(String xSignature) {
        return Arrays.stream(xSignature.split(","))
                .map(parte -> parte.split("=", 2))
                .filter(par -> par.length == 2)
                .collect(Collectors.toMap(par -> par[0].trim(), par -> par[1].trim()));
    }

    private static String calcularHmac(String mensagem, String chaveSecreta) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO_HMAC);
            mac.init(new SecretKeySpec(chaveSecreta.getBytes(StandardCharsets.UTF_8), ALGORITMO_HMAC));
            byte[] hash = mac.doFinal(mensagem.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao calcular HMAC do webhook Mercado Pago", e);
        }
    }
}
