package org.example.agendamento.domain.model.servico;

import java.util.Objects;
import java.util.UUID;

public record ServicoId(UUID valor) {

    public ServicoId {
        Objects.requireNonNull(valor, "valor nao pode ser nulo");
    }

    public static ServicoId novo() {
        return new ServicoId(UUID.randomUUID());
    }

    public static ServicoId de(String valor) {
        return new ServicoId(UUID.fromString(valor));
    }
}
