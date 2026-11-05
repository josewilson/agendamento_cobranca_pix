package br.com.josewilson.agendamento.adapter.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "login_tentativa")
public class LoginTentativaJpaEntity {

    @Id
    private String email;

    @Column(nullable = false)
    private int falhas;

    @Column(name = "bloqueado_ate")
    private Instant bloqueadoAte;

    public LoginTentativaJpaEntity() {
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getFalhas() { return falhas; }
    public void setFalhas(int falhas) { this.falhas = falhas; }
    public Instant getBloqueadoAte() { return bloqueadoAte; }
    public void setBloqueadoAte(Instant bloqueadoAte) { this.bloqueadoAte = bloqueadoAte; }
}
