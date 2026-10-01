package org.example.agendamento.adapter.in.web;

import org.example.agendamento.application.port.in.CadastrarPrestadorUseCase;
import org.example.agendamento.application.port.in.ListarPrestadoresUseCase;
import org.example.agendamento.domain.model.prestador.Prestador;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;
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

@WebMvcTest(PrestadorController.class)
class PrestadorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CadastrarPrestadorUseCase cadastrarPrestadorUseCase;
    @MockitoBean
    private ListarPrestadoresUseCase listarPrestadoresUseCase;

    private static Prestador prestadorExemplo() {
        return new Prestador(PrestadorId.novo(), "Clinica Bem-Estar",
                DocumentoFiscal.cnpj("11.222.333/0001-81"),
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100)));
    }

    @Test
    void deveCadastrarPrestadorERetornar201() throws Exception {
        Prestador prestador = prestadorExemplo();
        given(cadastrarPrestadorUseCase.executar(any())).willReturn(prestador);

        String corpo = """
                {
                    "nome": "Clinica Bem-Estar",
                    "documentoNumero": "11222333000181",
                    "documentoTipo": "CNPJ"
                }
                """;

        mockMvc.perform(post("/api/prestadores")
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(prestador.id().valor().toString()))
                .andExpect(jsonPath("$.nome").value("Clinica Bem-Estar"))
                .andExpect(jsonPath("$.documentoTipo").value("CNPJ"));
    }

    @Test
    void deveRetornar400QuandoNomeAusente() throws Exception {
        String corpo = """
                {
                    "documentoNumero": "11222333000181",
                    "documentoTipo": "CNPJ"
                }
                """;

        mockMvc.perform(post("/api/prestadores")
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveListarPrestadoresERetornar200() throws Exception {
        Prestador prestador = prestadorExemplo();
        given(listarPrestadoresUseCase.executar()).willReturn(List.of(prestador));

        mockMvc.perform(get("/api/prestadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(prestador.id().valor().toString()));
    }
}
