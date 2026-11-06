package br.com.josewilson.agendamento.adapter.out.persistence.jpa;

import br.com.josewilson.agendamento.application.port.out.ClienteRepository;
import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;
import br.com.josewilson.agendamento.domain.model.shared.Contato;
import br.com.josewilson.agendamento.domain.model.shared.DocumentoFiscal;
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

    @Test
    void devePaginarNoBancoEPersistirCanalPreferido() {
        String[] cpfs = {"111.444.777-35", "529.982.247-25", "168.995.350-09"};
        String[] nomes = {"Carlos", "Ana", "Bia"};
        for (int i = 0; i < 3; i++) {
            Cliente c = new Cliente(ClienteId.novo(), nomes[i],
                    new Contato("c" + i + "@exemplo.com", "1198765432" + i), DocumentoFiscal.cpf(cpfs[i]));
            c.definirCanalPreferido(br.com.josewilson.agendamento.domain.model.cliente.CanalPreferido.WHATSAPP);
            clienteRepository.salvar(c);
        }

        var pagina = clienteRepository.buscarPagina(1, 2);

        assertThat(pagina.total()).isEqualTo(3);
        assertThat(pagina.itens()).extracting(Cliente::nome).containsExactly("Carlos"); // ordenado por nome: Ana, Bia | Carlos
        assertThat(pagina.itens().get(0).canalPreferido())
                .isEqualTo(br.com.josewilson.agendamento.domain.model.cliente.CanalPreferido.WHATSAPP);
    }
    @Test
    void deveBuscarPorEmailSoQuemTemLoginIgnorandoMaiusculas() {
        Cliente comLogin = new Cliente(ClienteId.novo(), "Com Login",
                new Contato("Login@Exemplo.com", "11987654321"), DocumentoFiscal.cpf("111.444.777-35"));
        comLogin.definirSenhaHash("$2a$10$hashfalsodeteste");
        Cliente legado = new Cliente(ClienteId.novo(), "Legado",
                new Contato("legado@exemplo.com", "11987654322"), DocumentoFiscal.cpf("529.982.247-25"));
        clienteRepository.salvar(comLogin);
        clienteRepository.salvar(legado);

        assertThat(clienteRepository.buscarPorEmail("login@exemplo.com")).isPresent()
                .get().extracting(Cliente::senhaHash).isEqualTo("$2a$10$hashfalsodeteste");
        assertThat(clienteRepository.buscarPorEmail("legado@exemplo.com")).isEmpty();
    }
}
