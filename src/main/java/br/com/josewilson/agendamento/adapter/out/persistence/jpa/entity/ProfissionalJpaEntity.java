package br.com.josewilson.agendamento.adapter.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "profissional")
public class ProfissionalJpaEntity {

    @Id
    private UUID id;

    @Column(name = "estabelecimento_id", nullable = false)
    private UUID estabelecimentoId;

    @Column(nullable = false)
    private String nome;

    public ProfissionalJpaEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getEstabelecimentoId() { return estabelecimentoId; }
    public void setEstabelecimentoId(UUID estabelecimentoId) { this.estabelecimentoId = estabelecimentoId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
