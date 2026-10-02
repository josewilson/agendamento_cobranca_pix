package org.example.agendamento.domain.model.prestador;

import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.example.agendamento.domain.model.shared.FormatoContato;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;

import java.util.Objects;

public class Prestador {

    private final PrestadorId id;
    private String nome;
    private String telefone;
    private final String email;
    private final String senhaHash;
    private final DocumentoFiscal documento;
    private final PoliticaCancelamento politicaCancelamentoPadrao;

    public Prestador(PrestadorId id, String nome, String telefone, String email, String senhaHash,
                      DocumentoFiscal documento, PoliticaCancelamento politicaCancelamentoPadrao) {
        this.id = Objects.requireNonNull(id, "id nao pode ser nulo");
        this.nome = validarNome(nome);
        this.telefone = validarTelefone(telefone);
        this.email = validarEmail(email);
        this.senhaHash = validarSenhaHash(senhaHash);
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
        return FormatoContato.validarTelefone(telefone);
    }

    private static String validarEmail(String email) {
        return FormatoContato.validarEmail(email);
    }

    private static String validarSenhaHash(String senhaHash) {
        if (senhaHash == null || senhaHash.isBlank()) {
            throw new IllegalArgumentException("senhaHash nao pode ser vazio");
        }
        return senhaHash;
    }

    /** Edita nome/telefone do proprio cadastro. Email, senha e documento sao imutaveis por aqui —
     * trocar identidade de login ou documento fiscal exigiria um fluxo proprio (confirmacao por
     * email, nova validacao de documento), fora do escopo desta edicao simples. */
    public void atualizarPerfil(String nome, String telefone) {
        this.nome = validarNome(nome);
        this.telefone = validarTelefone(telefone);
    }

    public PrestadorId id() { return id; }
    public String nome() { return nome; }
    public String telefone() { return telefone; }
    public String email() { return email; }
    public String senhaHash() { return senhaHash; }
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
