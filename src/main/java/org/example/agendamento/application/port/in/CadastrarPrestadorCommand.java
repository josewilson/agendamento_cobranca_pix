package org.example.agendamento.application.port.in;

public record CadastrarPrestadorCommand(String nome, String telefone, String documentoNumero, String documentoTipo) {
}
