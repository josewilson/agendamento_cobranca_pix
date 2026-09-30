package org.example.agendamento.adapter.out.notificacao.twilio;

import org.example.agendamento.application.port.out.CanalNotificacao;
import org.example.agendamento.config.TwilioProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Profile("!dev")
public class TwilioSmsEnviadorDeNotificacao extends AbstractTwilioEnviadorDeNotificacao {

    public TwilioSmsEnviadorDeNotificacao(RestClient.Builder restClientBuilder, TwilioProperties properties) {
        super(restClientBuilder, properties, properties.smsFrom());
    }

    @Override
    protected String prefixoCanal() {
        return "";
    }

    @Override
    public CanalNotificacao canalSuportado() {
        return CanalNotificacao.SMS;
    }
}
