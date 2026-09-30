# Casos de Uso

Implementados em `src/main/java/org/example/agendamento/application/`. Cada caso de uso é uma porta de entrada (`port/in`, interface + Command/Query) implementada por um `@Service` em `service/`.

## CriarAgendamentoUseCase

**Comando:** `CriarAgendamentoCommand(prestadorId, clienteId, servicoId, periodo)`

1. Busca `Prestador`, `Cliente`, `Servico` pelos IDs — `RecursoNaoEncontradoException` se algum não existir.
2. Busca agendamentos ativos do prestador e chama `VerificadorDeConflito.verificarDisponibilidade` — `ConflitoDeHorarioException` se houver sobreposição.
3. Calcula o valor do sinal (regra 4 em `01-dominio.md`).
4. Cria o agendamento via `Agendamento.criar(...)` usando `Clock.agora()` (nunca `Instant.now()` direto, para manter o caso de uso testável deterministicamente).
5. Se exige sinal, chama `GatewayDePagamento.gerarCobrancaPix(agendamentoId, cliente, valorSinal)` — o `Cliente` é necessário porque gateways reais (Asaas) exigem nome/documento/contato para criar o cliente do lado deles antes de emitir a cobrança.
6. Persiste e publica `AgendamentoCriado`.

**Retorno:** `ResultadoCriacaoAgendamento(agendamento, Optional<CobrancaPix>)`.

## ConfirmarAgendamentoUseCase

**Comando:** `ConfirmarAgendamentoCommand(agendamentoId)`

Busca o agendamento, chama `agendamento.confirmar()`, persiste, publica `AgendamentoConfirmado(agendamentoId, clienteId, ocorridoEm)`. **Não é exposto por nenhum endpoint REST manual** — só é acionado internamente por `ProcessarWebhookPagamentoUseCase` (ver abaixo), já que confirmação deve vir da confirmação real do pagamento, não de uma chamada direta do cliente.

## CancelarAgendamentoUseCase

**Comando:** `CancelarAgendamentoCommand(agendamentoId)`

Busca o agendamento, chama `agendamento.cancelar(clock.agora())`, persiste, publica `AgendamentoCancelado(agendamentoId, clienteId, resultado, ocorridoEm)`. Retorna o `ResultadoCancelamento` (valor retido/reembolsado) para o chamador decidir a ação de reembolso — a Fase 2/3 não integra ainda com um gateway real para executar o reembolso automaticamente (ver `04-roadmap.md`).

## MarcarNoShowUseCase

**Comando:** `MarcarNoShowCommand(agendamentoId)`

Busca o agendamento e o cliente associado, chama `agendamento.marcarNoShow(agora)` e `cliente.registrarNoShow()`, persiste ambos.

## ExpirarReservasPendentesUseCase

Sem comando (nenhum parâmetro do chamador — usa `Clock.agora()` internamente). Busca todos os agendamentos `PENDENTE_PAGAMENTO`, chama `expirarSeNecessario` em cada um, persiste os que expiraram. Retorna a quantidade expirada. Acionado periodicamente por `ExpiracaoReservaScheduler` (`adapter/in/scheduler/`), um `@Scheduled` com `initialDelay`/`fixedDelay` configuráveis via `agendamento.expiracao.intervalo-ms` (padrão 60s) — o delay inicial evita que o scheduler dispare no instante em que o contexto Spring sobe (o que aconteceria por padrão com `fixedDelay` sozinho), o que seria ruído em todo teste que carrega o contexto completo.

## ProcessarWebhookPagamentoUseCase

**Comando:** `WebhookPagamentoCommand(agendamentoId, pagamentoConfirmado)`

Se `pagamentoConfirmado == true`, delega para `ConfirmarAgendamentoUseCase`. Se `false`, não faz nada — cobranças Pix não pagas expiram naturalmente via `ExpirarReservasPendentesUseCase`, não há um estado de "pagamento recusado" modelado.

O `agendamentoId` chega diretamente no comando porque, ao gerar a cobrança Pix, o próprio `agendamentoId` é passado como referência externa ao gateway (`externalReference` no Asaas e no Mercado Pago), então não é necessário manter um mapeamento separado referência-externa → agendamento. Cada gateway tem seu próprio controller traduzindo o payload real para `WebhookPagamentoCommand`:

- `AsaasWebhookController` (`POST /api/webhooks/asaas`) — o payload do Asaas já traz o status (`payment.status`/`event`), confirma direto.
- `MercadoPagoWebhookController` (`POST /api/webhooks/mercadopago`) — o payload do Mercado Pago só traz o id do pagamento (`data.id`), não o status; o controller consulta de volta via `MercadoPagoGatewayAdapter.consultarPagamento(id)` antes de decidir `pago`.

Os dois controllers só ficam ativos quando o gateway correspondente está configurado (`@ConditionalOnProperty(name = "pagamento.gateway", ...)`, ver tabela de portas abaixo) — não faz sentido expor o webhook de um gateway que não está em uso. O `WebhookPagamentoController` genérico (`/api/webhooks/pagamento`, corpo `{agendamentoId, pago}`) continua existindo à parte, sempre ativo, útil para testes manuais.

## ConsultarAgendamentoUseCase

**Query:** `ConsultarAgendamentoQuery(agendamentoId)`

Busca o agendamento por ID — `RecursoNaoEncontradoException` se não existir. Único caso de uso de leitura pura; existe para servir o `GET /api/agendamentos/{id}`.

## Notificações (reação a eventos, não um caso de uso próprio)

Não existe um `EnviarNotificacaoUseCase` — notificar o cliente é reação automática a um evento de domínio já publicado, não algo que um ator externo pede. `NotificacaoEventListener` (`adapter/in/evento/`) tem um `@EventListener` para cada um dos três eventos (`AgendamentoCriado`, `AgendamentoConfirmado`, `AgendamentoCancelado`): busca o `Cliente` pelo `clienteId` do evento, monta uma `Notificacao` (canal, destinatário, assunto, mensagem) e chama `NotificacaoDispatcher.enviar(...)`, que roteia para o `EnviadorDeNotificacao` que atende o canal pedido (hoje só `EMAIL` é usado; `SMS`/`WHATSAPP` existem no enum `CanalNotificacao` para adapters futuros). Falhas de envio são capturadas e logadas dentro do listener, nunca propagadas — um e-mail que falha não pode derrubar o caso de uso que já persistiu o agendamento.

Para o listener reagir a um evento publicado via `PublicadorDeEventos`, a implementação `!dev` (`PublicadorDeEventosSpring`) delega para o `ApplicationEventPublisher` do Spring — os eventos de domínio continuam sendo `record`s puros, só esse adapter conhece Spring. Por isso `AgendamentoConfirmado` e `AgendamentoCancelado` ganharam o campo `clienteId` (antes só tinham `agendamentoId`): o listener precisa dele para buscar o contato do cliente, e eventos de domínio devem carregar os dados que os consumidores precisam, não forçar uma releitura do agregado atual.

## Sincronização com calendário externo (reação a eventos, não um caso de uso próprio)

Mesmo padrão das notificações: `CalendarioEventListener` (`adapter/in/evento/`) reage a `AgendamentoConfirmado` (busca o `Agendamento`, `Servico` e `Cliente` para montar o evento externo e chama `CalendarioExternoPort.sincronizarEvento(agendamentoId, titulo, descricao, periodo)`) e a `AgendamentoCancelado` (`CalendarioExternoPort.removerEvento(agendamentoId)`). Propositalmente **não** reage a `AgendamentoCriado`: um agendamento `PENDENTE_PAGAMENTO` pode expirar sem nunca ter ocupado a agenda de verdade, então só vale a pena criar o evento externo quando o pagamento já foi confirmado. Falhas são logadas e nunca propagadas, mesmo motivo do `NotificacaoEventListener`.

O id do evento no Google Calendar é derivado deterministicamente do `agendamentoId` (hex do UUID sem hífens — alfabeto compatível com o exigido pela API), então não é necessário manter um mapeamento à parte entre agendamento e evento externo.

## Portas de saída usadas pelos casos de uso

| Porta | Papel |
|---|---|
| `AgendamentoRepository`, `ClienteRepository`, `PrestadorRepository`, `ServicoRepository` | Persistência por agregado. Implementadas tanto em memória (`@Profile("dev")`) quanto via JPA/PostgreSQL (`@Profile("!dev")`). |
| `GatewayDePagamento` | `gerarCobrancaPix(agendamentoId, cliente, valor) -> CobrancaPix`. Três implementações concorrentes, mutuamente exclusivas: `GatewayDePagamentoFake` (`@Profile("dev")`); `AsaasGatewayAdapter` (`@Profile("!dev")` + `@ConditionalOnProperty(pagamento.gateway=asaas, matchIfMissing=true)` — é o padrão); `MercadoPagoGatewayAdapter` (`@Profile("!dev")` + `@ConditionalOnProperty(pagamento.gateway=mercadopago)`). Trocar de gateway real é só setar `PAGAMENTO_GATEWAY=mercadopago` — nenhum código muda. Config em `asaas.*`/`mercadopago.*` (`application.yml`). |
| `PublicadorDeEventos` | `publicar(EventoDeDominio)`. `PublicadorDeEventosEmMemoria` (`@Profile("dev")`) só acumula os eventos, útil em teste. `PublicadorDeEventosSpring` (`@Profile("!dev")`) delega ao `ApplicationEventPublisher` do Spring, permitindo que `NotificacaoEventListener` reaja de verdade (ver acima). |
| `EnviadorDeNotificacao` | `enviar(Notificacao)` + `canalSuportado()`. `EnviadorDeNotificacaoFake` (`@Profile("dev")`, acumula em memória) ou `EmailEnviadorDeNotificacao` (`@Profile("!dev")`, envia via SMTP com `JavaMailSender`, config em `spring.mail.*`/`notificacao.email.remetente`). `NotificacaoDispatcher` (application/service, não é adapter) escolhe o enviador certo por `CanalNotificacao`. |
| `CalendarioExternoPort` | `sincronizarEvento(agendamentoId, titulo, descricao, periodo)` + `removerEvento(agendamentoId)`. `CalendarioExternoFake` (`@Profile("dev")`, acumula em memória) ou `GoogleCalendarAdapter` (`@Profile("!dev")`, integra com a API real do Google Calendar via conta de serviço — integração de saída pura, sem endpoint REST próprio). Config em `google-calendar.*`. |
| `Clock` | `agora() -> Instant`. Abstrai `Instant.now()` para permitir testes determinísticos de regras sensíveis a tempo (expiração, janela de cancelamento). |
