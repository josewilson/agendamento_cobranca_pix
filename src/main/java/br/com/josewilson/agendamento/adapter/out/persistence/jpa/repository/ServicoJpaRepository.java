package br.com.josewilson.agendamento.adapter.out.persistence.jpa.repository;

import br.com.josewilson.agendamento.adapter.out.persistence.jpa.entity.ServicoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServicoJpaRepository extends JpaRepository<ServicoJpaEntity, UUID> {

    List<ServicoJpaEntity> findByPrestadorId(UUID prestadorId);

    org.springframework.data.domain.Page<ServicoJpaEntity> findByPrestadorId(UUID prestadorId, org.springframework.data.domain.Pageable pageable);
}
