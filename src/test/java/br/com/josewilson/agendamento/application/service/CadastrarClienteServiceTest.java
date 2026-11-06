package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryClienteRepository;
import br.com.josewilson.agendamento.application.port.in.CadastrarClienteCommand;
import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CadastrarClienteServiceTest {

    private final InMemoryClienteRepository clienteRepository = new InMemoryClienteRepository();
    private final CadastrarClienteService service = new CadastrarClienteService(clienteRepository, new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder());

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
    void deveGuardarSomenteOHashDaSenhaEPermitirBuscarPorEmail() {
        Cliente cliente = service.executar(new CadastrarClienteCommand(
                "Maria Silva", "Maria@Exemplo.com", "11987654321", "11144477735", "CPF", null, "senha1234"));

        assertThat(cliente.senhaHash()).isNotEqualTo("senha1234").startsWith("$2");
        assertThat(clienteRepository.buscarPorEmail("maria@exemplo.com")).contains(cliente);
    }

    @Test
    void clienteSemSenhaNaoTemLogin() {
        service.executar(new CadastrarClienteCommand(
                "Maria Silva", "maria@exemplo.com", "11987654321", "11144477735", "CPF"));

        assertThat(clienteRepository.buscarPorEmail("maria@exemplo.com")).isEmpty();
    }

    @Test
    void deveLancarExcecaoQuandoEmailInvalido() {
        CadastrarClienteCommand command = new CadastrarClienteCommand(
                "Maria Silva", "nao-e-um-email", "11987654321", "11144477735", "CPF");

        assertThatThrownBy(() -> service.executar(command))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
