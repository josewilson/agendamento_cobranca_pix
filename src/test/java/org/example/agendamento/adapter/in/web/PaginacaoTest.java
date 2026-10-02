package org.example.agendamento.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaginacaoTest {

    @Test
    void semPageNemSizeDevolveTudo() {
        List<Integer> todos = List.of(1, 2, 3, 4, 5);

        ResponseEntity<List<Integer>> resposta = Paginacao.aplicar(todos, null, null);

        assertThat(resposta.getBody()).containsExactly(1, 2, 3, 4, 5);
        assertThat(resposta.getHeaders().getFirst("X-Total-Count")).isEqualTo("5");
    }

    @Test
    void comSizeCortaNoTamanhoPedido() {
        List<Integer> todos = List.of(1, 2, 3, 4, 5);

        ResponseEntity<List<Integer>> resposta = Paginacao.aplicar(todos, 0, 2);

        assertThat(resposta.getBody()).containsExactly(1, 2);
        assertThat(resposta.getHeaders().getFirst("X-Total-Count")).isEqualTo("5");
    }

    @Test
    void paginaSeguinteDevolveOProximoBloco() {
        List<Integer> todos = List.of(1, 2, 3, 4, 5);

        ResponseEntity<List<Integer>> resposta = Paginacao.aplicar(todos, 1, 2);

        assertThat(resposta.getBody()).containsExactly(3, 4);
    }

    @Test
    void paginaAlemDoFimDevolveListaVazia() {
        List<Integer> todos = List.of(1, 2, 3);

        ResponseEntity<List<Integer>> resposta = Paginacao.aplicar(todos, 10, 2);

        assertThat(resposta.getBody()).isEmpty();
        assertThat(resposta.getHeaders().getFirst("X-Total-Count")).isEqualTo("3");
    }

    @Test
    void pageSemSizeUsaTamanhoPadrao() {
        List<Integer> todos = java.util.stream.IntStream.rangeClosed(1, 30).boxed().toList();

        ResponseEntity<List<Integer>> resposta = Paginacao.aplicar(todos, 1, null);

        assertThat(resposta.getBody()).hasSize(10);
        assertThat(resposta.getBody().getFirst()).isEqualTo(21);
    }

    @Test
    void pageOuSizeNegativosSaoTratadosComoZeroOuUm() {
        List<Integer> todos = List.of(1, 2, 3);

        ResponseEntity<List<Integer>> resposta = Paginacao.aplicar(todos, -5, -5);

        assertThat(resposta.getBody()).containsExactly(1);
    }
}
