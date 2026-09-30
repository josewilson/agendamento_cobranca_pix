package org.example.agendamento.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "twilio")
public record TwilioProperties(String baseUrl, String accountSid, String authToken, String smsFrom, String whatsappFrom) {
}
