package org.example.agendamento.application.port.in;

import org.example.agendamento.domain.model.cliente.Cliente;

public interface AtualizarClienteUseCase {
    Cliente executar(AtualizarClienteCommand command);
}
