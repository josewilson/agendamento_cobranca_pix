package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.application.exception.RecursoNaoEncontradoException;
import br.com.josewilson.agendamento.application.port.in.AtualizarClienteCommand;
import br.com.josewilson.agendamento.application.port.in.AtualizarClienteUseCase;
import br.com.josewilson.agendamento.application.port.out.ClienteRepository;
import br.com.josewilson.agendamento.domain.model.cliente.CanalPreferido;
import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import br.com.josewilson.agendamento.domain.model.shared.Contato;
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
        if (command.canalPreferido() != null) {
            cliente.definirCanalPreferido(CanalPreferido.valueOf(command.canalPreferido().toUpperCase()));
        }
        return clienteRepository.salvar(cliente);
    }
}
