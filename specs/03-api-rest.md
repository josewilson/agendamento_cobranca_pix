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

**204 No Content**. **400** se `agendamentoId` ausente. Este é o **único** caminho que confirma um agendamento — propositalmente não existe `POST /api/agendamentos/{id}/confirmar` manual.

## Tratamento de erros (`GlobalExceptionHandler`)

Todo erro retorna `ErrorResponse { "mensagem": "..." }`.

| Exceção | Status |
|---|---|
| `RecursoNaoEncontradoException` | 404 |
| `ConflitoDeHorarioException` | 409 |
| `TransicaoDeStatusInvalidaException` | 409 |
| `IllegalArgumentException` / `IllegalStateException` (validações de domínio) | 400 |
| `MethodArgumentNotValidException` (Bean Validation) | 400, com `campo: mensagem` por erro de campo |
