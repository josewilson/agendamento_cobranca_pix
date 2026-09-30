# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Agendamento com cobrança via Pix — a scheduling platform for service providers (clinics, barbershops, studios) with Pix deposit/cancellation-fee billing. Hexagonal-architecture portfolio project in Java 21 + Spring Boot 3.5.11.

## Commands

No `mvn` executable or Maven wrapper (`mvnw`) is on PATH in this environment, and `.mvn/` has no wrapper jar. Use the JetBrains IDE MCP tools instead:

- Build/compile: `mcp__idea__build_project`. Pass `rebuild: true` if an incremental build reports success but a test still fails on a stale class — this has happened after editing a file many others depend on (e.g. adding `@Profile` to several adapters at once).
- Run a single test class or method: `mcp__idea__get_run_configurations` with `filePath` to get the line number of the class/method, then `mcp__idea__execute_run_configuration` with that `filePath` + `line`.
- If a real `mvn`/`mvnw` becomes available, the equivalents are `mvn compile`, `mvn test`, `mvn -Dtest=ClassName#methodName test`, `mvn spring-boot:run`.

### Running the app / integration tests locally

- `docker-compose up -d` starts the Postgres instance the default Spring profile needs (datasource config in `src/main/resources/application.yml`; schema comes from Flyway migrations in `src/main/resources/db/migration`, validated against the JPA entities at startup via `spring.jpa.hibernate.ddl-auto=validate`).
- The `*RepositoryAdapterIT` classes under `adapter/out/persistence/jpa` use Testcontainers (a throwaway Postgres container, independent of docker-compose) — Docker Desktop must be running or they fail to start the container.
- Spring profile `dev` swaps every persistence adapter for the in-memory ones (see Architecture below) — no Postgres needed. `AgendamentoApplicationTests` (the context-loads smoke test) runs under `@ActiveProfiles("dev")` for this reason.

## Architecture

Hexagonal (ports & adapters), enforced by `src/test/java/.../architecture/ArchitectureTest.java` (ArchUnit): domain must not depend on Spring, JPA, or `adapter.*`; application must not depend on `adapter.*` or JPA; no package cycles between `domain`/`application`/`adapter`.

- **`domain/`** — pure Java, zero framework imports. `model/` has the entities (`Agendamento`, `Cliente`, `Prestador`, `Servico` — mutable, identity-equals) and value objects (`Periodo`, `Dinheiro`, `Contato`, `DocumentoFiscal`, `PoliticaCancelamento` — records with validating compact constructors). `StatusAgendamento`'s `transicoesValidas()` switch is the single source of truth for the appointment state machine. `service/VerificadorDeConflito` is the one domain service (schedule-conflict check). `event/` holds a `sealed interface EventoDeDominio` and its record implementations.
  - `Agendamento.criar(...)` is the only way to create a *new* appointment — validates "not in the past" and derives the initial status from whether a deposit is owed. `Agendamento.reconstituir(...)` is the separate path persistence adapters use to rehydrate a row without re-running that creation-time validation (a historical, already-completed appointment must still load even though its period is in the past).
  - Money is always `Dinheiro` (wraps `BigDecimal`, scale normalized to 2 via `HALF_EVEN` in its compact constructor) — never `double`/`float`.
- **`application/`** — use-case orchestration. `port/in` has one interface + Command/Query record per use case (create, confirm, cancel, mark no-show, expire pending reservations, process payment webhook, query by id). `port/out` has one repository interface per aggregate plus `GatewayDePagamento`, `PublicadorDeEventos`, and `Clock` (abstracts `Instant.now()` so time-dependent rules are deterministically testable). `service/*Service` implement the `in` ports as `@Service` beans — application *is* allowed to depend on Spring, only `domain` is framework-free. `ProcessarWebhookPagamentoService` just delegates to `ConfirmarAgendamentoUseCase`; there's intentionally no other way to confirm an appointment (no manual "confirmar" REST endpoint).
- **`adapter/in/web/`** — REST controllers + one DTO per request/response + `GlobalExceptionHandler` mapping domain/application exceptions to HTTP status (404 `RecursoNaoEncontradoException`, 409 `ConflitoDeHorarioException`/`TransicaoDeStatusInvalidaException`, 400 bean-validation failures and `IllegalArgumentException`/`IllegalStateException`).
- **`adapter/out/persistence/jpa/`** — real Postgres adapters. `entity/` classes are flat JPA `@Entity` POJOs (no domain types, no embeddables) with a **public** no-arg constructor — it has to be public because `mapper/` lives in a sibling package, not the same one, so `protected` isn't reachable from there. `mapper/` classes do all domain↔JPA translation, including reconstructing value objects (which re-runs their validation on every load). `repository/` are plain `JpaRepository` interfaces; the `*RepositoryAdapter` classes wrap them and implement the `application/port/out` interfaces.
- **`adapter/out/persistence/memory/`**, `adapter/out/pagamento/memory/GatewayDePagamentoFake`, `adapter/out/evento/PublicadorDeEventosEmMemoria`, `adapter/out/clock/SystemClockAdapter` — in-memory/fake adapters. The persistence ones and the JPA ones implement the *same* `application/port/out` interfaces and are mutually exclusive via Spring profiles: `@Profile("dev")` on the in-memory repositories, `@Profile("!dev")` on the JPA ones — swapping the persistence technology is a config change only, never a code change. Application-service tests also use the in-memory adapters directly as their test doubles (`new InMemoryXxxRepository()`), not mocks, since they're the real adapter rather than test-only code. `GatewayDePagamentoFake`, `PublicadorDeEventosEmMemoria`, and `SystemClockAdapter` have no competing real implementation yet, so they carry no `@Profile` restriction.

## Testing strategy

- Domain tests: plain JUnit 5 + AssertJ, no Spring context. `AgendamentoTestDataBuilder` builds the aggregate with the most constructor parameters.
- Application-service tests: the real in-memory adapters (see above) plus a hand-written `ClockFixo` test double (package-private, lives in `application/service/` test sources) for deterministic time control.
- Web tests: `@WebMvcTest` per controller, use cases mocked with `@MockitoBean` (not the deprecated `@MockBean`).
- Persistence tests: `*RepositoryAdapterIT` classes extend `AbstractPersistenceIT` (`@Testcontainers` + a shared `PostgreSQLContainer` + `@DynamicPropertySource` wiring the datasource) — see Commands above for the Docker requirement.
- `pom.xml` registers Mockito's byte-buddy agent via `maven-dependency-plugin`'s `properties` goal plus `-javaagent` in the surefire `argLine`, per Mockito's own recommendation, to avoid the JDK's dynamic-agent-loading deprecation warning.
