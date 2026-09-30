package org.example.agendamento.adapter.out.persistence.jpa;

import org.example.agendamento.application.port.out.ClienteRepository;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.shared.Contato;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ClienteRepositoryAdapterIT extends AbstractPersistenceIT {

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    void deveSalvarEBuscarClientePorId() {
        Cliente cliente = new Cliente(ClienteId.novo(), "Maria Silva",
                new Contato("maria@exemplo.com", "11987654321"),
                DocumentoFiscal.cpf("111.444.777-35"));

        clienteRepository.salvar(cliente);
        Optional<Cliente> encontrado = clienteRepository.buscarPorId(cliente.id());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().nome()).isEqualTo("Maria Silva");
        assertThat(encontrado.get().contato()).isEqualTo(cliente.contato());
        assertThat(encontrado.get().quantidadeNoShow()).isZero();
    }

    @Test
    void devePersistirIncrementoDeNoShow() {
        Cliente cliente = new Cliente(ClienteId.novo(), "Maria Silva",
                new Contato("maria@exemplo.com", "11987654321"),
                DocumentoFiscal.cpf("111.444.777-35"));
        cliente.registrarNoShow();
        cliente.registrarNoShow();

        clienteRepository.salvar(cliente);
        Optional<Cliente> encontrado = clienteRepository.buscarPorId(cliente.id());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().quantidadeNoShow()).isEqualTo(2);
        assertThat(encontrado.get().exigeSinalObrigatorio()).isTrue();
    }

    @Test
    void deveRetornarVazioQuandoClienteNaoExiste() {
        assertThat(clienteRepository.buscarPorId(ClienteId.novo())).isEmpty();
    }
}
