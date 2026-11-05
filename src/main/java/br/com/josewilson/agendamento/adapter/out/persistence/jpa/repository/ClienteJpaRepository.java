package br.com.josewilson.agendamento.adapter.out.persistence.jpa.repository;

import br.com.josewilson.agendamento.adapter.out.persistence.jpa.entity.ClienteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClienteJpaRepository extends JpaRepository<ClienteJpaEntity, UUID> {
}
