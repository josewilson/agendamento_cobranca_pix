package org.example.agendamento.adapter.in.web;

import org.example.agendamento.adapter.in.web.security.SecurityConfig;
import org.example.agendamento.adapter.in.web.security.TestAutenticacao;
import org.example.agendamento.application.exception.AcessoNaoAutorizadoException;
import org.example.agendamento.application.port.in.AtualizarPrestadorUseCase;
import org.example.agendamento.application.port.in.CadastrarPrestadorUseCase;
import org.example.agendamento.application.port.in.ListarPrestadoresUseCase;
import org.example.agendamento.domain.model.prestador.Prestador;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrestadorController.class)
@Import(SecurityConfig.class)
class PrestadorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CadastrarPrestadorUseCase cadastrarPrestadorUseCase;
    @MockitoBean
    private ListarPrestadoresUseCase listarPrestadoresUseCase;
    @MockitoBean
    private AtualizarPrestadorUseCase atualizarPrestadorUseCase;

    private static Prestador prestadorExemplo() {
        return new Prestador(PrestadorId.novo(), "Clinica Bem-Estar", "11987654321",
                "clinica@exemplo.com", "hash-fake-de-teste", DocumentoFiscal.cnpj("11.222.333/0001-81"),
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100)));
    }

    @Test
    void deveCadastrarPrestadorERetornar201() throws Exception {
        Prestador prestador = prestadorExemplo();
        given(cadastrarPrestadorUseCase.executar(any())).willReturn(prestador);

        String corpo = """
                {
                    "nome": "Clinica Bem-Estar",
                    "telefone": "11987654321",
                    "email": "clinica@exemplo.com",
                    "senha": "senha123",
                    "documentoNumero": "11222333000181",
                    "documentoTipo": "CNPJ"
                }
                """;

        mockMvc.perform(post("/api/prestadores")
                        .with(csrf())
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(prestador.id().valor().toString()))
                .andExpect(jsonPath("$.nome").value("Clinica Bem-Estar"))
                .andExpect(jsonPath("$.telefone").value("11987654321"))
                .andExpect(jsonPath("$.email").value("clinica@exemplo.com"))
                .andExpect(jsonPath("$.documentoTipo").value("CNPJ"));
    }

    @Test
    void deveRetornar400QuandoNomeAusente() throws Exception {
        String corpo = """
                {
                    "telefone": "11987654321",
                    "documentoNumero": "11222333000181",
                    "documentoTipo": "CNPJ"
                }
                """;

        mockMvc.perform(post("/api/prestadores")
                        .with(csrf())
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400QuandoSenhaFraca() throws Exception {
        String corpo = """
                {
                    "nome": "Clinica Bem-Estar",
                    "telefone": "11987654321",
                    "email": "clinica@exemplo.com",
                    "senha": "12345678",
                    "documentoNumero": "11222333000181",
                    "documentoTipo": "CNPJ"
                }
                """;

        mockMvc.perform(post("/api/prestadores")
                        .with(csrf())
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar409QuandoDocumentoOuEmailDuplicado() throws Exception {
        given(cadastrarPrestadorUseCase.executar(any()))
                .willThrow(new DataIntegrityViolationException("uk_prestador_documento"));

        String corpo = """
                {
                    "nome": "Clinica Bem-Estar",
                    "telefone": "11987654321",
                    "email": "clinica@exemplo.com",
                    "senha": "senha123",
                    "documentoNumero": "11222333000181",
                    "documentoTipo": "CNPJ"
                }
                """;

        mockMvc.perform(post("/api/prestadores")
                        .with(csrf())
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isConflict());
    }

    @Test
    void deveListarPrestadoresERetornar200() throws Exception {
        Prestador prestador = prestadorExemplo();
        given(listarPrestadoresUseCase.executar()).willReturn(List.of(prestador));

        mockMvc.perform(get("/api/prestadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(prestador.id().valor().toString()));
    }

    @Test
    void devePaginarQuandoPageESizeInformados() throws Exception {
        List<Prestador> prestadores = List.of(prestadorExemplo(), prestadorExemplo(), prestadorExemplo());
        given(listarPrestadoresUseCase.executar()).willReturn(prestadores);

        mockMvc.perform(get("/api/prestadores").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "3"))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deveAtualizarPrestadorERetornar200() throws Exception {
        Prestador prestador = prestadorExemplo();
        given(atualizarPrestadorUseCase.executar(any())).willReturn(prestador);

        String corpo = """
                {
                    "nome": "Clinica Bem-Estar Ltda",
                    "telefone": "11999998888"
                }
                """;

        mockMvc.perform(put("/api/prestadores/" + prestador.id().valor())
                        .with(authentication(TestAutenticacao.doPrestador(prestador.id())))
                        .with(csrf())
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(prestador.id().valor().toString()));
    }

    @Test
    void deveRetornar403QuandoEditarOutroPrestador() throws Exception {
        PrestadorId idAlvo = PrestadorId.novo();
        given(atualizarPrestadorUseCase.executar(any()))
                .willThrow(new AcessoNaoAutorizadoException("nao autorizado"));

        String corpo = """
                {
                    "nome": "Invasor",
                    "telefone": "11999998888"
                }
                """;

        mockMvc.perform(put("/api/prestadores/" + idAlvo.valor())
                        .with(authentication(TestAutenticacao.doPrestador(PrestadorId.novo())))
                        .with(csrf())
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isForbidden());
    }
}
