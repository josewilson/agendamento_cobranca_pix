package org.example.agendamento.adapter.out.notificacao.email;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.example.agendamento.application.port.out.CanalNotificacao;
import org.example.agendamento.application.port.out.Notificacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.assertj.core.api.Assertions.assertThat;

class EmailEnviadorDeNotificacaoTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

    private EmailEnviadorDeNotificacao enviador() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(ServerSetupTest.SMTP.getPort());
        return new EmailEnviadorDeNotificacao(mailSender, "no-reply@agendamento.local");
    }

    @Test
    void deveEnviarEmailComRemetenteAssuntoEMensagem() throws Exception {
        Notificacao notificacao = new Notificacao(CanalNotificacao.EMAIL, "cliente@exemplo.com",
                "Agendamento confirmado", "Seu agendamento foi confirmado!");

        enviador().enviar(notificacao);

        MimeMessage[] mensagensRecebidas = greenMail.getReceivedMessages();
        assertThat(mensagensRecebidas).hasSize(1);
        assertThat(mensagensRecebidas[0].getSubject()).isEqualTo("Agendamento confirmado");
        assertThat(mensagensRecebidas[0].getFrom()[0].toString()).isEqualTo("no-reply@agendamento.local");
        assertThat(mensagensRecebidas[0].getAllRecipients()[0].toString()).isEqualTo("cliente@exemplo.com");
        assertThat(GreenMailUtil.getBody(mensagensRecebidas[0])).contains("Seu agendamento foi confirmado!");
    }

    @Test
    void canalSuportadoDeveSerEmail() {
        assertThat(enviador().canalSuportado()).isEqualTo(CanalNotificacao.EMAIL);
    }
}
