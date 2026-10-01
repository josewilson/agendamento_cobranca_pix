package org.example.agendamento.adapter.in.web.security;

import org.example.agendamento.application.port.out.PrestadorRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PrestadorUserDetailsService implements UserDetailsService {

    private final PrestadorRepository prestadorRepository;

    public PrestadorUserDetailsService(PrestadorRepository prestadorRepository) {
        this.prestadorRepository = prestadorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return prestadorRepository.buscarPorEmail(email)
                .map(PrestadorPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("prestador nao encontrado: " + email));
    }
}
