package br.com.josewilson.agendamento.adapter.in.web;

import br.com.josewilson.agendamento.application.port.out.Pagina;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.function.Function;

/**
 * Paginacao opt-in para os endpoints de listagem (GET /api/prestadores, /api/clientes,
 * /api/servicos, /api/agendamentos): sem "page"/"size" na query string, devolve a colecao
 * inteira (as telas de formulario do frontend usam esses endpoints pra popular um select e
 * precisam da lista completa); com qualquer um dos dois, o corte e feito no banco
 * (LIMIT/OFFSET via Pagina do port de repositorio). O total vai sempre no header
 * X-Total-Count, mesmo sem paginacao.
 */
final class Paginacao {

    private static final int TAMANHO_PADRAO = 20;

    private Paginacao() {
    }

    static boolean paginado(Integer page, Integer size) {
        return page != null || size != null;
    }

    static int pagina(Integer page) {
        return Math.max(page == null ? 0 : page, 0);
    }

    static int tamanho(Integer size) {
        return Math.max(size == null ? TAMANHO_PADRAO : size, 1);
    }

    static <T> ResponseEntity<List<T>> lista(List<T> todos) {
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(todos.size())).body(todos);
    }

    static <T, R> ResponseEntity<List<R>> pagina(Pagina<T> pagina, Function<T, R> mapeador) {
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(pagina.total()))
                .body(pagina.itens().stream().map(mapeador).toList());
    }
}
