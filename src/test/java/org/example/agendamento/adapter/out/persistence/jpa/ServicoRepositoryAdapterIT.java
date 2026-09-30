package org.example.agendamento.adapter.out.persistence.jpa;

import org.example.agendamento.application.port.out.PrestadorRepository;
import org.example.agendamento.application.port.out.ServicoRepository;
import org.example.agendamento.domain.model.prestador.Prestador;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.Servico;
import org.example.agendamento.domain.model.servico.ServicoId;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ServicoRepositoryAdapterIT extends AbstractPersistenceIT {

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private PrestadorRepository prestadorRepository;

    private Prestador prestador;

    @BeforeEach
    void setUp() {
        prestador = prestadorRepository.salvar(new Prestador(PrestadorId.novo(), "Clinica Bem Estar",
                DocumentoFiscal.cnpj("11.222.333/0001-81"),
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100))));
    }

    @Test
    void deveSalvarEBuscarServicoPorId() {
        Servico servico = new Servico(ServicoId.novo(), prestador.id(), "Corte de cabelo",
                Duration.ofMinutes(45), Dinheiro.de("80.00"), BigDecimal.valueOf(30));

        servicoRepository.salvar(servico);
        Optional<Servico> encontrado = servicoRepository.buscarPorId(servico.id());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().nome()).isEqualTo("Corte de cabelo");
        assertThat(encontrado.get().duracao()).isEqualTo(Duration.ofMinutes(45));
        assertThat(encontrado.get().preco()).isEqualTo(Dinheiro.de("80.00"));
        assertThat(encontrado.get().prestadorId()).isEqualTo(prestador.id());
    }

    @Test
    void deveRetornarVazioQuandoServicoNaoExiste() {
        assertThat(servicoRepository.buscarPorId(ServicoId.novo())).isEmpty();
    }
}
