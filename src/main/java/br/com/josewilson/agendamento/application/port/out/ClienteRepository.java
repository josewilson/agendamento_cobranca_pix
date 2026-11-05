package br.com.josewilson.agendamento.application.port.out;

import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository {

    Cliente salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(ClienteId id);

    List<Cliente> buscarTodos();

    Pagina<Cliente> buscarPagina(int page, int size);

    void excluir(ClienteId id);
}
