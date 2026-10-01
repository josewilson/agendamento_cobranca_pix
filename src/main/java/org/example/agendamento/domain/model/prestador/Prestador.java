package org.example.agendamento.domain.model.prestador;

import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;

import java.util.Objects;
import java.util.regex.Pattern;

public class Prestador {

    private static final Pattern TELEFONE_PATTERN = Pattern.compile("^\\+?\\d{10,14}$");

    private final PrestadorId id;
    private final String nome;
    private final String telefone;
    private final DocumentoFiscal documento;
    private final PoliticaCancelamento politicaCancelamentoPadrao;

    public Prestador(PrestadorId id, String nome, String telefone, DocumentoFiscal documento,
                      PoliticaCancelamento politicaCancelamentoPadrao) {
        this.id = Objects.requireNonNull(id, "id nao pode ser nulo");
        this.nome = validarNome(nome);
        this.telefone = validarTelefone(telefone);
        this.documento = Objects.requireNonNull(documento, "documento nao pode ser nulo");
        this.politicaCancelamentoPadrao = Objects.requireNonNull(politicaCancelamentoPadrao, "politicaCancelamentoPadrao nao pode ser nula");
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome nao pode ser vazio");
        }
        return nome;
    }

    private static String validarTelefone(String telefone) {
        Objects.requireNonNull(telefone, "telefone nao pode ser nulo");
        String telefoneNormalizado = telefone.replaceAll("[\\s()-]", "");
        if (!TELEFONE_PATTERN.matcher(telefoneNormalizado).matches()) {
            throw new IllegalArgumentException("telefone invalido: " + telefone);
        }
        return telefoneNormalizado;
    }

    public PrestadorId id() { return id; }
    public String nome() { return nome; }
    public String telefone() { return telefone; }
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
