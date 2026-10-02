package org.example.agendamento.adapter.in.web;

import org.example.agendamento.adapter.in.web.security.LoginRateLimiter;
import org.example.agendamento.adapter.in.web.security.PrestadorPrincipal;
import org.example.agendamento.adapter.in.web.security.SecurityConfig;
import org.example.agendamento.domain.model.prestador.Prestador;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private LoginRateLimiter loginRateLimiter;

    private static final String CORPO_LOGIN = """
            {
                "email": "prestador@exemplo.com",
                "senha": "senha123"
            }
            """;

    private static Prestador prestadorExemplo() {
        return new Prestador(PrestadorId.novo(), "Clinica Bem-Estar", "11987654321",
                "prestador@exemplo.com", "hash-fake-de-teste", DocumentoFiscal.cnpj("11.222.333/0001-81"),
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100)));
    }

    @Test
    void deveFazerLoginERetornarDadosDoPrestador() throws Exception {
        given(loginRateLimiter.bloqueado("prestador@exemplo.com")).willReturn(false);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new PrestadorPrincipal(prestadorExemplo()), null);
        given(authenticationManager.authenticate(any())).willReturn(authentication);

        mockMvc.perform(post("/api/auth/login").with(csrf())
                        .contentType("application/json")
                        .content(CORPO_LOGIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Clinica Bem-Estar"));

        then(loginRateLimiter).should().registrarSucesso("prestador@exemplo.com");
    }

    @Test
    void deveRegistrarFalhaQuandoCredencialInvalida() throws Exception {
        given(loginRateLimiter.bloqueado("prestador@exemplo.com")).willReturn(false);
        willThrow(new BadCredentialsException("credenciais invalidas"))
                .given(authenticationManager).authenticate(any());

        mockMvc.perform(post("/api/auth/login").with(csrf())
                        .contentType("application/json")
                        .content(CORPO_LOGIN))
                .andExpect(status().isUnauthorized());

        then(loginRateLimiter).should().registrarFalha("prestador@exemplo.com");
        then(loginRateLimiter).should(org.mockito.Mockito.never()).registrarSucesso(any());
    }

    @Test
    void deveRetornar429QuandoEmailEstaBloqueado() throws Exception {
        given(loginRateLimiter.bloqueado("prestador@exemplo.com")).willReturn(true);

        mockMvc.perform(post("/api/auth/login").with(csrf())
                        .contentType("application/json")
                        .content(CORPO_LOGIN))
                .andExpect(status().isTooManyRequests());

        then(authenticationManager).shouldHaveNoInteractions();
    }
}
