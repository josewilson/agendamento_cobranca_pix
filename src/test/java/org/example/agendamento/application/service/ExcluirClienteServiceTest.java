package org.example.agendamento.application.service;

import org.example.agendamento.adapter.out.persistence.memory.InMemoryClienteRepository;
import org.example.agendamento.application.exception.RecursoNaoEncontradoException;
import org.example.agendamento.application.port.in.ExcluirClienteCommand;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.shared.Contato;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcluirClienteServiceTest {

    private final InMemoryClienteRepository clienteRepository = new InMemoryClienteRepository();
    private final ExcluirClienteService service = new ExcluirClienteService(clienteRepository);

    @Test
    void deveExcluirClienteExistente() {
        Cliente cliente = new Cliente(ClienteId.novo(), "Maria Silva",
                new Contato("maria@exemplo.com", "11987654321"), DocumentoFiscal.cpf("111.444.777-35"));
        clienteRepository.salvar(cliente);

        service.executar(new ExcluirClienteCommand(cliente.id()));

        assertThat(clienteRepository.buscarPorId(cliente.id())).isEmpty();
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoExiste() {
        assertThatThrownBy(() -> service.executar(new ExcluirClienteCommand(ClienteId.novo())))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
