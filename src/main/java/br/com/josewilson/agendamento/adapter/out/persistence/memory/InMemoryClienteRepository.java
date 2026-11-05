package br.com.josewilson.agendamento.adapter.out.persistence.memory;

import br.com.josewilson.agendamento.application.port.out.ClienteRepository;
import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;
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

    @Override
    public void excluir(ClienteId id) {
        clientes.remove(id);
    }
}
