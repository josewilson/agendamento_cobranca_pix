package org.example.agendamento.adapter.out.persistence.jpa.repository;

import org.example.agendamento.adapter.out.persistence.jpa.entity.PrestadorJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PrestadorJpaRepository extends JpaRepository<PrestadorJpaEntity, UUID> {

    Optional<PrestadorJpaEntity> findByEmailIgnoreCase(String email);
}
