package org.example.agendamento.adapter.out.notificacao.memory;

import org.example.agendamento.application.port.out.CanalNotificacao;
import org.example.agendamento.application.port.out.EnviadorDeNotificacao;
import org.example.agendamento.application.port.out.Notificacao;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Profile("dev")
public class EnviadorDeNotificacaoFake implements EnviadorDeNotificacao {

    private final List<Notificacao> notificacoesEnviadas = new CopyOnWriteArrayList<>();

    @Override
    public void enviar(Notificacao notificacao) {
        notificacoesEnviadas.add(notificacao);
    }

    @Override
    public CanalNotificacao canalSuportado() {
        return CanalNotificacao.EMAIL;
    }

    public List<Notificacao> notificacoesEnviadas() {
        return List.copyOf(notificacoesEnviadas);
    }
}
