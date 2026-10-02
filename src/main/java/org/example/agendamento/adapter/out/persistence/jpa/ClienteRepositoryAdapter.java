package org.example.agendamento.adapter.out.persistence.jpa;

import org.example.agendamento.adapter.out.persistence.jpa.mapper.ClienteMapper;
import org.example.agendamento.adapter.out.persistence.jpa.repository.ClienteJpaRepository;
import org.example.agendamento.application.port.out.ClienteRepository;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("!dev")
public class ClienteRepositoryAdapter implements ClienteRepository {

    private final ClienteJpaRepository jpaRepository;

    public ClienteRepositoryAdapter(ClienteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        return ClienteMapper.paraDominio(jpaRepository.save(ClienteMapper.paraJpa(cliente)));
    }

    @Override
    public Optional<Cliente> buscarPorId(ClienteId id) {
        return jpaRepository.findById(id.valor()).map(ClienteMapper::paraDominio);
    }

    @Override
    public List<Cliente> buscarTodos() {
        return jpaRepository.findAll().stream().map(ClienteMapper::paraDominio).toList();
    }

    @Override
    public void excluir(ClienteId id) {
        jpaRepository.deleteById(id.valor());
    }
}
