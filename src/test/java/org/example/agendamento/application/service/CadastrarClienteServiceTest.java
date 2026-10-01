package org.example.agendamento.application.service;

import org.example.agendamento.adapter.out.persistence.memory.InMemoryClienteRepository;
import org.example.agendamento.application.port.in.CadastrarClienteCommand;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CadastrarClienteServiceTest {

    private final InMemoryClienteRepository clienteRepository = new InMemoryClienteRepository();
    private final CadastrarClienteService service = new CadastrarClienteService(clienteRepository);

    @Test
    void deveCadastrarClienteComCpfValido() {
        CadastrarClienteCommand command = new CadastrarClienteCommand(
                "Maria Silva", "maria@exemplo.com", "11987654321", "11144477735", "CPF");

        Cliente cliente = service.executar(command);

        assertThat(cliente.nome()).isEqualTo("Maria Silva");
        assertThat(cliente.contato().email()).isEqualTo("maria@exemplo.com");
        assertThat(cliente.quantidadeNoShow()).isZero();
        assertThat(clienteRepository.buscarPorId(cliente.id())).contains(cliente);
    }

    @Test
    void deveLancarExcecaoQuandoEmailInvalido() {
        CadastrarClienteCommand command = new CadastrarClienteCommand(
                "Maria Silva", "nao-e-um-email", "11987654321", "11144477735", "CPF");

        assertThatThrownBy(() -> service.executar(command))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
