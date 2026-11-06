package br.com.josewilson.agendamento.adapter.out.persistence.jpa;

import br.com.josewilson.agendamento.adapter.out.persistence.jpa.mapper.ProfissionalMapper;
import br.com.josewilson.agendamento.adapter.out.persistence.jpa.repository.ProfissionalJpaRepository;
import br.com.josewilson.agendamento.application.port.out.ProfissionalRepository;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.profissional.Profissional;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("!dev")
public class ProfissionalRepositoryAdapter implements ProfissionalRepository {

    private final ProfissionalJpaRepository jpaRepository;

    public ProfissionalRepositoryAdapter(ProfissionalJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Profissional salvar(Profissional profissional) {
        return ProfissionalMapper.paraDominio(jpaRepository.saveAndFlush(ProfissionalMapper.paraJpa(profissional)));
    }

    @Override
    public Optional<Profissional> buscarPorId(ProfissionalId id) {
        return jpaRepository.findById(id.valor()).map(ProfissionalMapper::paraDominio);
    }

    @Override
    public List<Profissional> buscarPorEstabelecimento(PrestadorId estabelecimentoId) {
        return jpaRepository.findByEstabelecimentoIdOrderByNomeAsc(estabelecimentoId.valor()).stream()
                .map(ProfissionalMapper::paraDominio).toList();
    }

    @Override
    public void excluir(ProfissionalId id) {
        jpaRepository.deleteById(id.valor());
        jpaRepository.flush();
    }
}
