package org.example.agendamento.adapter.in.web;

import org.example.agendamento.application.port.in.CadastrarClienteUseCase;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.shared.Contato;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CadastrarClienteUseCase cadastrarClienteUseCase;

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
                        .contentType("application/json")
                        .content(corpo))
                .andExpect(status().isBadRequest());
    }
}
