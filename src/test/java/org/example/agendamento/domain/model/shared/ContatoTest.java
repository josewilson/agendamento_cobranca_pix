package org.example.agendamento.domain.model.shared;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContatoTest {

    @Test
    void deveAceitarEmailETelefoneValidos() {
        Contato contato = new Contato("cliente@exemplo.com", "11987654321");

        assertThat(contato.email()).isEqualTo("cliente@exemplo.com");
        assertThat(contato.telefone()).isEqualTo("11987654321");
    }

    @Test
    void deveNormalizarTelefoneRemovendoFormatacao() {
        Contato contato = new Contato("cliente@exemplo.com", "(11) 98765-4321");

        assertThat(contato.telefone()).isEqualTo("11987654321");
    }

    @Test
    void deveLancarExcecaoParaEmailInvalido() {
        assertThatThrownBy(() -> new Contato("email-invalido", "11987654321"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveLancarExcecaoParaTelefoneInvalido() {
        assertThatThrownBy(() -> new Contato("cliente@exemplo.com", "123"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
