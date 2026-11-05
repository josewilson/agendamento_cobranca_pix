package br.com.josewilson.agendamento.adapter.out.persistence.jpa.repository;

import br.com.josewilson.agendamento.adapter.out.persistence.jpa.entity.AgendamentoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgendamentoJpaRepository extends JpaRepository<AgendamentoJpaEntity, UUID> {

    List<AgendamentoJpaEntity> findByPrestadorIdAndStatusIn(UUID prestadorId, List<String> status);

    List<AgendamentoJpaEntity> findByStatus(String status);

    List<AgendamentoJpaEntity> findByPrestadorIdOrderByPeriodoInicioAsc(UUID prestadorId);

    org.springframework.data.domain.Page<AgendamentoJpaEntity> findByPrestadorIdOrderByPeriodoInicioAsc(
            UUID prestadorId, org.springframework.data.domain.Pageable pageable);
}
