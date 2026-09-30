package org.example.agendamento.domain.model.shared;

import java.util.Objects;
import java.util.regex.Pattern;

public record Contato(String email, String telefone) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern TELEFONE_PATTERN =
            Pattern.compile("^\\+?\\d{10,14}$");

    public Contato {
        Objects.requireNonNull(email, "email nao pode ser nulo");
        Objects.requireNonNull(telefone, "telefone nao pode ser nulo");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("email invalido: " + email);
        }
        String telefoneNormalizado = telefone.replaceAll("[\\s()-]", "");
        if (!TELEFONE_PATTERN.matcher(telefoneNormalizado).matches()) {
            throw new IllegalArgumentException("telefone invalido: " + telefone);
        }
        telefone = telefoneNormalizado;
    }
}
