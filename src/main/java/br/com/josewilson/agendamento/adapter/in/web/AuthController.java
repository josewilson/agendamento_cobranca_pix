package br.com.josewilson.agendamento.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import br.com.josewilson.agendamento.adapter.in.web.security.ClientePrincipal;
import br.com.josewilson.agendamento.adapter.in.web.security.LoginBloqueadoException;
import br.com.josewilson.agendamento.adapter.in.web.security.LoginRateLimiter;
import br.com.josewilson.agendamento.adapter.in.web.security.PrestadorPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login simples de Prestador (sessao via cookie), nao JWT nem form-login padrao do Spring
 * Security (que espera corpo form-urlencoded e redireciona — ruim para uma API JSON consumida
 * por um frontend separado). Autentica explicitamente via {@link AuthenticationManager} e grava
 * o {@link SecurityContext} na sessao HTTP via {@link HttpSessionSecurityContextRepository} —
 * o mesmo mecanismo que o filtro padrao do Spring Security usaria, só disparado manualmente
 * para controlar o formato JSON da resposta.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final LoginRateLimiter loginRateLimiter;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(AuthenticationManager authenticationManager, LoginRateLimiter loginRateLimiter) {
        this.authenticationManager = authenticationManager;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/login")
    public SessaoResponse login(@Valid @RequestBody LoginRequest request,
                                          HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        if (loginRateLimiter.bloqueado(request.email())) {
            throw new LoginBloqueadoException("Muitas tentativas de login. Tente novamente em alguns minutos.");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha()));
        } catch (AuthenticationException ex) {
            loginRateLimiter.registrarFalha(request.email());
            throw ex;
        }
        loginRateLimiter.registrarSucesso(request.email());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return SessaoResponse.prestador((PrestadorPrincipal) authentication.getPrincipal());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest httpRequest) {
        httpRequest.getSession().invalidate();
        SecurityContextHolder.clearContext();
    }

    @GetMapping("/me")
    public SessaoResponse me(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof ClientePrincipal cliente) {
            return SessaoResponse.cliente(cliente);
        }
        return SessaoResponse.prestador((PrestadorPrincipal) principal);
    }

    public record LoginRequest(@NotBlank String email, @NotBlank String senha) {
    }

    /** tipo = PRESTADOR (prestadorId preenchido) ou CLIENTE (clienteId preenchido). */
    public record SessaoResponse(String tipo, java.util.UUID prestadorId, java.util.UUID clienteId, String nome) {
        public static SessaoResponse prestador(PrestadorPrincipal principal) {
            return new SessaoResponse("PRESTADOR", principal.prestadorId().valor(), null, principal.nome());
        }

        public static SessaoResponse cliente(ClientePrincipal principal) {
            return new SessaoResponse("CLIENTE", null, principal.clienteId().valor(), principal.nome());
        }
    }
}
