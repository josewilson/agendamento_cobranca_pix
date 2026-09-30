package org.example.agendamento.application.service;

import org.example.agendamento.application.port.out.Clock;

import java.time.Instant;

final class ClockFixo implements Clock {

    private Instant agora;

    ClockFixo(Instant agora) {
        this.agora = agora;
    }

    @Override
    public Instant agora() {
        return agora;
    }

    void avancarPara(Instant novoAgora) {
        this.agora = novoAgora;
    }
}
