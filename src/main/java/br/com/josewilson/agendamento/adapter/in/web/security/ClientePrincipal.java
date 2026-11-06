package br.com.josewilson.agendamento.adapter.in.web.security;

import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

/** Principal da sessao de um Cliente logado (so id e nome; nunca guarda o hash da senha). */
public class ClientePrincipal {

    private final ClienteId clienteId;
    private final String nome;

    public ClientePrincipal(Cliente cliente) {
        this.clienteId = cliente.id();
        this.nome = cliente.nome();
    }

    public ClienteId clienteId() {
        return clienteId;
    }

    public String nome() {
        return nome;
    }

    public Collection<? extends GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
    }
}
