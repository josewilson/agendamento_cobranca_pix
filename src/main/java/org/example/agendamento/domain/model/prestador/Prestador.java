package org.example.agendamento.domain.model.prestador;

import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;

import java.util.Objects;

public class Prestador {

    private final PrestadorId id;
    private final String nome;
    private final DocumentoFiscal documento;
    private final PoliticaCancelamento politicaCancelamentoPadrao;

    public Prestador(PrestadorId id, String nome, DocumentoFiscal documento, PoliticaCancelamento politicaCancelamentoPadrao) {
        this.id = Objects.requireNonNull(id, "id nao pode ser nulo");
        this.nome = validarNome(nome);
        this.documento = Objects.requireNonNull(documento, "documento nao pode ser nulo");
        this.politicaCancelamentoPadrao = Objects.requireNonNull(politicaCancelamentoPadrao, "politicaCancelamentoPadrao nao pode ser nula");
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome nao pode ser vazio");
        }
        return nome;
    }

    public PrestadorId id() { return id; }
    public String nome() { return nome; }
    public DocumentoFiscal documento() { return documento; }
    public PoliticaCancelamento politicaCancelamentoPadrao() { return politicaCancelamentoPadrao; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Prestador prestador)) return false;
        return id.equals(prestador.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
