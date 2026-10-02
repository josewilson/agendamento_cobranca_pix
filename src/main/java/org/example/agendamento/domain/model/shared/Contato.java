package org.example.agendamento.domain.model.shared;

public record Contato(String email, String telefone) {

    public Contato {
        email = FormatoContato.validarEmail(email);
        telefone = FormatoContato.validarTelefone(telefone);
    }
}
