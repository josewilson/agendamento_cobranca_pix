package org.example.agendamento.domain.model.shared;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Validacao de formato de email/telefone compartilhada entre {@link Contato} (cliente) e
 * {@code Prestador} — os dois precisam da mesma regra de formato, mas {@code Prestador} nao
 * reaproveita o VO {@code Contato} (telefone de prestador e um campo isolado, sem o par
 * email+telefone que {@code Contato} representa). Extraido nesta sessao (achado da auditoria de
 * 01/10/2026: os dois regex viviam duplicados em {@code Contato} e {@code Prestador}).
 */
public final class FormatoContato {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern TELEFONE_PATTERN = Pattern.compile("^\\+?\\d{10,14}$");

    private FormatoContato() {
    }

    public static String validarEmail(String email) {
        Objects.requireNonNull(email, "email nao pode ser nulo");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("email invalido: " + email);
        }
        return email;
    }

    /** Normaliza (remove espaco/parenteses/hifen) e valida o formato. Retorna o telefone normalizado. */
    public static String validarTelefone(String telefone) {
        Objects.requireNonNull(telefone, "telefone nao pode ser nulo");
        String telefoneNormalizado = telefone.replaceAll("[\\s()-]", "");
        if (!TELEFONE_PATTERN.matcher(telefoneNormalizado).matches()) {
            throw new IllegalArgumentException("telefone invalido: " + telefone);
        }
        return telefoneNormalizado;
    }
}
