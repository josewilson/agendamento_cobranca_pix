package org.example.agendamento.adapter.out.persistence.memory;

import org.example.agendamento.application.port.out.ClienteRepository;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("dev")
public class InMemoryClienteRepository implements ClienteRepository {

    private final Map<ClienteId, Cliente> clientes = new ConcurrentHashMap<>();

    @Override
    public Cliente salvar(Cliente cliente) {
        clientes.put(cliente.id(), cliente);
        return cliente;
    }

    @Override
    public Optional<Cliente> buscarPorId(ClienteId id) {
        return Optional.ofNullable(clientes.get(id));
    }

    @Override
    public List<Cliente> buscarTodos() {
        return new ArrayList<>(clientes.values());
    }
}
