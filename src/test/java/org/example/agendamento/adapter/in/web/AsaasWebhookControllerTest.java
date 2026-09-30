package org.example.agendamento.adapter.in.web;

import org.example.agendamento.application.port.in.ProcessarWebhookPagamentoUseCase;
import org.example.agendamento.application.port.in.WebhookPagamentoCommand;
import org.example.agendamento.config.AsaasProperties;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AsaasWebhookController.class)
class AsaasWebhookControllerTest {

    private static final String AGENDAMENTO_ID = "11111111-1111-1111-1111-111111111111";
    private static final String TOKEN_VALIDO = "token-secreto-de-teste";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProcessarWebhookPagamentoUseCase processarWebhookPagamentoUseCase;

    @TestConfiguration
    static class ConfiguracaoDeTeste {
        @Bean
        AsaasProperties asaasProperties() {
            return new AsaasProperties("http://localhost", "chave-de-teste", TOKEN_VALIDO);
        }
    }

    private String corpoWebhook(String evento) {
        return """
                {
                    "event": "%s",
                    "payment": {
                        "id": "pay_456",
                        "externalReference": "%s",
                        "status": "RECEIVED"
                    }
                }
                """.formatted(evento, AGENDAMENTO_ID);
    }

    @Test
    void deveConfirmarPagamentoQuandoEventoDeConfirmacaoComTokenValido() throws Exception {
        mockMvc.perform(post("/api/webhooks/asaas")
                        .header("asaas-access-token", TOKEN_VALIDO)
                        .contentType("application/json")
                        .content(corpoWebhook("PAYMENT_RECEIVED")))
                .andExpect(status().isNoContent());

        verify(processarWebhookPagamentoUseCase).executar(
                new WebhookPagamentoCommand(AgendamentoId.de(AGENDAMENTO_ID), true));
    }

    @Test
    void naoDeveConfirmarQuandoEventoNaoEDeConfirmacao() throws Exception {
        mockMvc.perform(post("/api/webhooks/asaas")
                        .header("asaas-access-token", TOKEN_VALIDO)
                        .contentType("application/json")
                        .content(corpoWebhook("PAYMENT_OVERDUE")))
                .andExpect(status().isNoContent());

        verify(processarWebhookPagamentoUseCase).executar(
                new WebhookPagamentoCommand(AgendamentoId.de(AGENDAMENTO_ID), false));
    }

    @Test
    void deveRetornar403QuandoTokenInvalido() throws Exception {
        mockMvc.perform(post("/api/webhooks/asaas")
                        .header("asaas-access-token", "token-errado")
                        .contentType("application/json")
                        .content(corpoWebhook("PAYMENT_RECEIVED")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(processarWebhookPagamentoUseCase);
    }

    @Test
    void deveRetornar403QuandoTokenAusente() throws Exception {
        mockMvc.perform(post("/api/webhooks/asaas")
                        .contentType("application/json")
                        .content(corpoWebhook("PAYMENT_RECEIVED")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(processarWebhookPagamentoUseCase);
    }
}
