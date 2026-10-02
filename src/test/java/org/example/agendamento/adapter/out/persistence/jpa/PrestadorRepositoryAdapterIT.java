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
        Prestador prestador = new Prestador(PrestadorId.novo(), "Clinica Bem Estar", "11987654321",
                "clinica@exemplo.com", "hash-fake-de-teste", DocumentoFiscal.cnpj("11.222.333/0001-81"),
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100)));

        prestadorRepository.salvar(prestador);
        Optional<Prestador> encontrado = prestadorRepository.buscarPorId(prestador.id());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().nome()).isEqualTo("Clinica Bem Estar");
        assertThat(encontrado.get().telefone()).isEqualTo("11987654321");
        assertThat(encontrado.get().email()).isEqualTo("clinica@exemplo.com");
        assertThat(encontrado.get().documento()).isEqualTo(prestador.documento());
        // Postgres "numeric" volta com escala propria (100.00); BigDecimal.equals() (usado pelo
        // equals() gerado do record PoliticaCancelamento) e sensivel a escala mesmo quando
        // compareTo() e zero, entao comparamos os dois campos em vez do record inteiro.
        assertThat(encontrado.get().politicaCancelamentoPadrao().antecedenciaMinima())
                .isEqualTo(prestador.politicaCancelamentoPadrao().antecedenciaMinima());
        assertThat(encontrado.get().politicaCancelamentoPadrao().percentualRetido())
                .isEqualByComparingTo(prestador.politicaCancelamentoPadrao().percentualRetido());
    }

    @Test
    void deveRetornarVazioQuandoPrestadorNaoExiste() {
        assertThat(prestadorRepository.buscarPorId(PrestadorId.novo())).isEmpty();
    }

    @Test
    void deveBuscarPrestadorPorEmailIgnorandoCaixa() {
        Prestador prestador = new Prestador(PrestadorId.novo(), "Clinica Bem Estar", "11987654321",
                "busca-email@exemplo.com", "hash-fake-de-teste", DocumentoFiscal.cnpj("11.444.777/0001-61"),
                new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100)));
        prestadorRepository.salvar(prestador);

        assertThat(prestadorRepository.buscarPorEmail("BUSCA-EMAIL@exemplo.com")).contains(prestador);
        assertThat(prestadorRepository.buscarPorEmail("ninguem@exemplo.com")).isEmpty();
    }
}
