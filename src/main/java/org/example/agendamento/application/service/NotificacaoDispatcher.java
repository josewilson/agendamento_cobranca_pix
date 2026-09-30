package org.example.agendamento.application.service;

import org.example.agendamento.application.port.out.CanalNotificacao;
import org.example.agendamento.application.port.out.EnviadorDeNotificacao;
import org.example.agendamento.application.port.out.Notificacao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Roteia uma Notificacao para o EnviadorDeNotificacao que atende o canal pedido.
 * Permite que quem dispara uma notificacao (ex.: NotificacaoEventListener) nao precise
 * conhecer quais adapters de canal estao disponiveis.
 */
@Service
public class NotificacaoDispatcher {

    private final Map<CanalNotificacao, EnviadorDeNotificacao> enviadoresPorCanal;

    public NotificacaoDispatcher(List<EnviadorDeNotificacao> enviadores) {
        this.enviadoresPorCanal = enviadores.stream()
                .collect(Collectors.toMap(EnviadorDeNotificacao::canalSuportado, Function.identity()));
    }

    public void enviar(Notificacao notificacao) {
        EnviadorDeNotificacao enviador = enviadoresPorCanal.get(notificacao.canal());
        if (enviador == null) {
            throw new IllegalStateException("Nenhum enviador configurado para o canal " + notificacao.canal());
        }
        enviador.enviar(notificacao);
    }
}
