package org.example.agendamento.application.service;

import org.example.agendamento.application.exception.RecursoNaoEncontradoException;
import org.example.agendamento.application.port.in.ExcluirClienteCommand;
import org.example.agendamento.application.port.in.ExcluirClienteUseCase;
import org.example.agendamento.application.port.out.ClienteRepository;
import org.springframework.stereotype.Service;

@Service
public class ExcluirClienteService implements ExcluirClienteUseCase {

    private final ClienteRepository clienteRepository;

    public ExcluirClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public void executar(ExcluirClienteCommand command) {
        clienteRepository.buscarPorId(command.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado: " + command.clienteId()));
        clienteRepository.excluir(command.clienteId());
    }
}
