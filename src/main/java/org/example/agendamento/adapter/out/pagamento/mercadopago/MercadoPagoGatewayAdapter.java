package org.example.agendamento.adapter.out.pagamento.mercadopago;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.agendamento.application.port.out.CobrancaPix;
import org.example.agendamento.application.port.out.GatewayDePagamento;
import org.example.agendamento.config.MercadoPagoProperties;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Integra com a API real do Mercado Pago (mercadopago.com.br/developers) para gerar
 * cobrancas Pix. Segundo gateway do projeto — junto com AsaasGatewayAdapter, demonstra
 * a troca de gateway via configuracao (propriedade pagamento.gateway), sem tocar em
 * codigo de aplicacao.
 *
 * Diferencas de integracao em relacao ao Asaas, propositalmente preservadas aqui como
 * registro do que cada gateway realmente exige:
 * - Nao ha "criar cliente" separado: os dados do pagador vao direto no corpo da cobranca.
 * - O corpo usa snake_case (transaction_amount, payment_method_id...), nao camelCase.
 * - O webhook so informa o id do pagamento, nao o status — por isso consultarPagamento()
 *   existe e e usado por MercadoPagoWebhookController apos validar a assinatura.
 * - Autenticacao via access token que ja distingue sandbox (prefixo TEST-) de producao
 *   (APP_USR-), nao por base URL separada como o Asaas.
 */
@Component
@Profile("!dev")
@ConditionalOnProperty(name = "pagamento.gateway", havingValue = "mercadopago")
public class MercadoPagoGatewayAdapter implements GatewayDePagamento {

    private static final ZoneId ZONA_BRASIL = ZoneId.of("America/Sao_Paulo");
    private static final Duration VALIDADE_QR_CODE = Duration.ofMinutes(30);

    private final RestClient restClient;

    public MercadoPagoGatewayAdapter(RestClient.Builder restClientBuilder, MercadoPagoProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader("Authorization", "Bearer " + properties.accessToken())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public CobrancaPix gerarCobrancaPix(AgendamentoId agendamentoId, Cliente cliente, Dinheiro valor) {
        Instant expiraEm = Instant.now().plus(VALIDADE_QR_CODE);
        String[] nome = dividirNome(cliente.nome());

        PagamentoRequest request = new PagamentoRequest(
                valor.valor(),
                "Sinal de agendamento " + agendamentoId.valor(),
                "pix",
                new Payer(cliente.contato().email(), nome[0], nome[1],
                        new Identification(cliente.documento().tipo().name(), cliente.documento().numero())),
                agendamentoId.valor().toString(),
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(expiraEm.atZone(ZONA_BRASIL)));

        PagamentoResponse response = restClient.post()
                .uri("/v1/payments")
                .header("X-Idempotency-Key", UUID.randomUUID().toString())
                .body(request)
                .retrieve()
                .body(PagamentoResponse.class);

        TransactionData dados = response.pointOfInteraction().transactionData();
        return new CobrancaPix(String.valueOf(response.id()), dados.qrCodeBase64(), dados.qrCode(), expiraEm);
    }

    @Override
    public void estornar(String referenciaExterna, Dinheiro valor) {
        RefundRequest request = new RefundRequest(valor.valor());
        restClient.post()
                .uri("/v1/payments/{id}/refunds", referenciaExterna)
                .header("X-Idempotency-Key", UUID.randomUUID().toString())
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    /** Usado por MercadoPagoWebhookController: o webhook so traz o id do pagamento, nao o status. */
    public StatusConsultado consultarPagamento(String idPagamento) {
        PagamentoResponse response = restClient.get()
                .uri("/v1/payments/{id}", idPagamento)
                .retrieve()
                .body(PagamentoResponse.class);
        return new StatusConsultado(response.status(), response.externalReference());
    }

    private static String[] dividirNome(String nomeCompleto) {
        String[] partes = nomeCompleto.trim().split("\\s+", 2);
        return partes.length == 2 ? partes : new String[] {partes[0], partes[0]};
    }

    public record StatusConsultado(String status, String externalReference) {
    }

    private record PagamentoRequest(
            @JsonProperty("transaction_amount") BigDecimal transactionAmount,
            String description,
            @JsonProperty("payment_method_id") String paymentMethodId,
            Payer payer,
            @JsonProperty("external_reference") String externalReference,
            @JsonProperty("date_of_expiration") String dateOfExpiration) {
    }

    private record Payer(String email, @JsonProperty("first_name") String firstName,
                          @JsonProperty("last_name") String lastName, Identification identification) {
    }

    private record Identification(String type, String number) {
    }

    private record PagamentoResponse(Long id, String status,
                                      @JsonProperty("external_reference") String externalReference,
                                      @JsonProperty("point_of_interaction") PointOfInteraction pointOfInteraction) {
    }

    private record PointOfInteraction(@JsonProperty("transaction_data") TransactionData transactionData) {
    }

    private record TransactionData(@JsonProperty("qr_code") String qrCode,
                                    @JsonProperty("qr_code_base64") String qrCodeBase64) {
    }

    private record RefundRequest(BigDecimal amount) {
    }
}
