package br.com.josewilson.agendamento.application.port.out;

public record Notificacao(CanalNotificacao canal, String destinatario, String assunto, String mensagem) {
}
