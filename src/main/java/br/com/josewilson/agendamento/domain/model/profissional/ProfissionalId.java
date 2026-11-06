package br.com.josewilson.agendamento.domain.model.profissional;

import java.util.Objects;
import java.util.UUID;

public record ProfissionalId(UUID valor) {

    public ProfissionalId {
        Objects.requireNonNull(valor, "valor nao pode ser nulo");
    }

    public static ProfissionalId de(String valor) {
        return new ProfissionalId(UUID.fromString(valor));
    }

    public static ProfissionalId novo() {
        return new ProfissionalId(UUID.randomUUID());
    }
}
