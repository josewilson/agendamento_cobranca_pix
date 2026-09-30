package org.example.agendamento.domain.model.agendamento;

import java.util.Objects;
import java.util.UUID;

public record AgendamentoId(UUID valor) {

    public AgendamentoId {
        Objects.requireNonNull(valor, "valor nao pode ser nulo");
    }

    public static AgendamentoId novo() {
        return new AgendamentoId(UUID.randomUUID());
    }

    public static AgendamentoId de(String valor) {
        return new AgendamentoId(UUID.fromString(valor));
    }
}
