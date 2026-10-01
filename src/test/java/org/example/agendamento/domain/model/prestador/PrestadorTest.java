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

    private static final String EMAIL = "clinica@exemplo.com";
    private static final String SENHA_HASH = "hash-fake-de-teste";

    @Test
    void deveCriarPrestadorValido() {
        Prestador prestador = new Prestador(PrestadorId.novo(), "Clinica Bem Estar", "11987654321",
                EMAIL, SENHA_HASH, documento, politica);

        assertThat(prestador.nome()).isEqualTo("Clinica Bem Estar");
        assertThat(prestador.telefone()).isEqualTo("11987654321");
        assertThat(prestador.email()).isEqualTo(EMAIL);
        assertThat(prestador.politicaCancelamentoPadrao()).isEqualTo(politica);
    }

    @Test
    void deveLancarExcecaoParaNomeVazio() {
        assertThatThrownBy(() -> new Prestador(PrestadorId.novo(), "", "11987654321", EMAIL, SENHA_HASH, documento, politica))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveLancarExcecaoParaTelefoneInvalido() {
        assertThatThrownBy(() -> new Prestador(PrestadorId.novo(), "Clinica Bem Estar", "123", EMAIL, SENHA_HASH, documento, politica))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveLancarExcecaoParaEmailInvalido() {
        assertThatThrownBy(() -> new Prestador(PrestadorId.novo(), "Clinica Bem Estar", "11987654321",
                "nao-e-email", SENHA_HASH, documento, politica))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
