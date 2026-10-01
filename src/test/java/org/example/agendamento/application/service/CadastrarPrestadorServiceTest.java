package org.example.agendamento.application.service;

import org.example.agendamento.adapter.out.persistence.memory.InMemoryPrestadorRepository;
import org.example.agendamento.application.port.in.CadastrarPrestadorCommand;
import org.example.agendamento.domain.model.prestador.Prestador;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CadastrarPrestadorServiceTest {

    private final InMemoryPrestadorRepository prestadorRepository = new InMemoryPrestadorRepository();
    private final CadastrarPrestadorService service = new CadastrarPrestadorService(prestadorRepository);

    @Test
    void deveCadastrarPrestadorComCnpjValido() {
        CadastrarPrestadorCommand command = new CadastrarPrestadorCommand(
                "Clinica Bem-Estar", "11987654321", "11222333000181", "CNPJ");

        Prestador prestador = service.executar(command);

        assertThat(prestador.nome()).isEqualTo("Clinica Bem-Estar");
        assertThat(prestador.telefone()).isEqualTo("11987654321");
        assertThat(prestador.documento().numero()).isEqualTo("11222333000181");
        assertThat(prestadorRepository.buscarPorId(prestador.id())).contains(prestador);
    }

    @Test
    void deveLancarExcecaoQuandoCnpjInvalido() {
        CadastrarPrestadorCommand command = new CadastrarPrestadorCommand(
                "Clinica Bem-Estar", "11987654321", "00000000000000", "CNPJ");

        assertThatThrownBy(() -> service.executar(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveLancarExcecaoQuandoTelefoneInvalido() {
        CadastrarPrestadorCommand command = new CadastrarPrestadorCommand(
                "Clinica Bem-Estar", "123", "11222333000181", "CNPJ");

        assertThatThrownBy(() -> service.executar(command))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
