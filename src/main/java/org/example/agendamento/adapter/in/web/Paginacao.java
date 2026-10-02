package org.example.agendamento.adapter.in.web;

import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Paginacao opt-in para os endpoints de listagem (GET /api/prestadores, /api/clientes,
 * /api/servicos, /api/agendamentos): sem "page"/"size" na query string, devolve a colecao
 * inteira, exatamente como antes (as telas de formulario do frontend usam esses endpoints
 * pra popular um <select> e precisam da lista completa, nao so uma pagina) — so quando o
 * chamador pede explicitamente uma pagina e que a resposta e cortada. O total de itens vai
 * sempre no header X-Total-Count, mesmo sem paginacao, pra quem quiser saber o tamanho total
 * sem ter que contar o corpo. Achado de auditoria de performance (02/10/2026): endpoints de
 * listagem sem nenhum jeito de pedir um subconjunto limitado nao escalariam com volume real.
 */
final class Paginacao {

    private static final int TAMANHO_PADRAO = 20;

    private Paginacao() {
    }

    static <T> ResponseEntity<List<T>> aplicar(List<T> todos, Integer page, Integer size) {
        ResponseEntity.BodyBuilder resposta = ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(todos.size()));

        if (page == null && size == null) {
            return resposta.body(todos);
        }

        int paginaEfetiva = Math.max(page == null ? 0 : page, 0);
        int tamanhoEfetivo = Math.max(size == null ? TAMANHO_PADRAO : size, 1);
        int de = Math.min(paginaEfetiva * tamanhoEfetivo, todos.size());
        int ate = Math.min(de + tamanhoEfetivo, todos.size());

        return resposta.body(todos.subList(de, ate));
    }
}
