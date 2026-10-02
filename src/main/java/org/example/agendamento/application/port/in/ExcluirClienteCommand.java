package org.example.agendamento.application.port.in;

import org.example.agendamento.domain.model.cliente.ClienteId;

public record ExcluirClienteCommand(ClienteId clienteId) {
}
