package br.com.josewilson.agendamento.adapter.in.web;

import br.com.josewilson.agendamento.adapter.in.web.security.SecurityConfig;
import br.com.josewilson.agendamento.adapter.in.web.security.TestAutenticacao;
import br.com.josewilson.agendamento.application.port.in.ConsultarAgendamentoUseCase;
import br.com.josewilson.agendamento.application.port.in.ProcessarWebhookPagamentoUseCase;
import br.com.josewilson.agendamento.application.port.in.WebhookPagamentoCommand;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;
import br.com.josewilson.agendamento.domain.model.servico.ServicoId;
import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;
import br.com.josewilson.agendamento.domain.model.shared.PoliticaCancelamento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SimulacaoPagamentoController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.simulacao-pagamento.habilitada=true")
class SimulacaoPagamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ConsultarAgendamentoUseCase consultarAgendamentoUseCase;
    @MockitoBean
    private ProcessarWebhookPagamentoUseCase processarWebhookPagamentoUseCase;

    private Agendamento agendamento() {
        Instant agora = Instant.parse("2026-09-30T12:00:00Z");
        Periodo periodo = new Periodo(agora.plus(Duration.ofDays(2)), agora.plus(Duration.ofDays(2)).plusSeconds(1800));
        return Agendamento.criar(AgendamentoId.novo(), PrestadorId.novo(), ClienteId.novo(), ServicoId.novo(),
                ProfissionalId.novo(), periodo, Dinheiro.de("100.00"), Dinheiro.de("30.00"),
                PoliticaCancelamento.padrao(), agora);
    }

    private static String url(Agendamento a) {
        return "/api/agendamentos/" + a.id().valor() + "/simular-pagamento";
    }

    @Test
    void prestadorDonoSimulaOPagamento() throws Exception {
        Agendamento agendamento = agendamento();
        given(consultarAgendamentoUseCase.executar(any())).willReturn(agendamento);

        mockMvc.perform(post(url(agendamento)).with(csrf())
                        .with(authentication(TestAutenticacao.doPrestador(agendamento.prestadorId()))))
                .andExpect(status().isNoContent());

        verify(processarWebhookPagamentoUseCase).executar(new WebhookPagamentoCommand(agendamento.id(), true));
    }

    @Test
    void ninguemMaisConfirmaPagamento() throws Exception {
        Agendamento agendamento = agendamento();
        given(consultarAgendamentoUseCase.executar(any())).willReturn(agendamento);

        // sem login
        mockMvc.perform(post(url(agendamento)).with(csrf())).andExpect(status().isUnauthorized());
        // o proprio cliente nao pode se "pagar"
        mockMvc.perform(post(url(agendamento)).with(csrf())
                        .with(authentication(TestAutenticacao.doCliente(agendamento.clienteId()))))
                .andExpect(status().isForbidden());
        // prestador de outra agenda
        mockMvc.perform(post(url(agendamento)).with(csrf())
                        .with(authentication(TestAutenticacao.doPrestador(PrestadorId.novo()))))
                .andExpect(status().isForbidden());
        // sem CSRF
        mockMvc.perform(post(url(agendamento))
                        .with(authentication(TestAutenticacao.doPrestador(agendamento.prestadorId()))))
                .andExpect(status().isForbidden());

        verify(processarWebhookPagamentoUseCase, never()).executar(any());
    }
}
