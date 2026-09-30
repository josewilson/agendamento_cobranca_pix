package org.example.agendamento.adapter.out.persistence.jpa.repository;

import org.example.agendamento.adapter.out.persistence.jpa.entity.ServicoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServicoJpaRepository extends JpaRepository<ServicoJpaEntity, UUID> {
}
