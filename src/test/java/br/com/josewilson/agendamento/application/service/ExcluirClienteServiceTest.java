package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryClienteRepository;
import br.com.josewilson.agendamento.application.exception.RecursoNaoEncontradoException;
import br.com.josewilson.agendamento.application.port.in.ExcluirClienteCommand;
import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;
import br.com.josewilson.agendamento.domain.model.shared.Contato;
import br.com.josewilson.agendamento.domain.model.shared.DocumentoFiscal;
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
