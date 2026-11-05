package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryClienteRepository;
import br.com.josewilson.agendamento.application.exception.RecursoNaoEncontradoException;
import br.com.josewilson.agendamento.application.port.in.AtualizarClienteCommand;
import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;
import br.com.josewilson.agendamento.domain.model.shared.Contato;
import br.com.josewilson.agendamento.domain.model.shared.DocumentoFiscal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AtualizarClienteServiceTest {

    private final InMemoryClienteRepository clienteRepository = new InMemoryClienteRepository();
    private final AtualizarClienteService service = new AtualizarClienteService(clienteRepository);

    @Test
    void deveAtualizarNomeEContatoMantendoDocumentoEHistorico() {
        Cliente cliente = new Cliente(ClienteId.novo(), "Maria Silva",
                new Contato("maria@exemplo.com", "11987654321"), DocumentoFiscal.cpf("111.444.777-35"));
        cliente.registrarNoShow();
        clienteRepository.salvar(cliente);

        Cliente atualizado = service.executar(new AtualizarClienteCommand(
                cliente.id(), "Maria S. Silva", "maria.nova@exemplo.com", "11999998888"));

        assertThat(atualizado.nome()).isEqualTo("Maria S. Silva");
        assertThat(atualizado.contato().email()).isEqualTo("maria.nova@exemplo.com");
        assertThat(atualizado.contato().telefone()).isEqualTo("11999998888");
        assertThat(atualizado.documento()).isEqualTo(cliente.documento());
        assertThat(atualizado.quantidadeNoShow()).isEqualTo(1);
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoExiste() {
        assertThatThrownBy(() -> service.executar(new AtualizarClienteCommand(
                ClienteId.novo(), "Nome", "email@exemplo.com", "11987654321")))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
