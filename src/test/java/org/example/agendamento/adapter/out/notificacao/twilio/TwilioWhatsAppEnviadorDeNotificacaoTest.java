package org.example.agendamento.adapter.out.notificacao.twilio;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.example.agendamento.application.port.out.CanalNotificacao;
import org.example.agendamento.application.port.out.Notificacao;
import org.example.agendamento.config.TwilioProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

class TwilioWhatsAppEnviadorDeNotificacaoTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance().build();

    private TwilioWhatsAppEnviadorDeNotificacao adapter() {
        TwilioProperties properties = new TwilioProperties(
                wireMock.baseUrl(), "AC123", "token-secreto", "+14155550100", "+14155238886");
        return new TwilioWhatsAppEnviadorDeNotificacao(RestClient.builder(), properties);
    }

    private String campoFormEsperado(String nome, String valor) {
        return nome + "=" + URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }

    @Test
    void devePrefixarNumerosComWhatsapp() {
        wireMock.stubFor(post(urlEqualTo("/Accounts/AC123/Messages.json"))
                .withRequestBody(containing(campoFormEsperado("To", "whatsapp:+5511987654321")))
                .withRequestBody(containing(campoFormEsperado("From", "whatsapp:+14155238886")))
                .willReturn(aResponse().withStatus(201)));

        Notificacao notificacao = new Notificacao(CanalNotificacao.WHATSAPP, "11987654321",
                "Agendamento confirmado", "Seu agendamento foi confirmado!");
        adapter().enviar(notificacao);

        wireMock.verify(postRequestedFor(urlEqualTo("/Accounts/AC123/Messages.json")));
    }
}
