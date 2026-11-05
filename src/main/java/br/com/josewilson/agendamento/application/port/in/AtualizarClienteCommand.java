package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;

public record AtualizarClienteCommand(ClienteId clienteId, String nome, String email, String telefone) {
}
