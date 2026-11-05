package br.com.josewilson.agendamento.application.port.out;

import java.util.List;

/** Uma pagina de resultados ja cortada no banco, mais o total de itens (para o X-Total-Count). */
public record Pagina<T>(List<T> itens, long total) {

    /** Corta uma lista ja carregada (adapters em memoria, que nao tem LIMIT/OFFSET). */
    public static <T> Pagina<T> de(List<T> todos, int page, int size) {
        int de = (int) Math.min((long) page * size, todos.size());
        int ate = Math.min(de + size, todos.size());
        return new Pagina<>(todos.subList(de, ate), todos.size());
    }
}
