package org.example.agendamento.adapter.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "servico")
public class ServicoJpaEntity {

    @Id
    private UUID id;

    @Column(name = "prestador_id", nullable = false)
    private UUID prestadorId;

    @Column(nullable = false)
    private String nome;

    @Column(name = "duracao_minutos", nullable = false)
    private long duracaoMinutos;

    @Column(nullable = false)
    private BigDecimal preco;

    @Column(name = "percentual_sinal", nullable = false)
    private BigDecimal percentualSinal;

    public ServicoJpaEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPrestadorId() { return prestadorId; }
    public void setPrestadorId(UUID prestadorId) { this.prestadorId = prestadorId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public long getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(long duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public BigDecimal getPercentualSinal() { return percentualSinal; }
    public void setPercentualSinal(BigDecimal percentualSinal) { this.percentualSinal = percentualSinal; }
}
