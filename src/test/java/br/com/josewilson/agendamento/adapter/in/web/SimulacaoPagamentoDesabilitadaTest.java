package br.com.josewilson.agendamento.adapter.in.web;

import br.com.josewilson.agendamento.adapter.in.web.security.SecurityConfig;
import br.com.josewilson.agendamento.adapter.in.web.security.TestAutenticacao;
import br.com.josewilson.agendamento.application.port.in.ConsultarAgendamentoUseCase;
import br.com.josewilson.agendamento.application.port.in.ProcessarWebhookPagamentoUseCase;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Padrao de producao: sem a propriedade, o endpoint de simulacao simplesmente nao existe. */
@WebMvcTest(SimulacaoPagamentoController.class)
@Import(SecurityConfig.class)
class SimulacaoPagamentoDesabilitadaTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ConsultarAgendamentoUseCase consultarAgendamentoUseCase;
    @MockitoBean
    private ProcessarWebhookPagamentoUseCase processarWebhookPagamentoUseCase;

    @Test
    void semAPropriedadeNaoHaRota() throws Exception {
        mockMvc.perform(post("/api/agendamentos/" + UUID.randomUUID() + "/simular-pagamento").with(csrf())
                        .with(authentication(TestAutenticacao.doPrestador(PrestadorId.novo()))))
                .andExpect(status().isNotFound());
    }
}
