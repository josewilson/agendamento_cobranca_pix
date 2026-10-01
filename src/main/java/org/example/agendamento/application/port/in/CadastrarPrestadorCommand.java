package org.example.agendamento.application.port.in;

public record CadastrarPrestadorCommand(String nome, String telefone, String email, String senha,
                                         String documentoNumero, String documentoTipo) {
}
