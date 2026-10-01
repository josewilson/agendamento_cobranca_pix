package org.example.agendamento.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.example.agendamento.adapter.in.web.security.PrestadorPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public PrestadorLogadoResponse login(@Valid @RequestBody LoginRequest request,
                                          HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return PrestadorLogadoResponse.de((PrestadorPrincipal) authentication.getPrincipal());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest httpRequest) {
        httpRequest.getSession().invalidate();
        SecurityContextHolder.clearContext();
    }

    @GetMapping("/me")
    public PrestadorLogadoResponse me(@AuthenticationPrincipal PrestadorPrincipal principal) {
        return PrestadorLogadoResponse.de(principal);
    }

    public record LoginRequest(@NotBlank String email, @NotBlank String senha) {
    }

    public record PrestadorLogadoResponse(java.util.UUID prestadorId, String nome) {
        public static PrestadorLogadoResponse de(PrestadorPrincipal principal) {
            return new PrestadorLogadoResponse(principal.prestadorId().valor(), principal.nome());
        }
    }
}
