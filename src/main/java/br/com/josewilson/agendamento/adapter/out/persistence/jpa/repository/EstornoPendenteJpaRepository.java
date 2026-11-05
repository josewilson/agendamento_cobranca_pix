package br.com.josewilson.agendamento.adapter.out.persistence.jpa.repository;

import br.com.josewilson.agendamento.adapter.out.persistence.jpa.entity.EstornoPendenteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EstornoPendenteJpaRepository extends JpaRepository<EstornoPendenteJpaEntity, UUID> {

    List<EstornoPendenteJpaEntity> findByTentativasLessThanOrderByCriadoEmAsc(int maxTentativas);
}
