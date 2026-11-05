package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryPrestadorRepository;
import br.com.josewilson.agendamento.application.exception.AcessoNaoAutorizadoException;
import br.com.josewilson.agendamento.application.exception.RecursoNaoEncontradoException;
import br.com.josewilson.agendamento.application.port.in.AtualizarPrestadorCommand;
import br.com.josewilson.agendamento.domain.model.prestador.Prestador;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.shared.DocumentoFiscal;
import br.com.josewilson.agendamento.domain.model.shared.PoliticaCancelamento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AtualizarPrestadorServiceTest {

    private final InMemoryPrestadorRepository prestadorRepository = new InMemoryPrestadorRepository();
    private final AtualizarPrestadorService service = new AtualizarPrestadorService(prestadorRepository);

    @Test
    void deveAtualizarNomeETelefoneDoProprioPrestador() {
        Prestador prestador = new Prestador(PrestadorId.novo(), "Clinica Bem-Estar", "11987654321",
                "clinica@exemplo.com", "hash-fake-de-teste", DocumentoFiscal.cnpj("11222333000181"),
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100)));
        prestadorRepository.salvar(prestador);

        Prestador atualizado = service.executar(new AtualizarPrestadorCommand(
                prestador.id(), prestador.id(), "Clinica Bem-Estar Ltda", "11999998888"));

        assertThat(atualizado.nome()).isEqualTo("Clinica Bem-Estar Ltda");
        assertThat(atualizado.telefone()).isEqualTo("11999998888");
        assertThat(atualizado.email()).isEqualTo("clinica@exemplo.com");
    }

    @Test
    void deveLancarExcecaoAoTentarEditarOutroPrestador() {
        Prestador prestador = new Prestador(PrestadorId.novo(), "Clinica Bem-Estar", "11987654321",
                "clinica@exemplo.com", "hash-fake-de-teste", DocumentoFiscal.cnpj("11222333000181"),
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100)));
        prestadorRepository.salvar(prestador);
        PrestadorId outroPrestador = PrestadorId.novo();

        assertThatThrownBy(() -> service.executar(new AtualizarPrestadorCommand(
                prestador.id(), outroPrestador, "Nome Invasor", "11999998888")))
                .isInstanceOf(AcessoNaoAutorizadoException.class);
    }

    @Test
    void deveLancarExcecaoQuandoPrestadorNaoExiste() {
        PrestadorId id = PrestadorId.novo();
        assertThatThrownBy(() -> service.executar(new AtualizarPrestadorCommand(id, id, "Nome", "11999998888")))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
