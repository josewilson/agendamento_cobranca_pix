package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.application.port.in.ListarClientesUseCase;
import br.com.josewilson.agendamento.application.port.out.ClienteRepository;
import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import org.springframework.stereotype.Service;

import br.com.josewilson.agendamento.application.port.out.Pagina;
import java.util.List;

@Service
public class ListarClientesService implements ListarClientesUseCase {

    private final ClienteRepository clienteRepository;

    public ListarClientesService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public List<Cliente> executar() {
        return clienteRepository.buscarTodos();
    }

    @Override
    public Pagina<Cliente> executar(int page, int size) {
        return clienteRepository.buscarPagina(page, size);
    }
}
