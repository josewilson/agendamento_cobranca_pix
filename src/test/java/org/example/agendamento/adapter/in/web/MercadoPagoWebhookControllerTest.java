package org.example.agendamento.adapter.in.web;

import org.example.agendamento.adapter.out.pagamento.mercadopago.MercadoPagoGatewayAdapter;
import org.example.agendamento.application.port.in.ProcessarWebhookPagamentoUseCase;
import org.example.agendamento.application.port.in.WebhookPagamentoCommand;
import org.example.agendamento.config.MercadoPagoProperties;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MercadoPagoWebhookController.class)
@TestPropertySource(properties = "pagamento.gateway=mercadopago")
class MercadoPagoWebhookControllerTest {

    private static final String SEGREDO = "segredo-teste";
    private static final String AGENDAMENTO_ID = "11111111-1111-1111-1111-111111111111";
    private static final String ID_PAGAMENTO = "123456789";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProcessarWebhookPagamentoUseCase processarWebhookPagamentoUseCase;

    @MockitoBean
    private MercadoPagoGatewayAdapter mercadoPagoGatewayAdapter;

    @TestConfiguration
    static class ConfiguracaoDeTeste {
        @Bean
        MercadoPagoProperties mercadoPagoProperties() {
            return new MercadoPagoProperties("http://localhost", "token-teste", SEGREDO);
        }
    }

    private String corpoWebhook() {
        return """
                {
                    "type": "payment",
                    "action": "payment.updated",
                    "data": { "id": "%s" }
                }
                """.formatted(ID_PAGAMENTO);
    }

    private String assinaturaValida(String ts, String requestId) {
        String manifesto = "id:" + ID_PAGAMENTO.toLowerCase() + ";request-id:" + requestId + ";ts:" + ts + ";";
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SEGREDO.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(manifesto.getBytes(StandardCharsets.UTF_8));
            return "ts=" + ts + ",v1=" + HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void deveConfirmarPagamentoQuandoAssinaturaValidaEStatusAprovado() throws Exception {
        given(mercadoPagoGatewayAdapter.consultarPagamento(ID_PAGAMENTO))
                .willReturn(new MercadoPagoGatewayAdapter.StatusConsultado("approved", AGENDAMENTO_ID));

        mockMvc.perform(post("/api/webhooks/mercadopago")
                        .header("x-signature", assinaturaValida("1700000000000", "req-1"))
                        .header("x-request-id", "req-1")
                        .contentType("application/json")
                        .content(corpoWebhook()))
                .andExpect(status().isNoContent());

        verify(processarWebhookPagamentoUseCase).executar(
                new WebhookPagamentoCommand(AgendamentoId.de(AGENDAMENTO_ID), true));
    }

    @Test
    void naoDeveConfirmarQuandoStatusNaoEAprovado() throws Exception {
        given(mercadoPagoGatewayAdapter.consultarPagamento(ID_PAGAMENTO))
                .willReturn(new MercadoPagoGatewayAdapter.StatusConsultado("pending", AGENDAMENTO_ID));

        mockMvc.perform(post("/api/webhooks/mercadopago")
                        .header("x-signature", assinaturaValida("1700000000000", "req-2"))
                        .header("x-request-id", "req-2")
                        .contentType("application/json")
                        .content(corpoWebhook()))
                .andExpect(status().isNoContent());

        verify(processarWebhookPagamentoUseCase).executar(
                new WebhookPagamentoCommand(AgendamentoId.de(AGENDAMENTO_ID), false));
    }

    @Test
    void deveRetornar403QuandoAssinaturaInvalida() throws Exception {
        mockMvc.perform(post("/api/webhooks/mercadopago")
                        .header("x-signature", "ts=1700000000000,v1=assinaturaErrada")
                        .header("x-request-id", "req-3")
                        .contentType("application/json")
                        .content(corpoWebhook()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(processarWebhookPagamentoUseCase);
        verifyNoInteractions(mercadoPagoGatewayAdapter);
    }

    @Test
    void deveRetornar403QuandoHeadersAusentes() throws Exception {
        mockMvc.perform(post("/api/webhooks/mercadopago")
                        .contentType("application/json")
                        .content(corpoWebhook()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(processarWebhookPagamentoUseCase);
    }
}
