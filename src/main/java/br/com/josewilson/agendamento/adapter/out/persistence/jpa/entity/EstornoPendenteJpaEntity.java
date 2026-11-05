package br.com.josewilson.agendamento.adapter.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "estorno_pendente")
public class EstornoPendenteJpaEntity {

    @Id
    private UUID id;

    @Column(name = "agendamento_id", nullable = false)
    private UUID agendamentoId;

    @Column(nullable = false)
    private String referencia;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private int tentativas;

    @Column(name = "ultimo_erro")
    private String ultimoErro;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    public EstornoPendenteJpaEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAgendamentoId() { return agendamentoId; }
    public void setAgendamentoId(UUID agendamentoId) { this.agendamentoId = agendamentoId; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public int getTentativas() { return tentativas; }
    public void setTentativas(int tentativas) { this.tentativas = tentativas; }
    public String getUltimoErro() { return ultimoErro; }
    public void setUltimoErro(String ultimoErro) { this.ultimoErro = ultimoErro; }
    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
}
