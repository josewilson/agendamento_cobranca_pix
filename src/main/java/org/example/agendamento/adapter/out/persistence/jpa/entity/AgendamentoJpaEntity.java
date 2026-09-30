package org.example.agendamento.adapter.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agendamento")
public class AgendamentoJpaEntity {

    @Id
    private UUID id;

    @Column(name = "prestador_id", nullable = false)
    private UUID prestadorId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "servico_id", nullable = false)
    private UUID servicoId;

    @Column(name = "periodo_inicio", nullable = false)
    private Instant periodoInicio;

    @Column(name = "periodo_fim", nullable = false)
    private Instant periodoFim;

    @Column(name = "valor_servico", nullable = false)
    private BigDecimal valorServico;

    @Column(name = "valor_sinal", nullable = false)
    private BigDecimal valorSinal;

    @Column(name = "politica_antecedencia_minima_segundos", nullable = false)
    private long politicaAntecedenciaMinimaSegundos;

    @Column(name = "politica_percentual_retido", nullable = false)
    private BigDecimal politicaPercentualRetido;

    @Column(nullable = false)
    private String status;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "referencia_pagamento")
    private String referenciaPagamento;

    public AgendamentoJpaEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPrestadorId() { return prestadorId; }
    public void setPrestadorId(UUID prestadorId) { this.prestadorId = prestadorId; }

    public UUID getClienteId() { return clienteId; }
    public void setClienteId(UUID clienteId) { this.clienteId = clienteId; }

    public UUID getServicoId() { return servicoId; }
    public void setServicoId(UUID servicoId) { this.servicoId = servicoId; }

    public Instant getPeriodoInicio() { return periodoInicio; }
    public void setPeriodoInicio(Instant periodoInicio) { this.periodoInicio = periodoInicio; }

    public Instant getPeriodoFim() { return periodoFim; }
    public void setPeriodoFim(Instant periodoFim) { this.periodoFim = periodoFim; }

    public BigDecimal getValorServico() { return valorServico; }
    public void setValorServico(BigDecimal valorServico) { this.valorServico = valorServico; }

    public BigDecimal getValorSinal() { return valorSinal; }
    public void setValorSinal(BigDecimal valorSinal) { this.valorSinal = valorSinal; }

    public long getPoliticaAntecedenciaMinimaSegundos() { return politicaAntecedenciaMinimaSegundos; }
    public void setPoliticaAntecedenciaMinimaSegundos(long politicaAntecedenciaMinimaSegundos) {
        this.politicaAntecedenciaMinimaSegundos = politicaAntecedenciaMinimaSegundos;
    }

    public BigDecimal getPoliticaPercentualRetido() { return politicaPercentualRetido; }
    public void setPoliticaPercentualRetido(BigDecimal politicaPercentualRetido) {
        this.politicaPercentualRetido = politicaPercentualRetido;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }

    public String getReferenciaPagamento() { return referenciaPagamento; }
    public void setReferenciaPagamento(String referenciaPagamento) { this.referenciaPagamento = referenciaPagamento; }
}
