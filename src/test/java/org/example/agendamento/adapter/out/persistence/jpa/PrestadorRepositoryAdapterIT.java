package org.example.agendamento.adapter.out.persistence.jpa;

import org.example.agendamento.application.port.out.PrestadorRepository;
import org.example.agendamento.domain.model.prestador.Prestador;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PrestadorRepositoryAdapterIT extends AbstractPersistenceIT {

    @Autowired
    private PrestadorRepository prestadorRepository;

    @Test
    void deveSalvarEBuscarPrestadorPorId() {
        Prestador prestador = new Prestador(PrestadorId.novo(), "Clinica Bem Estar",
                DocumentoFiscal.cnpj("11.222.333/0001-81"),
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100)));

        prestadorRepository.salvar(prestador);
        Optional<Prestador> encontrado = prestadorRepository.buscarPorId(prestador.id());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().nome()).isEqualTo("Clinica Bem Estar");
        assertThat(encontrado.get().documento()).isEqualTo(prestador.documento());
        assertThat(encontrado.get().politicaCancelamentoPadrao()).isEqualTo(prestador.politicaCancelamentoPadrao());
    }

    @Test
    void deveRetornarVazioQuandoPrestadorNaoExiste() {
        assertThat(prestadorRepository.buscarPorId(PrestadorId.novo())).isEmpty();
    }
}
