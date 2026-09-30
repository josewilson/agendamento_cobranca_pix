package org.example.agendamento.adapter.out.pagamento.asaas;

import org.example.agendamento.application.port.out.CobrancaPix;
import org.example.agendamento.application.port.out.GatewayDePagamento;
import org.example.agendamento.config.AsaasProperties;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Integra com a API real do Asaas (https://docs.asaas.com) para gerar cobrancas Pix.
 * Fluxo: cria/associa um cliente no Asaas, cria a cobranca vinculada a esse cliente
 * (com o proprio agendamentoId como externalReference, para o webhook conseguir
 * correlacionar sem precisarmos manter um mapeamento a parte), e busca o QR Code.
 */
@Component
@Profile("!dev")
public class AsaasGatewayAdapter implements GatewayDePagamento {

    private static final ZoneId ZONA_ASAAS = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter FORMATO_DATA_HORA_ASAAS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestClient restClient;

    public AsaasGatewayAdapter(RestClient.Builder restClientBuilder, AsaasProperties properties) {
        // Forca HTTP/1.1: o HttpClient da JDK tenta negociar HTTP/2 por padrao, o que
        // servidores de teste como o WireMock nao suportam, encerrando a conexao (EOF).
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .defaultHeader("access_token", properties.apiKey())
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("User-Agent", "agendamento-com-cobranca-pix")
                .build();
    }

    @Override
    public CobrancaPix gerarCobrancaPix(AgendamentoId agendamentoId, Cliente cliente, Dinheiro valor) {
        String clienteAsaasId = criarCliente(cliente);
        String pagamentoId = criarPagamento(agendamentoId, clienteAsaasId, valor);
        return buscarQrCode(pagamentoId);
    }

    private String criarCliente(Cliente cliente) {
        AsaasCustomerRequest request = new AsaasCustomerRequest(
                cliente.nome(),
                cliente.documento().numero(),
                cliente.contato().email(),
                cliente.contato().telefone(),
                cliente.id().valor().toString());

        AsaasCustomerResponse response = restClient.post()
                .uri("/customers")
                .body(request)
                .retrieve()
                .body(AsaasCustomerResponse.class);

        return response.id();
    }

    private String criarPagamento(AgendamentoId agendamentoId, String clienteAsaasId, Dinheiro valor) {
        AsaasPaymentRequest request = new AsaasPaymentRequest(
                clienteAsaasId,
                "PIX",
                valor.valor(),
                LocalDate.now(ZONA_ASAAS).plusDays(1),
                agendamentoId.valor().toString(),
                "Sinal de agendamento " + agendamentoId.valor());

        AsaasPaymentResponse response = restClient.post()
                .uri("/payments")
                .body(request)
                .retrieve()
                .body(AsaasPaymentResponse.class);

        return response.id();
    }

    private CobrancaPix buscarQrCode(String pagamentoId) {
        AsaasPixQrCodeResponse response = restClient.get()
                .uri("/payments/{id}/pixQrCode", pagamentoId)
                .retrieve()
                .body(AsaasPixQrCodeResponse.class);

        Instant expiraEm = LocalDateTime.parse(response.expirationDate(), FORMATO_DATA_HORA_ASAAS)
                .atZone(ZONA_ASAAS)
                .toInstant();

        return new CobrancaPix(pagamentoId, response.encodedImage(), response.payload(), expiraEm);
    }

    private record AsaasCustomerRequest(String name, String cpfCnpj, String email, String phone, String externalReference) {
    }

    private record AsaasCustomerResponse(String id) {
    }

    private record AsaasPaymentRequest(String customer, String billingType, BigDecimal value, LocalDate dueDate,
                                        String externalReference, String description) {
    }

    private record AsaasPaymentResponse(String id) {
    }

    private record AsaasPixQrCodeResponse(String encodedImage, String payload, String expirationDate) {
    }
}
