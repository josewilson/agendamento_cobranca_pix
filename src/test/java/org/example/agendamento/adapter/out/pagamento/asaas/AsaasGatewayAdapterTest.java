package org.example.agendamento.adapter.out.pagamento.asaas;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.example.agendamento.application.port.out.CobrancaPix;
import org.example.agendamento.config.AsaasProperties;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.shared.Contato;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

class AsaasGatewayAdapterTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance().build();

    private AsaasGatewayAdapter adapter() {
        AsaasProperties properties = new AsaasProperties(wireMock.baseUrl(), "test-api-key", "test-webhook-token");
        return new AsaasGatewayAdapter(RestClient.builder(), properties);
    }

    private Cliente clienteExemplo() {
        return new Cliente(ClienteId.novo(), "Maria Silva",
                new Contato("maria@exemplo.com", "11987654321"),
                DocumentoFiscal.cpf("111.444.777-35"));
    }

    @Test
    void deveGerarCobrancaPixOrquestrandoClienteEPagamentoEQrCode() {
        wireMock.stubFor(post(urlEqualTo("/customers"))
                .withHeader("access_token", equalTo("test-api-key"))
                .withRequestBody(matchingJsonPath("$.cpfCnpj", equalTo("11144477735")))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"cus_123\"}")));

        wireMock.stubFor(post(urlEqualTo("/payments"))
                .withRequestBody(matchingJsonPath("$.billingType", equalTo("PIX")))
                .withRequestBody(matchingJsonPath("$.customer", equalTo("cus_123")))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"pay_456\"}")));

        wireMock.stubFor(get(urlPathEqualTo("/payments/pay_456/pixQrCode"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"encodedImage\":\"BASE64IMG\",\"payload\":\"00020126COPIAECOLA\","
                                + "\"expirationDate\":\"2026-10-03 23:59:59\"}")));

        CobrancaPix cobranca = adapter().gerarCobrancaPix(AgendamentoId.novo(), clienteExemplo(), Dinheiro.de("30.00"));

        assertThat(cobranca.referenciaExterna()).isEqualTo("pay_456");
        assertThat(cobranca.qrCode()).isEqualTo("BASE64IMG");
        assertThat(cobranca.copiaECola()).isEqualTo("00020126COPIAECOLA");
    }
}
