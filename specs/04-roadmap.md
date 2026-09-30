# Roadmap

## Entregue

- **Fase 0 — Setup**: projeto Spring Boot 3.5.11 + Java 21, dependências (web, data-jpa, validation, postgresql, flyway, testcontainers, archunit, wiremock), `docker-compose.yml` com Postgres local.
- **Fase 1 — Domínio**: entidades, value objects, máquina de estados, `VerificadorDeConflito`, exceções de domínio. Testes unitários puros (sem Spring). Ver `01-dominio.md`.
- **Fase 2 — Casos de uso + adapters em memória**: as 7 portas de entrada, portas de saída, eventos de domínio, adapters em memória (repositórios, gateway de pagamento fake, publicador de eventos, clock real). `ArchitectureTest` (ArchUnit). Smoke test do contexto Spring completo em memória. Ver `02-casos-de-uso.md`.
- **Fase 3 — Persistência real + API REST**: entidades JPA, mappers, migrations Flyway, repository adapters PostgreSQL, testes de integração com Testcontainers (pendente de execução — requer Docker Desktop rodando neste ambiente), troca de adapter em memória ↔ PostgreSQL via `@Profile("dev")`/`@Profile("!dev")`. Controllers REST + DTOs + `GlobalExceptionHandler`, testados com `@WebMvcTest`. Ver `03-api-rest.md`.

## Pendente — Fase 4 (integrações externas, uma por vez)

1. **Gateway de pagamento Pix real** — implementar um gateway real primeiro (Asaas ou Efí, que têm sandbox gratuito), substituindo `GatewayDePagamentoFake`, testado com WireMock (já é dependência do projeto). Depois um segundo gateway só para demonstrar a troca via config — é o diferencial mais forte do projeto.
2. **Execução de estorno no cancelamento** — hoje `CancelarAgendamentoUseCase` retorna `ResultadoCancelamento` mas não aciona nenhum reembolso real; isso depende do gateway real existir e de decidir como referenciar a cobrança original a estornar.
3. **Scheduler de expiração** — `ExpirarReservasPendentesUseCase` existe e está testado, mas nada o aciona periodicamente ainda. Precisa de um `@Scheduled` em `adapter/in/scheduler/`.
4. **Notificações multicanal** (WhatsApp, e-mail, SMS) — porta `EnviadorDeNotificacao` ainda não existe; será criada junto com o primeiro adapter real, reagindo aos eventos de domínio já publicados (`AgendamentoCriado`, `AgendamentoConfirmado`, `AgendamentoCancelado`).
5. **Sincronização com Google Calendar** — porta `CalendarioExternoPort` ainda não existe; mesma lógica de reagir a eventos de domínio.
