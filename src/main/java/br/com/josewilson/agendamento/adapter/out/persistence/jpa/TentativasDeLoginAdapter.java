package br.com.josewilson.agendamento.adapter.out.persistence.jpa;

import br.com.josewilson.agendamento.adapter.out.persistence.jpa.entity.LoginTentativaJpaEntity;
import br.com.josewilson.agendamento.adapter.out.persistence.jpa.repository.LoginTentativaJpaRepository;
import br.com.josewilson.agendamento.application.port.out.TentativasDeLogin;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
@Profile("!dev")
public class TentativasDeLoginAdapter implements TentativasDeLogin {

    private final LoginTentativaJpaRepository jpaRepository;

    public TentativasDeLoginAdapter(LoginTentativaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Instant> bloqueadoAte(String email) {
        return jpaRepository.findById(email).map(LoginTentativaJpaEntity::getBloqueadoAte);
    }

    @Override
    @Transactional
    public void registrarFalha(String email, int maximo, Instant bloqueioAte) {
        jpaRepository.registrarFalha(email, maximo, bloqueioAte);
    }

    @Override
    @Transactional
    public void limpar(String email) {
        jpaRepository.deleteById(email);
    }
}
