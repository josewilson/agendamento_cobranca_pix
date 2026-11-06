package br.com.josewilson.agendamento.adapter.in.web;

import br.com.josewilson.agendamento.adapter.in.web.security.LoginRateLimiter;
import br.com.josewilson.agendamento.adapter.in.web.security.SecurityConfig;
import br.com.josewilson.agendamento.adapter.in.web.security.TestAutenticacao;
import br.com.josewilson.agendamento.application.port.out.ClienteRepository;
import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;
import br.com.josewilson.agendamento.domain.model.shared.Contato;
import br.com.josewilson.agendamento.domain.model.shared.DocumentoFiscal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ClienteAuthController.class, AuthController.class})
@Import(SecurityConfig.class)
class ClienteAuthControllerTest {

    private static final String CORPO = "{\"email\": \"maria@exemplo.com\", \"senha\": \"senha1234\"}";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private ClienteRepository clienteRepository;
    @MockitoBean
    private LoginRateLimiter loginRateLimiter;
    @MockitoBean
    private AuthenticationManager authenticationManager;

    private Cliente clienteComSenha() {
        Cliente cliente = new Cliente(ClienteId.novo(), "Maria Silva",
                new Contato("maria@exemplo.com", "11987654321"), DocumentoFiscal.cpf("111.444.777-35"));
        cliente.definirSenhaHash(passwordEncoder.encode("senha1234"));
        return cliente;
    }

    @Test
    void deveLogarClienteERetornarTipoCliente() throws Exception {
        Cliente cliente = clienteComSenha();
        given(clienteRepository.buscarPorEmail("maria@exemplo.com")).willReturn(Optional.of(cliente));

        mockMvc.perform(post("/api/auth/cliente/login").with(csrf()).contentType("application/json").content(CORPO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("CLIENTE"))
                .andExpect(jsonPath("$.clienteId").value(cliente.id().valor().toString()))
                .andExpect(jsonPath("$.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
        verify(loginRateLimiter).registrarSucesso("cliente:maria@exemplo.com");
    }

    @Test
    void deveRetornar401ParaSenhaErradaEContarFalha() throws Exception {
        given(clienteRepository.buscarPorEmail("maria@exemplo.com")).willReturn(Optional.of(clienteComSenha()));

        mockMvc.perform(post("/api/auth/cliente/login").with(csrf()).contentType("application/json")
                        .content("{\"email\": \"maria@exemplo.com\", \"senha\": \"errada1234\"}"))
                .andExpect(status().isUnauthorized());
        verify(loginRateLimiter).registrarFalha("cliente:maria@exemplo.com");
    }

    @Test
    void deveRetornar401ParaEmailSemConta() throws Exception {
        given(clienteRepository.buscarPorEmail("maria@exemplo.com")).willReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/cliente/login").with(csrf()).contentType("application/json").content(CORPO))
                .andExpect(status().isUnauthorized());
        verify(loginRateLimiter).registrarFalha("cliente:maria@exemplo.com");
    }

    @Test
    void deveRetornar429QuandoBloqueado() throws Exception {
        given(loginRateLimiter.bloqueado("cliente:maria@exemplo.com")).willReturn(true);

        mockMvc.perform(post("/api/auth/cliente/login").with(csrf()).contentType("application/json").content(CORPO))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void meDevolveTipoClienteParaSessaoDeCliente() throws Exception {
        ClienteId id = ClienteId.novo();

        mockMvc.perform(get("/api/auth/me").with(authentication(TestAutenticacao.doCliente(id))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("CLIENTE"))
                .andExpect(jsonPath("$.clienteId").value(id.valor().toString()));
    }
}
