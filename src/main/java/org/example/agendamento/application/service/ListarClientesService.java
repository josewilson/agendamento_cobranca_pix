package org.example.agendamento.application.service;

import org.example.agendamento.application.port.in.ListarClientesUseCase;
import org.example.agendamento.application.port.out.ClienteRepository;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.springframework.stereotype.Service;

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
}
