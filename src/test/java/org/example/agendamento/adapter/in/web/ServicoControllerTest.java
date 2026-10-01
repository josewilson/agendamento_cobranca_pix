package org.example.agendamento.adapter.in.web;

import org.example.agendamento.application.port.in.CadastrarServicoUseCase;
import org.example.agendamento.application.port.in.ListarServicosPorPrestadorUseCase;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.Servico;
import org.example.agendamento.domain.model.servico.ServicoId;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServicoController.class)
class ServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CadastrarServicoUseCase cadastrarServicoUseCase;
    @MockitoBean
    private ListarServicosPorPrestadorUseCase listarServicosPorPrestadorUseCase;

    private static final PrestadorId PRESTADOR_ID = PrestadorId.novo();

    private static Servico servicoExemplo() {
        return new Servico(ServicoId.novo(), PRESTADOR_ID, "Massagem relaxante",
                Duration.ofMinutes(60), Dinheiro.de("150.00"), BigDecimal.valueOf(30));
    }

    @Test
    void deveCadastrarServicoERetornar201() throws Exception {
        Servico servico = servicoExemplo();
        given(cadastrarServicoUseCase.executar(any())).willReturn(servico);

        String corpo = """
                {
                    "prestadorId": "%s",
                    "nome": "Massagem relaxante",
                    "duracaoMinutos": 60,
                    "preco": 150.00,
                    "percentualSinal": 30
                }
                """.formatted(PRESTADOR_ID.valor());

        mockMvc.perform(post("/api/servicos")
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(servico.id().valor().toString()))
                .andExpect(jsonPath("$.duracaoMinutos").value(60));
    }

    @Test
    void deveRetornar400QuandoDuracaoNaoPositiva() throws Exception {
        String corpo = """
                {
                    "prestadorId": "%s",
                    "nome": "Massagem relaxante",
                    "duracaoMinutos": 0,
                    "preco": 150.00,
                    "percentualSinal": 30
                }
                """.formatted(PRESTADOR_ID.valor());

        mockMvc.perform(post("/api/servicos")
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveListarServicosPorPrestadorERetornar200() throws Exception {
        Servico servico = servicoExemplo();
        given(listarServicosPorPrestadorUseCase.executar(any())).willReturn(List.of(servico));

        mockMvc.perform(get("/api/servicos").param("prestadorId", PRESTADOR_ID.valor().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(servico.id().valor().toString()));
    }
}
