package org.example.agendamento.application.port.in;

public record CadastrarPrestadorCommand(String nome, String documentoNumero, String documentoTipo) {
}
