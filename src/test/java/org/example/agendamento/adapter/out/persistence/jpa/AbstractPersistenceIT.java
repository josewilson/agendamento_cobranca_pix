package org.example.agendamento.adapter.out.persistence.jpa;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * {@code @Transactional} aqui faz cada metodo {@code @Test} das subclasses rodar na sua propria
 * transacao, desfeita (rollback) ao final — sem isso, os metodos de uma mesma classe compartilham
 * o mesmo container Testcontainers sem isolamento nenhum, e o segundo teste que insere um
 * Prestador/Cliente de documento fixo no proprio {@code setUp()} viola a constraint unica
 * (debito tecnico corrigido nesta sessao, ver specs/04-roadmap.md).
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
abstract class AbstractPersistenceIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
