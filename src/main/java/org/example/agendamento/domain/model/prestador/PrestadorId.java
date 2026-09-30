package org.example.agendamento.domain.model.prestador;

import java.util.Objects;
import java.util.UUID;

public record PrestadorId(UUID valor) {

    public PrestadorId {
        Objects.requireNonNull(valor, "valor nao pode ser nulo");
    }

    public static PrestadorId novo() {
        return new PrestadorId(UUID.randomUUID());
    }

    public static PrestadorId de(String valor) {
        return new PrestadorId(UUID.fromString(valor));
    }
}
