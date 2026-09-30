package org.example.agendamento.adapter.out.pagamento.mercadopago;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.example.agendamento.application.port.out.CobrancaPix;
import org.example.agendamento.config.MercadoPagoProperties;
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
import static org.assertj.core.api.Assertions.assertThat;

class MercadoPagoGatewayAdapterTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance().build();

    private MercadoPagoGatewayAdapter adapter() {
        MercadoPagoProperties properties = new MercadoPagoProperties(wireMock.baseUrl(), "TEST-token", "segredo-teste");
        return new MercadoPagoGatewayAdapter(RestClient.builder(), properties);
    }

    private Cliente clienteExemplo() {
        return new Cliente(ClienteId.novo(), "Maria Silva",
                new Contato("maria@exemplo.com", "11987654321"),
                DocumentoFiscal.cpf("111.444.777-35"));
    }

    @Test
    void deveGerarCobrancaPixComDadosDoPagadorNoCorpo() {
        wireMock.stubFor(post(urlEqualTo("/v1/payments"))
                .withHeader("Authorization", equalTo("Bearer TEST-token"))
                .withRequestBody(matchingJsonPath("$.payment_method_id", equalTo("pix")))
                .withRequestBody(matchingJsonPath("$.payer.first_name", equalTo("Maria")))
                .withRequestBody(matchingJsonPath("$.payer.last_name", equalTo("Silva")))
                .withRequestBody(matchingJsonPath("$.payer.identification.number", equalTo("11144477735")))
                .withRequestBody(matchingJsonPath("$.external_reference"))
                .willReturn(aResponse().withStatus(201).withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": 123456789,
                                  "status": "pending",
                                  "point_of_interaction": {
                                    "transaction_data": {
                                      "qr_code": "00020126COPIAECOLA",
                                      "qr_code_base64": "BASE64IMG"
                                    }
                                  }
                                }
                                """)));

        CobrancaPix cobranca = adapter().gerarCobrancaPix(AgendamentoId.novo(), clienteExemplo(), Dinheiro.de("30.00"));

        assertThat(cobranca.referenciaExterna()).isEqualTo("123456789");
        assertThat(cobranca.qrCode()).isEqualTo("BASE64IMG");
        assertThat(cobranca.copiaECola()).isEqualTo("00020126COPIAECOLA");
    }

    @Test
    void deveConsultarStatusDoPagamento() {
        wireMock.stubFor(get(urlEqualTo("/v1/payments/123456789"))
                .withHeader("Authorization", equalTo("Bearer TEST-token"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": 123456789,
                                  "status": "approved",
                                  "external_reference": "11111111-1111-1111-1111-111111111111"
                                }
                                """)));

        MercadoPagoGatewayAdapter.StatusConsultado status = adapter().consultarPagamento("123456789");

        assertThat(status.status()).isEqualTo("approved");
        assertThat(status.externalReference()).isEqualTo("11111111-1111-1111-1111-111111111111");
    }
}
