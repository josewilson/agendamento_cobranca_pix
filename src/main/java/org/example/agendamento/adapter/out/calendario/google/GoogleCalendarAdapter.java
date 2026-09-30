package org.example.agendamento.adapter.out.calendario.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.example.agendamento.application.port.out.CalendarioExternoPort;
import org.example.agendamento.config.GoogleCalendarProperties;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.shared.Periodo;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Integra com a API real do Google Calendar (https://developers.google.com/calendar/api)
 * para manter a agenda do prestador sincronizada. Autentica via conta de servico (service
 * account) — nao exige um fluxo de consentimento OAuth2 interativo, so que o e-mail da
 * conta de servico tenha sido convidado como colaborador do calendario de destino.
 * O id do evento no Google e derivado deterministicamente do agendamentoId (hexadecimal do
 * UUID, sem hifens — alfabeto compativel com o exigido pela API), entao nao precisamos
 * manter um mapeamento a parte entre agendamento e evento externo.
 */
@Component
@Profile("!dev")
public class GoogleCalendarAdapter implements CalendarioExternoPort {

    private static final ZoneId ZONA_BRASIL = ZoneId.of("America/Sao_Paulo");
    private static final List<String> ESCOPO = List.of("https://www.googleapis.com/auth/calendar.events");

    private final RestClient restClient;
    private final GoogleCalendarProperties properties;
    private volatile ServiceAccountCredentials credenciais;

    public GoogleCalendarAdapter(RestClient.Builder restClientBuilder, GoogleCalendarProperties properties) {
        // Mesmo workaround usado nos gateways de pagamento: forca HTTP/1.1 porque o
        // HttpClient da JDK tenta negociar HTTP/2 por padrao, o que quebra contra WireMock.
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
        this.properties = properties;
    }

    @Override
    public void sincronizarEvento(AgendamentoId agendamentoId, String titulo, String descricao, Periodo periodo) {
        EventoRequest request = new EventoRequest(
                idDoEvento(agendamentoId),
                titulo,
                descricao,
                new DataHora(formatar(periodo.inicio())),
                new DataHora(formatar(periodo.fim())));

        restClient.post()
                .uri("/calendars/{calendarId}/events", properties.calendarId())
                .header("Authorization", "Bearer " + tokenDeAcesso())
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void removerEvento(AgendamentoId agendamentoId) {
        restClient.delete()
                .uri("/calendars/{calendarId}/events/{eventId}", properties.calendarId(), idDoEvento(agendamentoId))
                .header("Authorization", "Bearer " + tokenDeAcesso())
                .retrieve()
                .toBodilessEntity();
    }

    private String idDoEvento(AgendamentoId agendamentoId) {
        return agendamentoId.valor().toString().replace("-", "");
    }

    private String formatar(Instant instante) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(instante.atZone(ZONA_BRASIL));
    }

    private String tokenDeAcesso() {
        try {
            ServiceAccountCredentials credenciaisCarregadas = credenciais();
            credenciaisCarregadas.refreshIfExpired();
            return credenciaisCarregadas.getAccessToken().getTokenValue();
        } catch (IOException e) {
            throw new IllegalStateException("falha ao obter token de acesso do Google Calendar", e);
        }
    }

    private ServiceAccountCredentials credenciais() throws IOException {
        ServiceAccountCredentials atuais = this.credenciais;
        if (atuais != null) {
            return atuais;
        }
        synchronized (this) {
            if (this.credenciais == null) {
                ServiceAccountCredentials carregadas = ServiceAccountCredentials.fromStream(
                        new ByteArrayInputStream(properties.credenciaisJson().getBytes(StandardCharsets.UTF_8)));
                this.credenciais = carregadas.toBuilder()
                        .setScopes(ESCOPO)
                        .setTokenServerUri(URI.create(properties.tokenServerUri()))
                        .build();
            }
            return this.credenciais;
        }
    }

    private record EventoRequest(String id, String summary, String description, DataHora start, DataHora end) {
    }

    private record DataHora(@JsonProperty("dateTime") String dateTime) {
    }
}
