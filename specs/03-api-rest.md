# API REST

Implementada em `src/main/java/org/example/agendamento/adapter/in/web/`. Base path: `/api`.

## `POST /api/agendamentos`

Cria um agendamento. Executa `CriarAgendamentoUseCase`.

**Request** (`CriarAgendamentoRequest`, todos os campos `@NotNull`):
```json
{
  "prestadorId": "uuid",
  "clienteId": "uuid",
  "servicoId": "uuid",
  "inicio": "2026-10-02T12:00:00Z",
  "fim": "2026-10-02T12:30:00Z"
}
```

**201 Created** (`CriarAgendamentoResponse`):
```json
{
  "agendamento": {
    "id": "uuid", "prestadorId": "uuid", "clienteId": "uuid", "servicoId": "uuid",
    "inicio": "...", "fim": "...",
    "valorServico": 100.00, "valorSinal": 30.00,
    "status": "PENDENTE_PAGAMENTO", "criadoEm": "..."
  },
  "cobranca": { "referenciaExterna": "...", "qrCode": "...", "copiaECola": "...", "expiraEm": "..." }
}
```
`cobranca` é `null` quando o agendamento não exige sinal (status já nasce `CONFIRMADO`).

**Erros:** `400` (campo ausente/inválido, ou `fim` não posterior a `inicio` — validado pelo próprio construtor de `Periodo`, reaproveitado aqui sem duplicar regra), `404` (prestador/cliente/serviço não encontrado), `409` (conflito de horário).

## `GET /api/agendamentos/{id}`

Consulta um agendamento. Executa `ConsultarAgendamentoUseCase`.

**200 OK** (`AgendamentoResponse`, mesmo formato do objeto `agendamento` acima). **404** se não existir.

## `POST /api/agendamentos/{id}/cancelar`

Cancela um agendamento. Executa `CancelarAgendamentoUseCase`.

**200 OK** (`ResultadoCancelamentoResponse`):
```json
{ "valorRetido": 0.00, "valorReembolsado": 30.00 }
```
**Erros:** `404` (não encontrado), `409` (agendamento já em estado terminal, transição inválida).

## `POST /api/agendamentos/{id}/no-show`

Marca no-show. Executa `MarcarNoShowUseCase`. **204 No Content**. **Erros:** `404`, `400` (marcado antes do horário do agendamento), `409` (transição inválida).

## `POST /api/webhooks/pagamento`

Callback de confirmação de pagamento. Executa `ProcessarWebhookPagamentoUseCase`.

**Request** (`WebhookPagamentoRequest`):
```json
{ "agendamentoId": "uuid", "pago": true }
```

**204 No Content**. **400** se `agendamentoId` ausente. Endpoint genérico/manual — não é o que o Asaas realmente chama (ver abaixo).

## `POST /api/webhooks/asaas`

Webhook real do Asaas (https://docs.asaas.com/docs/webhook-para-cobrancas). Executa `ProcessarWebhookPagamentoUseCase`, traduzindo o payload nativo do Asaas.

**Header obrigatório:** `asaas-access-token` — deve bater com `asaas.webhook-token` (`ASAAS_WEBHOOK_TOKEN`); caso contrário, **403 Forbidden** sem processar nada.

**Request** (payload real do Asaas, `AsaasWebhookRequest`):
```json
{
  "event": "PAYMENT_RECEIVED",
  "payment": {
    "id": "pay_080225913252",
    "externalReference": "uuid-do-agendamento",
    "status": "RECEIVED"
  }
}
```

`pago = true` quando `event` é `PAYMENT_CONFIRMED` ou `PAYMENT_RECEIVED`; qualquer outro evento (`PAYMENT_OVERDUE`, `PAYMENT_DELETED`, etc.) chama o caso de uso com `pago = false`, que é um no-op. `externalReference` é o `agendamentoId` que nós mesmos enviamos ao criar a cobrança — não precisa de mapeamento à parte.

**204 No Content** em qualquer evento com token válido.

Só ativo quando `pagamento.gateway` está `asaas` (o padrão — `@ConditionalOnProperty(..., matchIfMissing = true)`).

## `POST /api/webhooks/mercadopago`

Webhook real do Mercado Pago (mercadopago.com.br/developers/.../notifications/webhooks). Executa `ProcessarWebhookPagamentoUseCase`. Só ativo quando `pagamento.gateway=mercadopago`.

**Headers obrigatórios:** `x-signature` (formato `ts=<epoch-ms>,v1=<hmac-sha256-hex>`) e `x-request-id`. A assinatura é validada via HMAC-SHA256 sobre o manifesto `id:<data.id em minúsculas>;request-id:<x-request-id>;ts:<ts>;`, usando `mercadopago.webhook-secret` (`MERCADOPAGO_WEBHOOK_SECRET`) como chave. Assinatura ausente ou inválida → **403 Forbidden** sem processar nada.

**Request** (payload real do Mercado Pago, `MercadoPagoWebhookRequest`):
```json
{
  "type": "payment",
  "action": "payment.updated",
  "data": { "id": "123456789" }
}
```

Diferente do Asaas, o corpo **não traz o status do pagamento** — só o id. O controller consulta de volta (`GET /v1/payments/{id}` via `MercadoPagoGatewayAdapter.consultarPagamento`) para saber o status e o `external_reference` (nosso `agendamentoId`). `pago = true` somente quando o status consultado é `approved`.

**204 No Content** com assinatura válida.

---

`/api/webhooks/asaas`, `/api/webhooks/mercadopago` e o `/api/webhooks/pagamento` genérico acima são os únicos caminhos que confirmam um agendamento — propositalmente não existe `POST /api/agendamentos/{id}/confirmar` manual.

## Tratamento de erros (`GlobalExceptionHandler`)

Todo erro retorna `ErrorResponse { "mensagem": "..." }`.

| Exceção | Status |
|---|---|
| `RecursoNaoEncontradoException` | 404 |
| `ConflitoDeHorarioException` | 409 |
| `TransicaoDeStatusInvalidaException` | 409 |
| `IllegalArgumentException` / `IllegalStateException` (validações de domínio) | 400 |
| `MethodArgumentNotValidException` (Bean Validation) | 400, com `campo: mensagem` por erro de campo |
