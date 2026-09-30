package org.example.agendamento.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google-calendar")
public record GoogleCalendarProperties(String baseUrl, String calendarId, String credenciaisJson, String tokenServerUri) {
}
