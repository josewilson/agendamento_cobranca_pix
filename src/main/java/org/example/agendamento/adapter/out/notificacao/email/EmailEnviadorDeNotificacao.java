package org.example.agendamento.adapter.out.notificacao.email;

import org.example.agendamento.application.port.out.CanalNotificacao;
import org.example.agendamento.application.port.out.EnviadorDeNotificacao;
import org.example.agendamento.application.port.out.Notificacao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@Profile("!dev")
public class EmailEnviadorDeNotificacao implements EnviadorDeNotificacao {

    private final JavaMailSender mailSender;
    private final String remetente;

    public EmailEnviadorDeNotificacao(JavaMailSender mailSender,
                                       @Value("${notificacao.email.remetente}") String remetente) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    @Override
    public void enviar(Notificacao notificacao) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(notificacao.destinatario());
        mensagem.setSubject(notificacao.assunto());
        mensagem.setText(notificacao.mensagem());
        mailSender.send(mensagem);
    }

    @Override
    public CanalNotificacao canalSuportado() {
        return CanalNotificacao.EMAIL;
    }
}
