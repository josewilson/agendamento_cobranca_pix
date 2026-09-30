package org.example.agendamento.application.service;

import org.example.agendamento.application.port.out.CanalNotificacao;
import org.example.agendamento.application.port.out.EnviadorDeNotificacao;
import org.example.agendamento.application.port.out.Notificacao;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificacaoDispatcherTest {

    private static class EnviadorFake implements EnviadorDeNotificacao {
        private final CanalNotificacao canal;
        private Notificacao ultimaEnviada;

        EnviadorFake(CanalNotificacao canal) {
            this.canal = canal;
        }

        @Override
        public void enviar(Notificacao notificacao) {
            this.ultimaEnviada = notificacao;
        }

        @Override
        public CanalNotificacao canalSuportado() {
            return canal;
        }
    }

    @Test
    void deveRotearParaOEnviadorDoCanalCorreto() {
        EnviadorFake email = new EnviadorFake(CanalNotificacao.EMAIL);
        EnviadorFake sms = new EnviadorFake(CanalNotificacao.SMS);
        NotificacaoDispatcher dispatcher = new NotificacaoDispatcher(List.of(email, sms));

        Notificacao notificacao = new Notificacao(CanalNotificacao.SMS, "11999999999", "Assunto", "Mensagem");
        dispatcher.enviar(notificacao);

        assertThat(sms.ultimaEnviada).isEqualTo(notificacao);
        assertThat(email.ultimaEnviada).isNull();
    }

    @Test
    void deveLancarExcecaoQuandoNaoHaEnviadorParaOCanal() {
        NotificacaoDispatcher dispatcher = new NotificacaoDispatcher(List.of());

        Notificacao notificacao = new Notificacao(CanalNotificacao.WHATSAPP, "11999999999", "Assunto", "Mensagem");

        assertThatThrownBy(() -> dispatcher.enviar(notificacao))
                .isInstanceOf(IllegalStateException.class);
    }
}
