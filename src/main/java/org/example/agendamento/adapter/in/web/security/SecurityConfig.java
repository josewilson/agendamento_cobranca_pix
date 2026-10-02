package org.example.agendamento.adapter.in.web.security;

import org.example.agendamento.config.CorsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Login simples de Prestador (sessao via cookie) — ver CLAUDE.md. Cliente continua sem login
 * (decisao explicita de escopo): o fluxo publico de reserva (listar prestador/servico, criar
 * agendamento, pagar, cancelar, marcar no-show) fica liberado; so a area de gestao do prestador
 * (agenda propria, cadastro/edicao de servico) exige autenticacao.
 *
 * <p>CORS vive só aqui (ver {@link #corsConfigurationSource()}) — {@code WebConfig} foi removido
 * nesta sessão porque a config de CORS estava duplicada entre o {@code WebMvcConfigurer} dele e
 * esse bean (achado da auditoria de 01/10/2026): todo {@code @WebMvcTest} do projeto já importa
 * {@code SecurityConfig} e, em produção, o Security governa toda a cadeia de filtros de qualquer
 * forma, então o `addCorsMappings` do Spring MVC nunca era o código que realmente decidia CORS —
 * só existia uma segunda lista de origens/métodos pra divergir da primeira em silêncio.
 * {@code @EnableConfigurationProperties(CorsProperties.class)} migrou pra cá pelo mesmo motivo.
 *
 * <p><b>CSRF</b>: habilitado via {@link CookieCsrfTokenRepository} (cookie {@code XSRF-TOKEN},
 * não {@code HttpOnly} — o frontend precisa ler e reenviar como header {@code X-XSRF-TOKEN}, ver
 * {@code frontend/src/api/client.js}), exceto para {@code /api/webhooks/**}: Asaas/Mercado Pago
 * chamam esses endpoints direto, sem navegador nem cookie de sessão, então não têm como carregar
 * nem reenviar um token CSRF — a autenticidade desses dois já é garantida por outro mecanismo
 * (header de token/assinatura HMAC validado nos próprios controllers). {@link CsrfCookieFilter}
 * força o cookie a ser gravado em toda requisição, inclusive GET — sem ele o Spring Security só
 * grava o cookie na primeira vez que algo lê o token, o que nunca aconteceria sozinho numa API
 * sem views server-side.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(CorsProperties.class)
public class SecurityConfig {

    private final CorsProperties corsProperties;

    public SecurityConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(corsProperties.allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("X-XSRF-TOKEN"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> response.sendError(HttpStatus.UNAUTHORIZED.value(), "nao autenticado");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/api/webhooks/**"))
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e.authenticationEntryPoint(authenticationEntryPoint()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/prestadores").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/prestadores").permitAll()
                        .requestMatchers("/api/clientes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/servicos").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/agendamentos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/agendamentos/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/agendamentos/*/cancelar").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/agendamentos/*/no-show").permitAll()
                        .requestMatchers("/api/webhooks/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
