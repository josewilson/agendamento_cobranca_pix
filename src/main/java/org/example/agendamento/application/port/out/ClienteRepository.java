package org.example.agendamento.application.port.out;

import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.cliente.ClienteId;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository {

    Cliente salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(ClienteId id);

    List<Cliente> buscarTodos();

    void excluir(ClienteId id);
}
