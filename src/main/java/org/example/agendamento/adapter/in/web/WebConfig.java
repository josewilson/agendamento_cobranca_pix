package org.example.agendamento.adapter.in.web;

import org.example.agendamento.config.CorsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Libera o frontend separado (React/Vite, outra porta) para chamar a API. Sem isso o
 * browser bloqueia as requisicoes por CORS antes mesmo de chegarem aos controllers.
 * {@code @EnableConfigurationProperties} aqui (em vez de depender so do
 * {@code @ConfigurationPropertiesScan} da aplicacao) garante que {@link CorsProperties}
 * tambem fica disponivel quando o Spring sobe so a fatia de web MVC em {@code @WebMvcTest}
 * — que inclui todo {@code WebMvcConfigurer} do classpath, mas nao faz component scan geral.
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class WebConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    public WebConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(corsProperties.allowedOrigins().toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
