package org.example.agendamento.adapter.in.web;

import org.example.agendamento.adapter.in.web.security.SecurityConfig;
import org.example.agendamento.application.port.in.AtualizarClienteUseCase;
import org.example.agendamento.application.port.in.CadastrarClienteUseCase;
import org.example.agendamento.application.port.in.ExcluirClienteUseCase;
import org.example.agendamento.application.port.in.ListarClientesUseCase;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.shared.Contato;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
@Import(SecurityConfig.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CadastrarClienteUseCase cadastrarClienteUseCase;
    @MockitoBean
    private ListarClientesUseCase listarClientesUseCase;
    @MockitoBean
    private AtualizarClienteUseCase atualizarClienteUseCase;
    @MockitoBean
    private ExcluirClienteUseCase excluirClienteUseCase;

    @Test
    void deveCadastrarClienteERetornar201() throws Exception {
        Cliente cliente = new Cliente(ClienteId.novo(), "Maria Silva",
                new Contato("maria@exemplo.com", "11987654321"), DocumentoFiscal.cpf("111.444.777-35"));
        given(cadastrarClienteUseCase.executar(any())).willReturn(cliente);

        String corpo = """
                {
                    "nome": "Maria Silva",
                    "email": "maria@exemplo.com",
                    "telefone": "11987654321",
                    "documentoNumero": "11144477735",
                    "documentoTipo": "CPF"
                }
                """;

        mockMvc.perform(post("/api/clientes")
                        .with(csrf())
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(cliente.id().valor().toString()))
                .andExpect(jsonPath("$.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.quantidadeNoShow").value(0));
    }

    @Test
    void deveRetornar400QuandoEmailAusente() throws Exception {
        String corpo = """
                {
                    "nome": "Maria Silva",
                    "telefone": "11987654321",
                    "documentoNumero": "11144477735",
                    "documentoTipo": "CPF"
                }
                """;

        mockMvc.perform(post("/api/clientes")
                        .with(csrf())
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar409QuandoDocumentoDuplicado() throws Exception {
        given(cadastrarClienteUseCase.executar(any()))
                .willThrow(new DataIntegrityViolationException("uk_cliente_documento"));

        String corpo = """
                {
                    "nome": "Maria Silva",
                    "email": "maria@exemplo.com",
                    "telefone": "11987654321",
                    "documentoNumero": "11144477735",
                    "documentoTipo": "CPF"
                }
                """;

        mockMvc.perform(post("/api/clientes")
                        .with(csrf())
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isConflict());
    }

    @Test
    void deveListarClientesERetornar200() throws Exception {
        Cliente cliente = new Cliente(ClienteId.novo(), "Maria Silva",
                new Contato("maria@exemplo.com", "11987654321"), DocumentoFiscal.cpf("111.444.777-35"));
        given(listarClientesUseCase.executar()).willReturn(List.of(cliente));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(cliente.id().valor().toString()))
                .andExpect(jsonPath("$[0].nome").value("Maria Silva"));
    }

    @Test
    void deveAtualizarClienteERetornar200() throws Exception {
        Cliente cliente = new Cliente(ClienteId.novo(), "Maria S. Silva",
                new Contato("maria.nova@exemplo.com", "11999998888"), DocumentoFiscal.cpf("111.444.777-35"));
        given(atualizarClienteUseCase.executar(any())).willReturn(cliente);

        String corpo = """
                {
                    "nome": "Maria S. Silva",
                    "email": "maria.nova@exemplo.com",
                    "telefone": "11999998888"
                }
                """;

        mockMvc.perform(put("/api/clientes/" + cliente.id().valor())
                        .with(csrf())
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria S. Silva"));
    }

    @Test
    void deveExcluirClienteERetornar204() throws Exception {
        ClienteId id = ClienteId.novo();

        mockMvc.perform(delete("/api/clientes/" + id.valor()).with(csrf()))
                .andExpect(status().isNoContent());

        verify(excluirClienteUseCase).executar(any());
    }
}
