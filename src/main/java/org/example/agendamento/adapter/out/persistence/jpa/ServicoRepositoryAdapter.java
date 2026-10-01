package org.example.agendamento.adapter.out.persistence.jpa;

import org.example.agendamento.adapter.out.persistence.jpa.mapper.ServicoMapper;
import org.example.agendamento.adapter.out.persistence.jpa.repository.ServicoJpaRepository;
import org.example.agendamento.application.port.out.ServicoRepository;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.Servico;
import org.example.agendamento.domain.model.servico.ServicoId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("!dev")
public class ServicoRepositoryAdapter implements ServicoRepository {

    private final ServicoJpaRepository jpaRepository;

    public ServicoRepositoryAdapter(ServicoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Servico salvar(Servico servico) {
        return ServicoMapper.paraDominio(jpaRepository.save(ServicoMapper.paraJpa(servico)));
    }

    @Override
    public Optional<Servico> buscarPorId(ServicoId id) {
        return jpaRepository.findById(id.valor()).map(ServicoMapper::paraDominio);
    }

    @Override
    public List<Servico> buscarPorPrestador(PrestadorId prestadorId) {
        return jpaRepository.findByPrestadorId(prestadorId.valor()).stream()
                .map(ServicoMapper::paraDominio)
                .toList();
    }
}
