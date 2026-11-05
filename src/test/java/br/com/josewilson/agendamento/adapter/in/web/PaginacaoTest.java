package br.com.josewilson.agendamento.adapter.in.web;

import br.com.josewilson.agendamento.application.port.out.Pagina;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaginacaoTest {

    @Test
    void semPageNemSizeNaoEPaginado() {
        assertThat(Paginacao.paginado(null, null)).isFalse();
        assertThat(Paginacao.paginado(0, null)).isTrue();
        assertThat(Paginacao.paginado(null, 5)).isTrue();
    }

    @Test
    void pageSemSizeUsaTamanhoPadrao() {
        assertThat(Paginacao.tamanho(null)).isEqualTo(20);
        assertThat(Paginacao.pagina(null)).isZero();
    }

    @Test
    void pageOuSizeNegativosSaoTratadosComoZeroOuUm() {
        assertThat(Paginacao.pagina(-5)).isZero();
        assertThat(Paginacao.tamanho(-5)).isEqualTo(1);
    }

    @Test
    void listaCompletaTrazOTotalNoHeader() {
        ResponseEntity<List<Integer>> resposta = Paginacao.lista(List.of(1, 2, 3));

        assertThat(resposta.getBody()).containsExactly(1, 2, 3);
        assertThat(resposta.getHeaders().getFirst("X-Total-Count")).isEqualTo("3");
    }

    @Test
    void paginaTrazOTotalDoBancoNoHeaderEMapeiaOsItens() {
        ResponseEntity<List<String>> resposta = Paginacao.pagina(new Pagina<>(List.of(1, 2), 7L), n -> "n" + n);

        assertThat(resposta.getBody()).containsExactly("n1", "n2");
        assertThat(resposta.getHeaders().getFirst("X-Total-Count")).isEqualTo("7");
    }

    @Test
    void paginaDeCortaListaEmMemoria() {
        List<Integer> todos = List.of(1, 2, 3, 4, 5);

        assertThat(Pagina.de(todos, 0, 2).itens()).containsExactly(1, 2);
        assertThat(Pagina.de(todos, 1, 2).itens()).containsExactly(3, 4);
        assertThat(Pagina.de(todos, 10, 2).itens()).isEmpty();
        assertThat(Pagina.de(todos, 1, 2).total()).isEqualTo(5);
    }
}
