# Casos de Uso

Implementados em `src/main/java/org/example/agendamento/application/`. Cada caso de uso é uma porta de entrada (`port/in`, interface + Command/Query) implementada por um `@Service` em `service/`.

## CriarAgendamentoUseCase

**Comando:** `CriarAgendamentoCommand(prestadorId, clienteId, servicoId, periodo)`

1. Busca `Prestador`, `Cliente`, `Servico` pelos IDs — `RecursoNaoEncontradoException` se algum não existir.
2. Busca agendamentos ativos do prestador e chama `VerificadorDeConflito.verificarDisponibilidade` — `ConflitoDeHorarioException` se houver sobreposição.
3. Calcula o valor do sinal (regra 4 em `01-dominio.md`).
4. Cria o agendamento via `Agendamento.criar(...)` usando `Clock.agora()` (nunca `Instant.now()` direto, para manter o caso de uso testável deterministicamente).
5. Se exige sinal, chama `GatewayDePagamento.gerarCobrancaPix(agendamentoId, valorSinal)`.
6. Persiste e publica `AgendamentoCriado`.

**Retorno:** `ResultadoCriacaoAgendamento(agendamento, Optional<CobrancaPix>)`.

## ConfirmarAgendamentoUseCase

**Comando:** `ConfirmarAgendamentoCommand(agendamentoId)`

Busca o agendamento, chama `agendamento.confirmar()`, persiste, publica `AgendamentoConfirmado`. **Não é exposto por nenhum endpoint REST manual** — só é acionado internamente por `ProcessarWebhookPagamentoUseCase` (ver abaixo), já que confirmação deve vir da confirmação real do pagamento, não de uma chamada direta do cliente.

## CancelarAgendamentoUseCase

**Comando:** `CancelarAgendamentoCommand(agendamentoId)`

Busca o agendamento, chama `agendamento.cancelar(clock.agora())`, persiste, publica `AgendamentoCancelado`. Retorna o `ResultadoCancelamento` (valor retido/reembolsado) para o chamador decidir a ação de reembolso — a Fase 2/3 não integra ainda com um gateway real para executar o reembolso automaticamente (ver `04-roadmap.md`).

## MarcarNoShowUseCase

**Comando:** `MarcarNoShowCommand(agendamentoId)`

Busca o agendamento e o cliente associado, chama `agendamento.marcarNoShow(agora)` e `cliente.registrarNoShow()`, persiste ambos.

## ExpirarReservasPendentesUseCase

Sem comando (nenhum parâmetro do chamador — usa `Clock.agora()` internamente). Busca todos os agendamentos `PENDENTE_PAGAMENTO`, chama `expirarSeNecessario` em cada um, persiste os que expiraram. Retorna a quantidade expirada. Pensado para ser acionado por um scheduler (`@Scheduled`) — o scheduler em si ainda não foi implementado (ver `04-roadmap.md`).

## ProcessarWebhookPagamentoUseCase

**Comando:** `WebhookPagamentoCommand(agendamentoId, pagamentoConfirmado)`

Se `pagamentoConfirmado == true`, delega para `ConfirmarAgendamentoUseCase`. Se `false`, não faz nada — cobranças Pix não pagas expiram naturalmente via `ExpirarReservasPendentesUseCase`, não há um estado de "pagamento recusado" modelado.

O `agendamentoId` chega diretamente no comando porque, ao gerar a cobrança Pix, o próprio `agendamentoId` é passado como referência externa ao gateway (`GatewayDePagamento.gerarCobrancaPix(agendamentoId, valor)`) — gateways reais (Asaas, Mercado Pago, Efí) permitem anexar uma referência própria à cobrança e a ecoam de volta no webhook, então não é necessário manter um mapeamento separado referência-externa → agendamento.

## ConsultarAgendamentoUseCase

**Query:** `ConsultarAgendamentoQuery(agendamentoId)`

Busca o agendamento por ID — `RecursoNaoEncontradoException` se não existir. Único caso de uso de leitura pura; existe para servir o `GET /api/agendamentos/{id}`.

## Portas de saída usadas pelos casos de uso

| Porta | Papel |
|---|---|
| `AgendamentoRepository`, `ClienteRepository`, `PrestadorRepository`, `ServicoRepository` | Persistência por agregado. Implementadas tanto em memória (`@Profile("dev")`) quanto via JPA/PostgreSQL (`@Profile("!dev")`). |
| `GatewayDePagamento` | `gerarCobrancaPix(agendamentoId, valor) -> CobrancaPix`. Só existe a implementação fake (`GatewayDePagamentoFake`) até a Fase 4. |
| `PublicadorDeEventos` | `publicar(EventoDeDominio)`. Só existe a implementação em memória (`PublicadorDeEventosEmMemoria`), que apenas acumula os eventos publicados — não há consumidor real ainda (notificações/calendário ficam para a Fase 4). |
| `Clock` | `agora() -> Instant`. Abstrai `Instant.now()` para permitir testes determinísticos de regras sensíveis a tempo (expiração, janela de cancelamento). |
