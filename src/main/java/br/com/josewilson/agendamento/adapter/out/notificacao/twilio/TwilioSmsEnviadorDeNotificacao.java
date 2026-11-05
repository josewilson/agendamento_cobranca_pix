package br.com.josewilson.agendamento.adapter.out.notificacao.twilio;

import br.com.josewilson.agendamento.application.port.out.CanalNotificacao;
import br.com.josewilson.agendamento.config.TwilioProperties;
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
