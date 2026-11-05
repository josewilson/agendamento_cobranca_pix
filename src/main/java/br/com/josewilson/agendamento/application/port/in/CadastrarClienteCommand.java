package br.com.josewilson.agendamento.application.port.in;

public record CadastrarClienteCommand(String nome, String email, String telefone,
                                       String documentoNumero, String documentoTipo) {
}
