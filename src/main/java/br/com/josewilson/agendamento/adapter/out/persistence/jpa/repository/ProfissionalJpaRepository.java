package br.com.josewilson.agendamento.adapter.out.persistence.jpa.repository;

import br.com.josewilson.agendamento.adapter.out.persistence.jpa.entity.ProfissionalJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProfissionalJpaRepository extends JpaRepository<ProfissionalJpaEntity, UUID> {

    List<ProfissionalJpaEntity> findByEstabelecimentoIdOrderByNomeAsc(UUID estabelecimentoId);
}
