package br.com.josewilson.agendamento.adapter.out.persistence.jpa;

import br.com.josewilson.agendamento.adapter.out.persistence.jpa.mapper.BloqueioMapper;
import br.com.josewilson.agendamento.adapter.out.persistence.jpa.repository.BloqueioJpaRepository;
import br.com.josewilson.agendamento.application.port.out.BloqueioRepository;
import br.com.josewilson.agendamento.domain.model.profissional.BloqueioDeAgenda;
import br.com.josewilson.agendamento.domain.model.profissional.BloqueioId;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("!dev")
public class BloqueioRepositoryAdapter implements BloqueioRepository {

    private final BloqueioJpaRepository jpaRepository;

    public BloqueioRepositoryAdapter(BloqueioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public BloqueioDeAgenda salvar(BloqueioDeAgenda bloqueio) {
        return BloqueioMapper.paraDominio(jpaRepository.saveAndFlush(BloqueioMapper.paraJpa(bloqueio)));
    }

    @Override
    public Optional<BloqueioDeAgenda> buscarPorId(BloqueioId id) {
        return jpaRepository.findById(id.valor()).map(BloqueioMapper::paraDominio);
    }

    @Override
    public List<BloqueioDeAgenda> buscarPorProfissional(ProfissionalId profissionalId) {
        return jpaRepository.findByProfissionalIdOrderByPeriodoInicioAsc(profissionalId.valor()).stream()
                .map(BloqueioMapper::paraDominio).toList();
    }

    @Override
    public void excluir(BloqueioId id) {
        jpaRepository.deleteById(id.valor());
    }
}
