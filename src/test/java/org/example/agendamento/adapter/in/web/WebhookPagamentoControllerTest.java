package org.example.agendamento.adapter.in.web;

import org.example.agendamento.application.port.in.ProcessarWebhookPagamentoUseCase;
import org.example.agendamento.application.port.in.WebhookPagamentoCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookPagamentoController.class)
class WebhookPagamentoControllerTest {

    private static final String AGENDAMENTO_ID = "11111111-1111-1111-1111-111111111111";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProcessarWebhookPagamentoUseCase processarWebhookPagamentoUseCase;

    @Test
    void deveProcessarWebhookERetornar204() throws Exception {
        String corpo = """
                {
                    "agendamentoId": "%s",
                    "pago": true
                }
                """.formatted(AGENDAMENTO_ID);

        mockMvc.perform(post("/api/webhooks/pagamento")
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isNoContent());

        verify(processarWebhookPagamentoUseCase).executar(
                new WebhookPagamentoCommand(
                        org.example.agendamento.domain.model.agendamento.AgendamentoId.de(AGENDAMENTO_ID), true));
    }

    @Test
    void deveRetornar400QuandoAgendamentoIdAusente() throws Exception {
        String corpo = """
                {
                    "pago": true
                }
                """;

        mockMvc.perform(post("/api/webhooks/pagamento")
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isBadRequest());
    }
}
