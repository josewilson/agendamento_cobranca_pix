package org.example.agendamento.application.port.out;

public interface EnviadorDeNotificacao {

    void enviar(Notificacao notificacao);

    CanalNotificacao canalSuportado();
}
