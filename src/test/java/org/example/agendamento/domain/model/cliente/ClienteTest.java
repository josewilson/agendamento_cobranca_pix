package org.example.agendamento.domain.model.cliente;

import org.example.agendamento.domain.model.shared.Contato;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClienteTest {

    private final Contato contato = new Contato("cliente@exemplo.com", "11987654321");
    private final DocumentoFiscal documento = DocumentoFiscal.cpf("111.444.777-35");

    @Test
    void deveCriarClienteValido() {
        Cliente cliente = new Cliente(ClienteId.novo(), "Maria Silva", contato, documento);

        assertThat(cliente.nome()).isEqualTo("Maria Silva");
        assertThat(cliente.quantidadeNoShow()).isZero();
    }

    @Test
    void deveLancarExcecaoParaNomeVazio() {
        assertThatThrownBy(() -> new Cliente(ClienteId.novo(), " ", contato, documento))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void naoDeveExigirSinalObrigatorioComPoucosNoShows() {
        Cliente cliente = new Cliente(ClienteId.novo(), "Maria Silva", contato, documento);

        cliente.registrarNoShow();

        assertThat(cliente.exigeSinalObrigatorio()).isFalse();
    }

    @Test
    void deveExigirSinalObrigatorioAoAtingirLimiteDeNoShow() {
        Cliente cliente = new Cliente(ClienteId.novo(), "Maria Silva", contato, documento);

        cliente.registrarNoShow();
        cliente.registrarNoShow();

        assertThat(cliente.exigeSinalObrigatorio()).isTrue();
    }

    @Test
    void deveIncrementarContadorDeNoShow() {
        Cliente cliente = new Cliente(ClienteId.novo(), "Maria Silva", contato, documento);

        cliente.registrarNoShow();

        assertThat(cliente.quantidadeNoShow()).isEqualTo(1);
    }

    @Test
    void doisClientesComMesmoIdDevemSerIguaisMesmoComDadosDiferentes() {
        ClienteId id = ClienteId.novo();
        Cliente cliente1 = new Cliente(id, "Maria", contato, documento);
        Cliente cliente2 = new Cliente(id, "Outra Maria", contato, documento);

        assertThat(cliente1).isEqualTo(cliente2);
    }
}
