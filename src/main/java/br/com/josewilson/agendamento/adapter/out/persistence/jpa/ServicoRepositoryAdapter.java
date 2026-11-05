package br.com.josewilson.agendamento.adapter.out.persistence.jpa;

import br.com.josewilson.agendamento.adapter.out.persistence.jpa.mapper.ServicoMapper;
import br.com.josewilson.agendamento.adapter.out.persistence.jpa.repository.ServicoJpaRepository;
import br.com.josewilson.agendamento.application.port.out.ServicoRepository;
import br.com.josewilson.agendamento.application.port.out.Pagina;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.servico.Servico;
import br.com.josewilson.agendamento.domain.model.servico.ServicoId;
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

    @Override
    public Pagina<Servico> buscarPaginaPorPrestador(PrestadorId prestadorId, int page, int size) {
        var resultado = jpaRepository.findByPrestadorId(prestadorId.valor(), PageRequest.of(page, size, Sort.by("nome")));
        return new Pagina<>(resultado.map(ServicoMapper::paraDominio).getContent(), resultado.getTotalElements());
    }

    @Override
    public void excluir(ServicoId id) {
        jpaRepository.deleteById(id.valor());
    }
}
