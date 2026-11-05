package br.com.josewilson.agendamento.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "asaas")
public record AsaasProperties(String baseUrl, String apiKey, String webhookToken) {
}
