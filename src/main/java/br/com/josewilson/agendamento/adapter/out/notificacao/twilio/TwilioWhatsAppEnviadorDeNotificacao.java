package br.com.josewilson.agendamento.adapter.out.notificacao.twilio;

import br.com.josewilson.agendamento.application.port.out.CanalNotificacao;
import br.com.josewilson.agendamento.config.TwilioProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Profile("!dev")
public class TwilioWhatsAppEnviadorDeNotificacao extends AbstractTwilioEnviadorDeNotificacao {

    public TwilioWhatsAppEnviadorDeNotificacao(RestClient.Builder restClientBuilder, TwilioProperties properties) {
        super(restClientBuilder, properties, properties.whatsappFrom());
    }

    @Override
    protected String prefixoCanal() {
        return "whatsapp:";
    }

    @Override
    public CanalNotificacao canalSuportado() {
        return CanalNotificacao.WHATSAPP;
    }
}
