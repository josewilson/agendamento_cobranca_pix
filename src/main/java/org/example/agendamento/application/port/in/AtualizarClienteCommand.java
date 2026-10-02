package org.example.agendamento.application.port.in;

import org.example.agendamento.domain.model.cliente.ClienteId;

public record AtualizarClienteCommand(ClienteId clienteId, String nome, String email, String telefone) {
}
