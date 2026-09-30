package org.example.agendamento.domain.model.cliente;

import java.util.Objects;
import java.util.UUID;

public record ClienteId(UUID valor) {

    public ClienteId {
        Objects.requireNonNull(valor, "valor nao pode ser nulo");
    }

    public static ClienteId novo() {
        return new ClienteId(UUID.randomUUID());
    }

    public static ClienteId de(String valor) {
        return new ClienteId(UUID.fromString(valor));
    }
}
