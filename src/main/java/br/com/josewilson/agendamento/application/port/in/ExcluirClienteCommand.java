package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;

public record ExcluirClienteCommand(ClienteId clienteId) {
}
