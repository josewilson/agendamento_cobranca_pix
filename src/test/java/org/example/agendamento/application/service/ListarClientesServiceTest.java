package org.example.agendamento.application.service;

import org.example.agendamento.adapter.out.persistence.memory.InMemoryClienteRepository;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.shared.Contato;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ListarClientesServiceTest {

    private final InMemoryClienteRepository clienteRepository = new InMemoryClienteRepository();
    private final ListarClientesService service = new ListarClientesService(clienteRepository);

    @Test
    void deveListarTodosOsClientesCadastrados() {
        Cliente cliente1 = new Cliente(ClienteId.novo(), "Maria Silva",
                new Contato("maria@exemplo.com", "11987654321"), DocumentoFiscal.cpf("111.444.777-35"));
        Cliente cliente2 = new Cliente(ClienteId.novo(), "Joao Souza",
                new Contato("joao@exemplo.com", "11988887777"), DocumentoFiscal.cpf("529.982.247-25"));
        clienteRepository.salvar(cliente1);
        clienteRepository.salvar(cliente2);

        assertThat(service.executar()).containsExactlyInAnyOrder(cliente1, cliente2);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaClientes() {
        assertThat(service.executar()).isEmpty();
    }
}
