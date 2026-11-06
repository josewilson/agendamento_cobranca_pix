package br.com.josewilson.agendamento.adapter.out.persistence.jpa.repository;

import br.com.josewilson.agendamento.adapter.out.persistence.jpa.entity.BloqueioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BloqueioJpaRepository extends JpaRepository<BloqueioJpaEntity, UUID> {

    List<BloqueioJpaEntity> findByProfissionalIdOrderByPeriodoInicioAsc(UUID profissionalId);
}
