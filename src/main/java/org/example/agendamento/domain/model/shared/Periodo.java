package org.example.agendamento.domain.model.shared;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record Periodo(Instant inicio, Instant fim) {

    public Periodo {
        Objects.requireNonNull(inicio, "inicio nao pode ser nulo");
        Objects.requireNonNull(fim, "fim nao pode ser nulo");
        if (!fim.isAfter(inicio)) {
            throw new IllegalArgumentException("fim deve ser posterior ao inicio");
        }
    }

    public boolean sobrepoe(Periodo outro) {
        Objects.requireNonNull(outro, "outro nao pode ser nulo");
        return this.inicio.isBefore(outro.fim) && outro.inicio.isBefore(this.fim);
    }

    public boolean estaNoPassado(Instant agora) {
        return inicio.isBefore(agora);
    }

    public Duration duracao() {
        return Duration.between(inicio, fim);
    }
}
