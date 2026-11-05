package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryServicoRepository;
import br.com.josewilson.agendamento.application.exception.AcessoNaoAutorizadoException;
import br.com.josewilson.agendamento.application.exception.RecursoNaoEncontradoException;
import br.com.josewilson.agendamento.application.port.in.ExcluirServicoCommand;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.servico.Servico;
import br.com.josewilson.agendamento.domain.model.servico.ServicoId;
import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcluirServicoServiceTest {

    private final InMemoryServicoRepository servicoRepository = new InMemoryServicoRepository();
    private final ExcluirServicoService service = new ExcluirServicoService(servicoRepository);

    @Test
    void deveExcluirServicoDoProprioPrestador() {
        PrestadorId prestadorId = PrestadorId.novo();
        Servico servico = new Servico(ServicoId.novo(), prestadorId, "Corte",
                Duration.ofMinutes(30), Dinheiro.de("50.00"), BigDecimal.ZERO);
        servicoRepository.salvar(servico);

        service.executar(new ExcluirServicoCommand(servico.id(), prestadorId));

        assertThat(servicoRepository.buscarPorId(servico.id())).isEmpty();
    }

    @Test
    void deveLancarExcecaoQuandoServicoNaoPertenceAoPrestadorAutenticado() {
        PrestadorId dono = PrestadorId.novo();
        PrestadorId outroPrestador = PrestadorId.novo();
        Servico servico = new Servico(ServicoId.novo(), dono, "Corte",
                Duration.ofMinutes(30), Dinheiro.de("50.00"), BigDecimal.ZERO);
        servicoRepository.salvar(servico);

        assertThatThrownBy(() -> service.executar(new ExcluirServicoCommand(servico.id(), outroPrestador)))
                .isInstanceOf(AcessoNaoAutorizadoException.class);
        assertThat(servicoRepository.buscarPorId(servico.id())).isPresent();
    }

    @Test
    void deveLancarExcecaoQuandoServicoNaoExiste() {
        assertThatThrownBy(() -> service.executar(new ExcluirServicoCommand(ServicoId.novo(), PrestadorId.novo())))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
