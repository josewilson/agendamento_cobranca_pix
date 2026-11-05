package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.cliente.Cliente;

import java.util.List;

public interface ListarClientesUseCase {
    List<Cliente> executar();
}
