package org.example.agendamento.domain.model.shared;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentoFiscalTest {

    @Test
    void deveAceitarCpfValido() {
        DocumentoFiscal documento = DocumentoFiscal.cpf("111.444.777-35");

        assertThat(documento.numero()).isEqualTo("11144477735");
        assertThat(documento.tipo()).isEqualTo(DocumentoFiscal.TipoDocumento.CPF);
    }

    @Test
    void deveRejeitarCpfComDigitosTodosIguais() {
        assertThatThrownBy(() -> DocumentoFiscal.cpf("111.111.111-11"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveRejeitarCpfComDigitoVerificadorInvalido() {
        assertThatThrownBy(() -> DocumentoFiscal.cpf("111.444.777-36"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveAceitarCnpjValido() {
        DocumentoFiscal documento = DocumentoFiscal.cnpj("11.222.333/0001-81");

        assertThat(documento.numero()).isEqualTo("11222333000181");
        assertThat(documento.tipo()).isEqualTo(DocumentoFiscal.TipoDocumento.CNPJ);
    }

    @Test
    void deveRejeitarCnpjComDigitoVerificadorInvalido() {
        assertThatThrownBy(() -> DocumentoFiscal.cnpj("11.222.333/0001-82"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveRejeitarDocumentoComTamanhoIncorreto() {
        assertThatThrownBy(() -> DocumentoFiscal.cpf("123"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
