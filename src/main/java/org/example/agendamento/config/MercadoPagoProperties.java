package org.example.agendamento.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mercadopago")
public record MercadoPagoProperties(String baseUrl, String accessToken, String webhookSecret) {
}
