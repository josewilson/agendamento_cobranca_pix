package org.example.agendamento.adapter.in.web.security;

import org.example.agendamento.domain.model.prestador.Prestador;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class PrestadorPrincipal implements UserDetails {

    private final Prestador prestador;

    public PrestadorPrincipal(Prestador prestador) {
        this.prestador = prestador;
    }

    public PrestadorId prestadorId() {
        return prestador.id();
    }

    public String nome() {
        return prestador.nome();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_PRESTADOR"));
    }

    @Override
    public String getPassword() {
        return prestador.senhaHash();
    }

    @Override
    public String getUsername() {
        return prestador.email();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
