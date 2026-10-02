package org.example.agendamento.domain.model.cliente;

import org.example.agendamento.domain.model.shared.Contato;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;

import java.util.Objects;

public class Cliente {

    private static final int LIMITE_NO_SHOW_PARA_SINAL_OBRIGATORIO = 2;

    private final ClienteId id;
    private String nome;
    private Contato contato;
    private final DocumentoFiscal documento;
    private int quantidadeNoShow;

    public Cliente(ClienteId id, String nome, Contato contato, DocumentoFiscal documento) {
        this(id, nome, contato, documento, 0);
    }

    public Cliente(ClienteId id, String nome, Contato contato, DocumentoFiscal documento, int quantidadeNoShow) {
        this.id = Objects.requireNonNull(id, "id nao pode ser nulo");
        this.nome = validarNome(nome);
        this.contato = Objects.requireNonNull(contato, "contato nao pode ser nulo");
        this.documento = Objects.requireNonNull(documento, "documento nao pode ser nulo");
        if (quantidadeNoShow < 0) {
            throw new IllegalArgumentException("quantidadeNoShow nao pode ser negativa");
        }
        this.quantidadeNoShow = quantidadeNoShow;
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome nao pode ser vazio");
        }
        return nome;
    }

    public void registrarNoShow() {
        quantidadeNoShow++;
    }

    /** Edita nome/contato do proprio cadastro. Documento e imutavel — trocar o documento fiscal
     * de um cliente ja existente (com historico de agendamentos/no-show) mudaria a identidade
     * fiscal da pessoa, nao um dado de cadastro. */
    public void atualizarDadosCadastrais(String nome, Contato contato) {
        this.nome = validarNome(nome);
        this.contato = Objects.requireNonNull(contato, "contato nao pode ser nulo");
    }

    public boolean exigeSinalObrigatorio() {
        return quantidadeNoShow >= LIMITE_NO_SHOW_PARA_SINAL_OBRIGATORIO;
    }

    public ClienteId id() { return id; }
    public String nome() { return nome; }
    public Contato contato() { return contato; }
    public DocumentoFiscal documento() { return documento; }
    public int quantidadeNoShow() { return quantidadeNoShow; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente cliente)) return false;
        return id.equals(cliente.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
