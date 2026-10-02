package org.example.agendamento.application.service;

import org.example.agendamento.application.exception.RecursoNaoEncontradoException;
import org.example.agendamento.application.port.in.AtualizarClienteCommand;
import org.example.agendamento.application.port.in.AtualizarClienteUseCase;
import org.example.agendamento.application.port.out.ClienteRepository;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.shared.Contato;
import org.springframework.stereotype.Service;

@Service
public class AtualizarClienteService implements AtualizarClienteUseCase {

    private final ClienteRepository clienteRepository;

    public AtualizarClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public Cliente executar(AtualizarClienteCommand command) {
        Cliente cliente = clienteRepository.buscarPorId(command.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado: " + command.clienteId()));
        cliente.atualizarDadosCadastrais(command.nome(), new Contato(command.email(), command.telefone()));
        return clienteRepository.salvar(cliente);
    }
}
