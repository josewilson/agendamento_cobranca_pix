package org.example.agendamento.adapter.in.web.security;

import org.example.agendamento.config.CorsProperties;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Login simples de Prestador (sessao via cookie) — ver CLAUDE.md. Cliente continua sem login
 * (decisao explicita de escopo): o fluxo publico de reserva (listar prestador/servico, criar
 * agendamento, pagar, cancelar, marcar no-show) fica liberado; so a area de gestao do prestador
 * (agenda propria, cadastro de servico) exige autenticacao. CORS tem um {@link CorsConfigurationSource}
 * proprio aqui (reaproveitando {@link CorsProperties}, ja registrada por
 * {@link org.example.agendamento.adapter.in.web.WebConfig}) em vez de depender do
 * HandlerMappingIntrospector do Spring MVC — os testes de persistencia (*RepositoryAdapterIT) sobem
 * o contexto com {@code web-application-type=none}, sem MVC nenhum, e {@code .cors(withDefaults())}
 * sem essa fonte propria falha com NoSuchBeanDefinitionException nesse cenario.
 */
@Configuration
@EnableWebSecurity
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
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e.authenticationEntryPoint(authenticationEntryPoint()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
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
