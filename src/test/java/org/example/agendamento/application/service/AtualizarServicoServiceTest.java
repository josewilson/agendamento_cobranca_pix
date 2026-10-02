package org.example.agendamento.application.service;

import org.example.agendamento.adapter.out.persistence.memory.InMemoryServicoRepository;
import org.example.agendamento.application.exception.AcessoNaoAutorizadoException;
import org.example.agendamento.application.exception.RecursoNaoEncontradoException;
import org.example.agendamento.application.port.in.AtualizarServicoCommand;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.Servico;
import org.example.agendamento.domain.model.servico.ServicoId;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AtualizarServicoServiceTest {

    private final InMemoryServicoRepository servicoRepository = new InMemoryServicoRepository();
    private final AtualizarServicoService service = new AtualizarServicoService(servicoRepository);

    @Test
    void deveAtualizarDadosDoServicoDoProprioPrestador() {
        PrestadorId prestadorId = PrestadorId.novo();
        Servico servico = new Servico(ServicoId.novo(), prestadorId, "Corte",
                Duration.ofMinutes(30), Dinheiro.de("50.00"), BigDecimal.ZERO);
        servicoRepository.salvar(servico);

        Servico atualizado = service.executar(new AtualizarServicoCommand(
                servico.id(), prestadorId, "Corte e barba", 45, BigDecimal.valueOf(80), BigDecimal.valueOf(20)));

        assertThat(atualizado.nome()).isEqualTo("Corte e barba");
        assertThat(atualizado.duracao()).isEqualTo(Duration.ofMinutes(45));
        assertThat(atualizado.preco()).isEqualTo(Dinheiro.de("80.00"));
        assertThat(atualizado.percentualSinal()).isEqualByComparingTo(BigDecimal.valueOf(20));
    }

    @Test
    void deveLancarExcecaoQuandoServicoNaoPertenceAoPrestadorAutenticado() {
        PrestadorId dono = PrestadorId.novo();
        PrestadorId outroPrestador = PrestadorId.novo();
        Servico servico = new Servico(ServicoId.novo(), dono, "Corte",
                Duration.ofMinutes(30), Dinheiro.de("50.00"), BigDecimal.ZERO);
        servicoRepository.salvar(servico);

        assertThatThrownBy(() -> service.executar(new AtualizarServicoCommand(
                servico.id(), outroPrestador, "Corte", 30, BigDecimal.valueOf(50), BigDecimal.ZERO)))
                .isInstanceOf(AcessoNaoAutorizadoException.class);
    }

    @Test
    void deveLancarExcecaoQuandoServicoNaoExiste() {
        assertThatThrownBy(() -> service.executar(new AtualizarServicoCommand(
                ServicoId.novo(), PrestadorId.novo(), "Corte", 30, BigDecimal.valueOf(50), BigDecimal.ZERO)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
