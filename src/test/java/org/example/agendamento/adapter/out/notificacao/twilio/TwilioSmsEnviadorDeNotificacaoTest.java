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
import java.util.Base64;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

class TwilioSmsEnviadorDeNotificacaoTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance().build();

    private TwilioSmsEnviadorDeNotificacao adapter() {
        TwilioProperties properties = new TwilioProperties(
                wireMock.baseUrl(), "AC123", "token-secreto", "+14155550100", "+14155238886");
        return new TwilioSmsEnviadorDeNotificacao(RestClient.builder(), properties);
    }

    private String autenticacaoEsperada() {
        return "Basic " + Base64.getEncoder().encodeToString("AC123:token-secreto".getBytes(StandardCharsets.UTF_8));
    }

    private String campoFormEsperado(String nome, String valor) {
        return nome + "=" + URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }

    @Test
    void deveEnviarSmsComDddBrasileiroEAutenticacaoBasica() {
        wireMock.stubFor(post(urlEqualTo("/Accounts/AC123/Messages.json"))
                .withHeader("Authorization", equalTo(autenticacaoEsperada()))
                .withRequestBody(containing(campoFormEsperado("To", "+5511987654321")))
                .withRequestBody(containing(campoFormEsperado("From", "+14155550100")))
                .willReturn(aResponse().withStatus(201).withHeader("Content-Type", "application/json").withBody("{}")));

        Notificacao notificacao = new Notificacao(CanalNotificacao.SMS, "11987654321",
                "Agendamento confirmado", "Seu agendamento foi confirmado!");
        adapter().enviar(notificacao);

        wireMock.verify(postRequestedFor(urlEqualTo("/Accounts/AC123/Messages.json")));
    }

    @Test
    void naoDeveAlterarNumeroQueJaTemCodigoDoPais() {
        wireMock.stubFor(post(urlEqualTo("/Accounts/AC123/Messages.json"))
                .withRequestBody(containing(campoFormEsperado("To", "+16285550100")))
                .willReturn(aResponse().withStatus(201)));

        Notificacao notificacao = new Notificacao(CanalNotificacao.SMS, "+16285550100", "Assunto", "Mensagem");
        adapter().enviar(notificacao);

        wireMock.verify(postRequestedFor(urlEqualTo("/Accounts/AC123/Messages.json")));
    }
}
