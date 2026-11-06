package br.com.josewilson.agendamento.adapter.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "cliente")
public class ClienteJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String telefone;

    @Column(name = "documento_numero", nullable = false)
    private String documentoNumero;

    @Column(name = "documento_tipo", nullable = false)
    private String documentoTipo;

    @Column(name = "quantidade_no_show", nullable = false)
    private int quantidadeNoShow;

    @Column(name = "senha_hash")
    private String senhaHash;

    @Column(name = "canal_preferido", nullable = false)
    private String canalPreferido = "AMBOS";

    public ClienteJpaEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getDocumentoNumero() { return documentoNumero; }
    public void setDocumentoNumero(String documentoNumero) { this.documentoNumero = documentoNumero; }

    public String getDocumentoTipo() { return documentoTipo; }
    public void setDocumentoTipo(String documentoTipo) { this.documentoTipo = documentoTipo; }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public String getCanalPreferido() { return canalPreferido; }
    public void setCanalPreferido(String canalPreferido) { this.canalPreferido = canalPreferido; }

    public int getQuantidadeNoShow() { return quantidadeNoShow; }
    public void setQuantidadeNoShow(int quantidadeNoShow) { this.quantidadeNoShow = quantidadeNoShow; }
}
