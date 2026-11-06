package br.com.josewilson.agendamento.domain.model.profissional;

import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;

import java.util.Objects;

/** Quem atende dentro de um estabelecimento (Prestador); cada profissional tem agenda propria. */
public class Profissional {

    private final ProfissionalId id;
    private final PrestadorId estabelecimentoId;
    private String nome;

    public Profissional(ProfissionalId id, PrestadorId estabelecimentoId, String nome) {
        this.id = Objects.requireNonNull(id, "id nao pode ser nulo");
        this.estabelecimentoId = Objects.requireNonNull(estabelecimentoId, "estabelecimentoId nao pode ser nulo");
        this.nome = validarNome(nome);
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome nao pode ser vazio");
        }
        return nome;
    }

    public void renomear(String nome) {
        this.nome = validarNome(nome);
    }

    public ProfissionalId id() { return id; }
    public PrestadorId estabelecimentoId() { return estabelecimentoId; }
    public String nome() { return nome; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Profissional p && id.equals(p.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
