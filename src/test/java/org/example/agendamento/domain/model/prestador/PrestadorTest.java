package org.example.agendamento.domain.model.prestador;

import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrestadorTest {

    private final DocumentoFiscal documento = DocumentoFiscal.cnpj("11.222.333/0001-81");
    private final PoliticaCancelamento politica = new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100));

    @Test
    void deveCriarPrestadorValido() {
        Prestador prestador = new Prestador(PrestadorId.novo(), "Clinica Bem Estar", "11987654321", documento, politica);

        assertThat(prestador.nome()).isEqualTo("Clinica Bem Estar");
        assertThat(prestador.telefone()).isEqualTo("11987654321");
        assertThat(prestador.politicaCancelamentoPadrao()).isEqualTo(politica);
    }

    @Test
    void deveLancarExcecaoParaNomeVazio() {
        assertThatThrownBy(() -> new Prestador(PrestadorId.novo(), "", "11987654321", documento, politica))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveLancarExcecaoParaTelefoneInvalido() {
        assertThatThrownBy(() -> new Prestador(PrestadorId.novo(), "Clinica Bem Estar", "123", documento, politica))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
