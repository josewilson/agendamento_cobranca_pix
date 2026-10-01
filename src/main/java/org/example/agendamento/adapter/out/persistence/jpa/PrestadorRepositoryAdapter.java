package org.example.agendamento.adapter.out.persistence.jpa;

import org.example.agendamento.adapter.out.persistence.jpa.mapper.PrestadorMapper;
import org.example.agendamento.adapter.out.persistence.jpa.repository.PrestadorJpaRepository;
import org.example.agendamento.application.port.out.PrestadorRepository;
import org.example.agendamento.domain.model.prestador.Prestador;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("!dev")
public class PrestadorRepositoryAdapter implements PrestadorRepository {

    private final PrestadorJpaRepository jpaRepository;

    public PrestadorRepositoryAdapter(PrestadorJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Prestador salvar(Prestador prestador) {
        return PrestadorMapper.paraDominio(jpaRepository.save(PrestadorMapper.paraJpa(prestador)));
    }

    @Override
    public Optional<Prestador> buscarPorId(PrestadorId id) {
        return jpaRepository.findById(id.valor()).map(PrestadorMapper::paraDominio);
    }

    @Override
    public Optional<Prestador> buscarPorEmail(String email) {
        return jpaRepository.findByEmailIgnoreCase(email).map(PrestadorMapper::paraDominio);
    }

    @Override
    public List<Prestador> buscarTodos() {
        return jpaRepository.findAll().stream().map(PrestadorMapper::paraDominio).toList();
    }
}
