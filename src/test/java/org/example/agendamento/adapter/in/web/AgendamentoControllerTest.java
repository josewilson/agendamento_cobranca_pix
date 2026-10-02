package org.example.agendamento.adapter.in.web;

import org.example.agendamento.adapter.in.web.security.SecurityConfig;
import org.example.agendamento.adapter.in.web.security.TestAutenticacao;
import org.example.agendamento.application.exception.RecursoNaoEncontradoException;
import org.example.agendamento.application.port.in.CancelarAgendamentoUseCase;
import org.example.agendamento.application.port.in.ConsultarAgendamentoUseCase;
import org.example.agendamento.application.port.in.CriarAgendamentoUseCase;
import org.example.agendamento.application.port.in.ListarAgendamentosPorPrestadorUseCase;
import org.example.agendamento.application.port.in.MarcarNoShowUseCase;
import org.example.agendamento.application.port.in.ResultadoCriacaoAgendamento;
import org.example.agendamento.domain.exception.ConflitoDeHorarioException;
import org.example.agendamento.domain.model.agendamento.Agendamento;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.agendamento.ResultadoCancelamento;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.ServicoId;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.example.agendamento.domain.model.shared.Periodo;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgendamentoController.class)
@Import(SecurityConfig.class)
class AgendamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CriarAgendamentoUseCase criarAgendamentoUseCase;
    @MockitoBean
    private ConsultarAgendamentoUseCase consultarAgendamentoUseCase;
    @MockitoBean
    private CancelarAgendamentoUseCase cancelarAgendamentoUseCase;
    @MockitoBean
    private MarcarNoShowUseCase marcarNoShowUseCase;
    @MockitoBean
    private ListarAgendamentosPorPrestadorUseCase listarAgendamentosPorPrestadorUseCase;

    private static Agendamento agendamentoExemplo() {
        Instant agora = Instant.parse("2026-09-30T12:00:00Z");
        Periodo periodo = new Periodo(agora.plus(Duration.ofDays(2)), agora.plus(Duration.ofDays(2)).plus(Duration.ofMinutes(30)));
        return Agendamento.criar(AgendamentoId.novo(), PrestadorId.novo(), ClienteId.novo(), ServicoId.novo(),
                periodo, Dinheiro.de("100.00"), Dinheiro.ZERO,
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100)), agora);
    }

    @Test
    void deveCriarAgendamentoERetornar201() throws Exception {
        Agendamento agendamento = agendamentoExemplo();
        given(criarAgendamentoUseCase.executar(any()))
                .willReturn(new ResultadoCriacaoAgendamento(agendamento, Optional.empty()));

        String corpo = """
                {
                    "prestadorId": "%s",
                    "clienteId": "%s",
                    "servicoId": "%s",
                    "inicio": "2026-10-02T12:00:00Z",
                    "fim": "2026-10-02T12:30:00Z"
                }
                """.formatted(agendamento.prestadorId().valor(), agendamento.clienteId().valor(), agendamento.servicoId().valor());

        mockMvc.perform(post("/api/agendamentos")
                        .with(csrf())
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.agendamento.id").value(agendamento.id().valor().toString()))
                .andExpect(jsonPath("$.agendamento.status").value("CONFIRMADO"))
                .andExpect(jsonPath("$.cobranca").doesNotExist());
    }

    @Test
    void deveRetornar400QuandoCampoObrigatorioAusente() throws Exception {
        String corpo = """
                {
                    "clienteId": "%s",
                    "servicoId": "%s",
                    "inicio": "2026-10-02T12:00:00Z",
                    "fim": "2026-10-02T12:30:00Z"
                }
                """.formatted(UUID_EXEMPLO, UUID_EXEMPLO);

        mockMvc.perform(post("/api/agendamentos")
                        .with(csrf())
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar409QuandoHorarioEmConflito() throws Exception {
        given(criarAgendamentoUseCase.executar(any()))
                .willThrow(new ConflitoDeHorarioException("conflito"));

        String corpo = """
                {
                    "prestadorId": "%s",
                    "clienteId": "%s",
                    "servicoId": "%s",
                    "inicio": "2026-10-02T12:00:00Z",
                    "fim": "2026-10-02T12:30:00Z"
                }
                """.formatted(UUID_EXEMPLO, UUID_EXEMPLO, UUID_EXEMPLO);

        mockMvc.perform(post("/api/agendamentos")
                        .with(csrf())
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar502QuandoGatewayDePagamentoFalhar() throws Exception {
        given(criarAgendamentoUseCase.executar(any()))
                .willThrow(org.springframework.web.client.HttpClientErrorException.Unauthorized
                        .create(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null));

        String corpo = """
                {
                    "prestadorId": "%s",
                    "clienteId": "%s",
                    "servicoId": "%s",
                    "inicio": "2026-10-02T12:00:00Z",
                    "fim": "2026-10-02T12:30:00Z"
                }
                """.formatted(UUID_EXEMPLO, UUID_EXEMPLO, UUID_EXEMPLO);

        mockMvc.perform(post("/api/agendamentos")
                        .with(csrf())
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isBadGateway());
    }

    @Test
    void deveConsultarAgendamentoERetornar200() throws Exception {
        Agendamento agendamento = agendamentoExemplo();
        given(consultarAgendamentoUseCase.executar(any())).willReturn(agendamento);

        mockMvc.perform(get("/api/agendamentos/" + agendamento.id().valor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(agendamento.id().valor().toString()));
    }

    @Test
    void deveRetornar404QuandoAgendamentoNaoEncontrado() throws Exception {
        given(consultarAgendamentoUseCase.executar(any()))
                .willThrow(new RecursoNaoEncontradoException("nao encontrado"));

        mockMvc.perform(get("/api/agendamentos/" + UUID_EXEMPLO))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveCancelarAgendamentoERetornar200() throws Exception {
        given(cancelarAgendamentoUseCase.executar(any()))
                .willReturn(new ResultadoCancelamento(Dinheiro.ZERO, Dinheiro.de("30.00")));

        mockMvc.perform(post("/api/agendamentos/" + UUID_EXEMPLO + "/cancelar").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorReembolsado").value(30.00));
    }

    @Test
    void deveMarcarNoShowERetornar204() throws Exception {
        mockMvc.perform(post("/api/agendamentos/" + UUID_EXEMPLO + "/no-show").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveListarAgendamentosPorPrestadorERetornar200() throws Exception {
        Agendamento agendamento = agendamentoExemplo();
        given(listarAgendamentosPorPrestadorUseCase.executar(any())).willReturn(List.of(agendamento));

        mockMvc.perform(get("/api/agendamentos").with(authentication(TestAutenticacao.doPrestador(agendamento.prestadorId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(agendamento.id().valor().toString()));
    }

    @Test
    void deveRetornar401QuandoListarSemLogin() throws Exception {
        mockMvc.perform(get("/api/agendamentos"))
                .andExpect(status().isUnauthorized());
    }

    private static final String UUID_EXEMPLO = "11111111-1111-1111-1111-111111111111";
}
