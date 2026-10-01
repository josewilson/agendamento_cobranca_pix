package org.example.agendamento.application.service;

import org.example.agendamento.adapter.out.persistence.memory.InMemoryPrestadorRepository;
import org.example.agendamento.adapter.out.persistence.memory.InMemoryServicoRepository;
import org.example.agendamento.application.exception.RecursoNaoEncontradoException;
import org.example.agendamento.application.port.in.CadastrarServicoCommand;
import org.example.agendamento.domain.model.prestador.Prestador;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.Servico;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CadastrarServicoServiceTest {

    private final InMemoryServicoRepository servicoRepository = new InMemoryServicoRepository();
    private final InMemoryPrestadorRepository prestadorRepository = new InMemoryPrestadorRepository();
    private final CadastrarServicoService service = new CadastrarServicoService(servicoRepository, prestadorRepository);

    private Prestador prestador;

    @BeforeEach
    void setUp() {
        prestador = new Prestador(PrestadorId.novo(), "Clinica Bem-Estar",
                DocumentoFiscal.cnpj("11222333000181"), PoliticaCancelamento.padrao());
        prestadorRepository.salvar(prestador);
    }

    @Test
    void deveCadastrarServicoVinculadoAoPrestador() {
        CadastrarServicoCommand command = new CadastrarServicoCommand(
                prestador.id(), "Massagem relaxante", 60, BigDecimal.valueOf(150), BigDecimal.valueOf(30));

        Servico servico = service.executar(command);

        assertThat(servico.prestadorId()).isEqualTo(prestador.id());
        assertThat(servico.duracao().toMinutes()).isEqualTo(60);
        assertThat(servicoRepository.buscarPorId(servico.id())).contains(servico);
    }

    @Test
    void deveLancarExcecaoQuandoPrestadorNaoEncontrado() {
        CadastrarServicoCommand command = new CadastrarServicoCommand(
                PrestadorId.novo(), "Massagem relaxante", 60, BigDecimal.valueOf(150), BigDecimal.valueOf(30));

        assertThatThrownBy(() -> service.executar(command))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
