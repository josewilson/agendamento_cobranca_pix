package org.example.agendamento.adapter.out.notificacao.twilio;

import org.example.agendamento.application.port.out.EnviadorDeNotificacao;
import org.example.agendamento.application.port.out.Notificacao;
import org.example.agendamento.config.TwilioProperties;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base comum para os enviadores via Twilio (SMS e WhatsApp usam exatamente a mesma API de
 * mensagens — https://www.twilio.com/docs/sms/api/message — a unica diferenca e o prefixo
 * "whatsapp:" nos numeros De/Para). Nao e um @Component: as subclasses concretas sao os
 * beans, cada uma respondendo por um CanalNotificacao diferente.
 */
abstract class AbstractTwilioEnviadorDeNotificacao implements EnviadorDeNotificacao {

    private static final String DDI_PADRAO = "+55";

    private final RestClient restClient;
    private final String accountSid;
    private final String numeroRemetente;

    protected AbstractTwilioEnviadorDeNotificacao(RestClient.Builder restClientBuilder, TwilioProperties properties,
                                                    String numeroRemetente) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        String credenciais = Base64.getEncoder()
                .encodeToString((properties.accountSid() + ":" + properties.authToken()).getBytes(StandardCharsets.UTF_8));

        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .defaultHeader("Authorization", "Basic " + credenciais)
                .build();
        this.accountSid = properties.accountSid();
        this.numeroRemetente = numeroRemetente;
    }

    @Override
    public void enviar(Notificacao notificacao) {
        MultiValueMap<String, String> corpo = new LinkedMultiValueMap<>();
        corpo.add("To", prefixoCanal() + formatarE164(notificacao.destinatario()));
        corpo.add("From", prefixoCanal() + numeroRemetente);
        corpo.add("Body", notificacao.assunto() + ": " + notificacao.mensagem());

        restClient.post()
                .uri("/Accounts/{accountSid}/Messages.json", accountSid)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(corpo)
                .retrieve()
                .toBodilessEntity();
    }

    private static String formatarE164(String telefone) {
        return telefone.startsWith("+") ? telefone : DDI_PADRAO + telefone;
    }

    protected abstract String prefixoCanal();
}
