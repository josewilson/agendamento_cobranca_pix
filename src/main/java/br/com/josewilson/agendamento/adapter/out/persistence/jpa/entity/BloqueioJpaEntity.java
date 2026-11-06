package br.com.josewilson.agendamento.adapter.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bloqueio_agenda")
public class BloqueioJpaEntity {

    @Id
    private UUID id;

    @Column(name = "profissional_id", nullable = false)
    private UUID profissionalId;

    @Column(name = "periodo_inicio", nullable = false)
    private Instant periodoInicio;

    @Column(name = "periodo_fim", nullable = false)
    private Instant periodoFim;

    private String motivo;

    public BloqueioJpaEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getProfissionalId() { return profissionalId; }
    public void setProfissionalId(UUID profissionalId) { this.profissionalId = profissionalId; }
    public Instant getPeriodoInicio() { return periodoInicio; }
    public void setPeriodoInicio(Instant periodoInicio) { this.periodoInicio = periodoInicio; }
    public Instant getPeriodoFim() { return periodoFim; }
    public void setPeriodoFim(Instant periodoFim) { this.periodoFim = periodoFim; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
