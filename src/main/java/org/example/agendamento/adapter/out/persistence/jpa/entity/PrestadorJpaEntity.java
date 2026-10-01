package org.example.agendamento.adapter.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "prestador")
public class PrestadorJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String telefone;

    @Column(name = "documento_numero", nullable = false)
    private String documentoNumero;

    @Column(name = "documento_tipo", nullable = false)
    private String documentoTipo;

    @Column(name = "politica_antecedencia_minima_segundos", nullable = false)
    private long politicaAntecedenciaMinimaSegundos;

    @Column(name = "politica_percentual_retido", nullable = false)
    private BigDecimal politicaPercentualRetido;

    public PrestadorJpaEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getDocumentoNumero() { return documentoNumero; }
    public void setDocumentoNumero(String documentoNumero) { this.documentoNumero = documentoNumero; }

    public String getDocumentoTipo() { return documentoTipo; }
    public void setDocumentoTipo(String documentoTipo) { this.documentoTipo = documentoTipo; }

    public long getPoliticaAntecedenciaMinimaSegundos() { return politicaAntecedenciaMinimaSegundos; }
    public void setPoliticaAntecedenciaMinimaSegundos(long politicaAntecedenciaMinimaSegundos) {
        this.politicaAntecedenciaMinimaSegundos = politicaAntecedenciaMinimaSegundos;
    }

    public BigDecimal getPoliticaPercentualRetido() { return politicaPercentualRetido; }
    public void setPoliticaPercentualRetido(BigDecimal politicaPercentualRetido) {
        this.politicaPercentualRetido = politicaPercentualRetido;
    }
}
