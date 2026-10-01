package org.example.agendamento.application.port.in;

import org.example.agendamento.domain.model.cliente.Cliente;

import java.util.List;

public interface ListarClientesUseCase {
    List<Cliente> executar();
}
