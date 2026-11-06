package br.com.josewilson.agendamento.domain.model.profissional;

import java.util.Objects;
import java.util.UUID;

public record BloqueioId(UUID valor) {

    public BloqueioId {
        Objects.requireNonNull(valor, "valor nao pode ser nulo");
    }

    public static BloqueioId novo() {
        return new BloqueioId(UUID.randomUUID());
    }
}
