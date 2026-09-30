# Visão Geral

## Domínio

Plataforma de agendamento para prestadores de serviço (clínicas, barbearias, estúdios) com cobrança de sinal via Pix.

## Escopo funcional

- Gestão de agendamentos com detecção de conflito de horários.
- Políticas de cancelamento e cobrança de sinal.
- Bloqueio de agenda por histórico de no-show.
- Integração com gateway de pagamento via abstração (porta `GatewayDePagamento`), trocável entre Asaas/Mercado Pago/Efí sem alterar casos de uso.
- Notificações multicanal (WhatsApp, e-mail, SMS) via adapter — **planejado, não implementado** (ver `04-roadmap.md`).
- Sincronização com Google Calendar — **planejado, não implementado** (ver `04-roadmap.md`).

## Stack técnico

Java 21, Spring Boot 3.5.11, PostgreSQL, Flyway, Testcontainers, Docker Compose, ArchUnit, JUnit 5 + AssertJ, Mockito, WireMock (dependência já registrada, uso previsto para a Fase 4).

## Diferenciais de arquitetura

- Domínio em Java puro (sem Spring, sem JPA, sem qualquer framework) — as regras de negócio vivem nas entidades e value objects.
- Mappers dedicados traduzindo entre domínio e entidades JPA — `@Entity` nunca aparece no domínio.
- ArchUnit (`src/test/java/.../architecture/ArchitectureTest.java`) garantindo automaticamente, a cada build, que domínio não depende de Spring/JPA/adapters e que aplicação não depende de adapters/JPA.
- Testes de integração dos adapters de persistência com Testcontainers (PostgreSQL real, não H2/mock).
- Troca de adapter de persistência (memória ↔ PostgreSQL) via `@Profile` do Spring, sem tocar em código de aplicação ou domínio — demonstração prática da inversão de dependência do hexágono.

## Estrutura de documentos desta pasta

- `01-dominio.md` — entidades, value objects, máquina de estados e regras de negócio críticas.
- `02-casos-de-uso.md` — casos de uso da camada de aplicação, com pré-condições e comportamento.
- `03-api-rest.md` — contrato da API REST exposta pela Fase 3.
- `04-roadmap.md` — o que já foi entregue por fase e o que resta (Fase 4).
