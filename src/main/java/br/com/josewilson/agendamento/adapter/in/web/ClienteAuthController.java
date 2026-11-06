package br.com.josewilson.agendamento.adapter.in.web;

import br.com.josewilson.agendamento.adapter.in.web.security.ClientePrincipal;
import br.com.josewilson.agendamento.adapter.in.web.security.LoginBloqueadoException;
import br.com.josewilson.agendamento.adapter.in.web.security.LoginRateLimiter;
import br.com.josewilson.agendamento.application.port.out.ClienteRepository;
import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login de Cliente (mesmo mecanismo de sessao do Prestador, ver AuthController). Fica separado do
 * AuthenticationManager do Prestador de proposito: sao dois tipos de usuario com tabelas e papeis
 * (ROLE_CLIENTE/ROLE_PRESTADOR) distintos, e um unico UserDetailsService nao saberia qual procurar.
 * Logout e /me sao os mesmos endpoints de /api/auth.
 */
@RestController
@RequestMapping("/api/auth/cliente")
public class ClienteAuthController {

    // BCrypt de uma senha qualquer: comparado quando o email nao existe, para a resposta demorar o
    // mesmo e nao revelar (por tempo) quais emails tem conta.
    private static final String HASH_FALSO = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimiter loginRateLimiter;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public ClienteAuthController(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder,
                                 LoginRateLimiter loginRateLimiter) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/login")
    public AuthController.SessaoResponse login(@Valid @RequestBody AuthController.LoginRequest request,
                                               HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        // prefixo separa o contador de falhas de clientes do de prestadores com o mesmo email
        String chave = "cliente:" + request.email();
        if (loginRateLimiter.bloqueado(chave)) {
            throw new LoginBloqueadoException("Muitas tentativas de login. Tente novamente em alguns minutos.");
        }

        Cliente cliente = clienteRepository.buscarPorEmail(request.email()).orElse(null);
        String hash = cliente != null ? cliente.senhaHash() : HASH_FALSO;
        boolean senhaConfere = passwordEncoder.matches(request.senha(), hash);
        if (cliente == null || !senhaConfere) {
            loginRateLimiter.registrarFalha(chave);
            throw new BadCredentialsException("credenciais invalidas");
        }
        loginRateLimiter.registrarSucesso(chave);

        ClientePrincipal principal = new ClientePrincipal(cliente);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.authorities()));
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return AuthController.SessaoResponse.cliente(principal);
    }
}
