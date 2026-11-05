package br.com.josewilson.agendamento.application.port.out;

public interface EnviadorDeNotificacao {

    void enviar(Notificacao notificacao);

    CanalNotificacao canalSuportado();
}
