package org.example.agendamento.adapter.out.calendario.google;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.example.agendamento.config.GoogleCalendarProperties;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.shared.Periodo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.RestClient;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * O token server (oauth2.googleapis.com/token) e redirecionado para o proprio WireMock via
 * GoogleCalendarProperties.tokenServerUri: a biblioteca google-auth-library assina um JWT
 * localmente com a chave privada de teste gerada em gerarCredenciaisDeTeste() e troca esse
 * JWT por um access token nesse endpoint — o WireMock nunca valida a assinatura, so
 * responde com um token fixo, entao uma chave RSA descartavel gerada em memoria basta.
 */
class GoogleCalendarAdapterTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance().build();

    private static String credenciaisJson;

    @BeforeAll
    static void gerarCredenciaisDeTeste() throws Exception {
        KeyPairGenerator geradorDeChaves = KeyPairGenerator.getInstance("RSA");
        geradorDeChaves.initialize(2048);
        KeyPair par = geradorDeChaves.generateKeyPair();
        String chavePrivadaPem = "-----BEGIN PRIVATE KEY-----\\n"
                + Base64.getEncoder().encodeToString(par.getPrivate().getEncoded())
                + "\\n-----END PRIVATE KEY-----\\n";

        credenciaisJson = """
                {
                  "type": "service_account",
                  "project_id": "projeto-teste",
                  "private_key_id": "chave-teste",
                  "private_key": "%s",
                  "client_email": "conta-servico@projeto-teste.iam.gserviceaccount.com",
                  "client_id": "123456789"
                }
                """.formatted(chavePrivadaPem);
    }

    private void stubTokenServer() {
        wireMock.stubFor(post(urlEqualTo("/token"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("""
                                { "access_token": "token-de-acesso-teste", "expires_in": 3599, "token_type": "Bearer" }
                                """)));
    }

    private GoogleCalendarAdapter adapter() {
        GoogleCalendarProperties properties = new GoogleCalendarProperties(
                wireMock.baseUrl(), "calendario-teste", credenciaisJson, wireMock.baseUrl() + "/token");
        return new GoogleCalendarAdapter(RestClient.builder(), properties);
    }

    private Periodo periodoExemplo() {
        Instant agora = Instant.now();
        return new Periodo(agora.plus(Duration.ofDays(1)), agora.plus(Duration.ofDays(1)).plus(Duration.ofMinutes(60)));
    }

    @Test
    void deveCriarEventoComIdDerivadoDoAgendamentoId() {
        stubTokenServer();
        AgendamentoId agendamentoId = AgendamentoId.de("11111111-2222-3333-4444-555555555555");
        String idEventoEsperado = "11111111222233334444555555555555";

        wireMock.stubFor(post(urlEqualTo("/calendars/calendario-teste/events"))
                .withRequestBody(matchingJsonPath("$.id", equalTo(idEventoEsperado)))
                .withRequestBody(matchingJsonPath("$.summary", equalTo("Massagem - Maria Silva")))
                .willReturn(aResponse().withStatus(200)));

        adapter().sincronizarEvento(agendamentoId, "Massagem - Maria Silva", "descricao", periodoExemplo());

        wireMock.verify(postRequestedFor(urlEqualTo("/calendars/calendario-teste/events"))
                .withHeader("Authorization", equalTo("Bearer token-de-acesso-teste")));
    }

    @Test
    void deveRemoverEventoPeloIdDerivadoDoAgendamentoId() {
        stubTokenServer();
        AgendamentoId agendamentoId = AgendamentoId.de("11111111-2222-3333-4444-555555555555");
        String idEventoEsperado = "11111111222233334444555555555555";

        wireMock.stubFor(delete(urlEqualTo("/calendars/calendario-teste/events/" + idEventoEsperado))
                .willReturn(aResponse().withStatus(204)));

        adapter().removerEvento(agendamentoId);

        wireMock.verify(deleteRequestedFor(urlEqualTo("/calendars/calendario-teste/events/" + idEventoEsperado))
                .withHeader("Authorization", equalTo("Bearer token-de-acesso-teste")));
    }
}
