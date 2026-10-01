package org.example.agendamento.application.service;

import org.example.agendamento.adapter.out.persistence.memory.InMemoryServicoRepository;
import org.example.agendamento.application.port.in.ListarServicosPorPrestadorQuery;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.Servico;
import org.example.agendamento.domain.model.servico.ServicoId;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ListarServicosPorPrestadorServiceTest {

    private final InMemoryServicoRepository servicoRepository = new InMemoryServicoRepository();
    private final ListarServicosPorPrestadorService service = new ListarServicosPorPrestadorService(servicoRepository);

    @Test
    void deveListarSoOsServicosDoPrestadorPedido() {
        PrestadorId prestadorA = PrestadorId.novo();
        PrestadorId prestadorB = PrestadorId.novo();

        Servico servicoDoA = new Servico(ServicoId.novo(), prestadorA, "Massagem",
                Duration.ofMinutes(60), Dinheiro.de("150.00"), BigDecimal.valueOf(30));
        Servico outroServicoDoA = new Servico(ServicoId.novo(), prestadorA, "Avaliacao",
                Duration.ofMinutes(30), Dinheiro.de("50.00"), BigDecimal.ZERO);
        Servico servicoDoB = new Servico(ServicoId.novo(), prestadorB, "Corte de cabelo",
                Duration.ofMinutes(45), Dinheiro.de("80.00"), BigDecimal.valueOf(20));

        servicoRepository.salvar(servicoDoA);
        servicoRepository.salvar(outroServicoDoA);
        servicoRepository.salvar(servicoDoB);

        assertThat(service.executar(new ListarServicosPorPrestadorQuery(prestadorA)))
                .containsExactlyInAnyOrder(servicoDoA, outroServicoDoA);
    }
}
