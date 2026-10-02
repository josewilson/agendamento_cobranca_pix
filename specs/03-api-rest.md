# API REST

Implementada em `src/main/java/org/example/agendamento/adapter/in/web/`. Base path: `/api`.

Documentação interativa via `springdoc-openapi-starter-webmvc-ui` em `/swagger-ui.html` (`/v3/api-docs` para o JSON cru) — permite testar todos os endpoints direto do navegador. Com o perfil `dev` ativo, `DevDataSeeder` (`adapter/in/seed/`) popula um prestador, cliente e serviço de IDs fixos no startup (logados no console), um atalho rápido além dos endpoints de cadastro abaixo.

**CORS**: liberado para `app.cors.allowed-origins` (`SecurityConfig`, `adapter/in/web/security/` — único lugar que configura CORS desde que `WebConfig` foi removido nesta sessão, ver `specs/04-roadmap.md`), por padrão só `http://localhost:5173` (o frontend separado em `frontend/`, ver `CLAUDE.md`), com `allowCredentials(true)` — necessário para o cookie de sessão do login (ver abaixo) ser enviado nas chamadas entre origens diferentes. Sem isso o browser bloqueia as chamadas antes de chegarem aos controllers.

**Autenticação**: login simples de `Prestador` via sessão (cookie `JSESSIONID`), ver seção `/api/auth/*` abaixo. `Cliente` continua sem login (decisão de escopo, risco aceito conscientemente — ver `specs/02-casos-de-uso.md`). A maior parte da API é pública (fluxo de reserva: listar prestador/serviço, cadastrar cliente, criar/consultar/cancelar/marcar no-show um agendamento, webhooks); `GET /api/agendamentos`, `POST`/`PUT`/`DELETE /api/servicos` e `PUT /api/prestadores/{id}` exigem estar autenticado como o `Prestador` dono dos dados — endpoints sem login nessas rotas retornam **401**.

**CSRF**: habilitado via `CookieCsrfTokenRepository` (cookie `XSRF-TOKEN`, não `HttpOnly` — precisa ser legível por JS), exceto para `/api/webhooks/**`. Qualquer chamada que não seja `GET` precisa do header `X-XSRF-TOKEN` com o valor desse cookie — o frontend já faz isso automaticamente (`frontend/src/api/client.js`). Sem o header (ou com um valor que não bate com o cookie), a chamada é rejeitada antes de chegar ao controller.

## `POST /api/prestadores`

Cadastra um prestador — é também o cadastro (sign-up) de login, já que só `Prestador` autentica. Executa `CadastrarPrestadorUseCase`. Política de cancelamento sempre `PoliticaCancelamento.padrao()` (não configurável via API ainda). Público (não exige login — é como se cria a conta).

**Request** (`CadastrarPrestadorRequest`, todos `@NotBlank`; `senha` com `@Size(min = 6)`):
```json
{ "nome": "Clinica Bem-Estar", "telefone": "11987654321", "email": "clinica@exemplo.com", "senha": "senha123", "documentoNumero": "11444777000161", "documentoTipo": "CNPJ" }
```
`documentoTipo` é `"CPF"` ou `"CNPJ"`. `email` precisa ser único (`uk_prestador_email`); `senha` nunca é retornada nem armazenada em texto puro (hash BCrypt, ver `CLAUDE.md`).

**201 Created** (`PrestadorResponse` — nunca inclui a senha/hash):
```json
{ "id": "uuid", "nome": "Clinica Bem-Estar", "telefone": "11987654321", "email": "clinica@exemplo.com", "documentoNumero": "11444777000161", "documentoTipo": "CNPJ" }
```
**400** se nome vazio, telefone/email em formato inválido, senha com menos de 6 caracteres, ou documento com dígito verificador inválido (validação do próprio `Prestador`/`DocumentoFiscal`). O frontend mostra `telefone` na listagem de prestadores em vez do documento (ver `CLAUDE.md`, "Regra inegociável") — o documento continua sendo coletado e armazenado, só não aparece na tela.

## `GET /api/prestadores`

Lista todos os prestadores cadastrados. Executa `ListarPrestadoresUseCase`. **200 OK**, array de `PrestadorResponse`. Público — a tela de novo agendamento (fluxo de reserva, sem login) precisa listar prestadores para o cliente escolher.

## `PUT /api/prestadores/{id}`

Edita o próprio perfil do prestador autenticado. Executa `AtualizarPrestadorUseCase`. **Exige login** — `{id}` precisa bater com o prestador da sessão.

**Request** (`AtualizarPrestadorRequest`): `{ "nome": "Clinica Bem-Estar Ltda", "telefone": "11999998888" }`

**200 OK** (`PrestadorResponse`). Só `nome`/`telefone` são editáveis por aqui — documento, email e senha permanecem imutáveis (trocar identidade de login ou documento fiscal exigiria um fluxo próprio, fora de escopo).

**Erros:** `401` (sem login), `403` (`{id}` não é o prestador da sessão), `404` (não existe), `400` (nome vazio ou telefone em formato inválido). Não existe `DELETE /api/prestadores/{id}` — excluir a própria conta logada envolveria invalidar a sessão e decidir o que fazer com serviços/agendamentos já vinculados, adiado conscientemente (ver `specs/04-roadmap.md`).

## `POST /api/auth/login`

Login do prestador. Autentica via `AuthenticationManager` (Spring Security) e grava a sessão num cookie `JSESSIONID` (`HttpOnly`, `Set-Cookie` na resposta) — nenhum token é devolvido no corpo para o frontend guardar.

**Request**: `{ "email": "clinica@exemplo.com", "senha": "senha123" }`

**200 OK**: `{ "prestadorId": "uuid", "nome": "Clinica Bem-Estar" }`. **401** se email/senha não conferem.

## `POST /api/auth/logout`

Invalida a sessão atual. **204 No Content**.

## `GET /api/auth/me`

Consulta quem está logado na sessão atual (usado pelo frontend para saber se deve mostrar a tela de login ou a área autenticada). **200 OK**: mesmo formato de `/api/auth/login`. **401** se não houver sessão válida.

## `POST /api/clientes`

Cadastra um cliente. Executa `CadastrarClienteUseCase`.

**Request** (`CadastrarClienteRequest`, todos `@NotBlank`):
```json
{ "nome": "Maria Silva", "email": "maria@exemplo.com", "telefone": "11987654321", "documentoNumero": "52998224725", "documentoTipo": "CPF" }
```

**201 Created** (`ClienteResponse`, inclui `quantidadeNoShow` sempre `0` para um cliente novo):
```json
{ "id": "uuid", "nome": "Maria Silva", "email": "maria@exemplo.com", "telefone": "11987654321",
  "documentoNumero": "52998224725", "documentoTipo": "CPF", "quantidadeNoShow": 0 }
```
**400** se email/telefone/documento inválidos (validação de `Contato`/`DocumentoFiscal`).

## `GET /api/clientes`

Lista todos os clientes cadastrados. Executa `ListarClientesUseCase`. **200 OK**, array de `ClienteResponse`. Existe especificamente para o frontend poder oferecer um `<select>` de clientes por nome na tela de novo agendamento — a regra do projeto é que nenhum id de cadastro aparece como texto na UI (ver `CLAUDE.md`), então pedir pro usuário colar um UUID não é uma opção.

## `PUT /api/clientes/{id}`

Edita nome/email/telefone de um cliente. Executa `AtualizarClienteUseCase`. Público (mesmo escopo de sempre do `Cliente`).

**Request** (`AtualizarClienteRequest`, todos `@NotBlank`): `{ "nome": "Maria S. Silva", "email": "maria.nova@exemplo.com", "telefone": "11999998888" }`

**200 OK** (`ClienteResponse`, mantém `documentoNumero`/`documentoTipo`/`quantidadeNoShow` intactos — documento é imutável). **Erros:** `404`, `400`, `409` (email/telefone resultando em conflito, raro já que só documento tem constraint única hoje).

## `DELETE /api/clientes/{id}`

Exclui um cliente. Executa `ExcluirClienteUseCase`. Público. **204 No Content**. **404** se não existir. **409** se houver agendamento vinculado (constraint de chave estrangeira — `DataIntegrityViolationException`, ver tabela de erros abaixo).

## `POST /api/servicos`

Cadastra um serviço vinculado ao prestador autenticado. Executa `CadastrarServicoUseCase`. **Exige login** — `prestadorId` não vem mais no corpo da requisição, o controller pega do `@AuthenticationPrincipal` (sessão), para um prestador não poder cadastrar serviço em nome de outro.

**Request** (`CadastrarServicoRequest`):
```json
{ "nome": "Massagem relaxante", "duracaoMinutos": 60, "preco": 150.00, "percentualSinal": 30 }
```

**201 Created** (`ServicoResponse`):
```json
{ "id": "uuid", "prestadorId": "uuid", "nome": "Massagem relaxante", "duracaoMinutos": 60, "preco": 150.00, "percentualSinal": 30.00 }
```
**401** se não autenticado. **400** se `duracaoMinutos` não for positivo ou demais validações de domínio falharem.

## `GET /api/servicos?prestadorId={uuid}`

Lista os serviços de um prestador. Executa `ListarServicosPorPrestadorUseCase`. **200 OK**, array de `ServicoResponse` (vazio se o prestador não tiver serviços ou não existir — não há 404 aqui, uma lista vazia é uma resposta válida). Público — o fluxo de reserva (sem login) precisa listar os serviços de um prestador pra montar a tela de novo agendamento.

## `PUT /api/servicos/{id}`

Edita um serviço do prestador autenticado. Executa `AtualizarServicoUseCase`. **Exige login** — o serviço precisa pertencer ao prestador da sessão.

**Request** (`CadastrarServicoRequest`, mesmo formato do cadastro): `{ "nome": "Massagem relaxante", "duracaoMinutos": 60, "preco": 150.00, "percentualSinal": 30 }`

**200 OK** (`ServicoResponse`). Agendamentos já criados com esse serviço não mudam — cada um guarda sua própria cópia de `valorServico`/política no momento da criação. **Erros:** `401` (sem login), `403` (serviço pertence a outro prestador), `404` (não existe), `400` (validação de domínio).

## `DELETE /api/servicos/{id}`

Exclui um serviço do prestador autenticado. Executa `ExcluirServicoUseCase`. **Exige login**, mesma checagem de dono do `PUT`. **204 No Content**. **Erros:** `401`, `403`, `404`.

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

## `GET /api/agendamentos`

Lista os agendamentos do **prestador autenticado** (qualquer status, inclusive histórico). Executa `ListarAgendamentosPorPrestadorUseCase`. **Exige login** — não recebe `prestadorId` como parâmetro (recebia antes do login existir); o controller deriva do `@AuthenticationPrincipal`, então um prestador só vê a própria agenda, nunca a de outro. **200 OK**, array de `AgendamentoResponse` ordenado por `inicio` ascendente (vazio se o prestador não tiver agendamentos — não há 404). **401** se não autenticado. É a tela "Minha agenda" do frontend: por causa da regra de nunca mostrar id na UI (ver `CLAUDE.md`), sem este endpoint não havia nenhum jeito de voltar a encontrar um agendamento depois de sair da tela em que ele foi criado.

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

## `GET /actuator/health`

Liveness/readiness para orquestrador ou monitoramento. **200 OK**: `{ "status": "UP" }` (ou `"DOWN"`). Único endpoint do Actuator exposto (`management.endpoints.web.exposure.include: health`) — os demais (`env`, `beans`, etc.) vazam detalhe de implementação sem necessidade num projeto sem autenticação de operador separada. `show-details: never` pelo mesmo motivo. Público (`permitAll` em `SecurityConfig`, senão um orquestrador não autenticado não conseguiria checar a saúde da aplicação). O indicador de saúde do `spring-mail` fica desabilitado (`management.health.mail.enabled: false`) — `spring-boot-starter-mail` registra um health check que tenta conectar no SMTP configurado, mas email é integração opcional aqui (a aplicação sobe e funciona normalmente sem SMTP configurado), então um SMTP fora do ar não deveria derrubar o `/health` da aplicação inteira.

---

`/api/webhooks/asaas`, `/api/webhooks/mercadopago` e o `/api/webhooks/pagamento` genérico acima são os únicos caminhos que confirmam um agendamento — propositalmente não existe `POST /api/agendamentos/{id}/confirmar` manual.

## Tratamento de erros (`GlobalExceptionHandler`)

Todo erro de caso de uso retorna `ErrorResponse { "mensagem": "..." }`.

| Exceção | Status |
|---|---|
| `RecursoNaoEncontradoException` | 404 |
| `AcessoNaoAutorizadoException` | 403 |
| `ConflitoDeHorarioException` | 409 |
| `TransicaoDeStatusInvalidaException` | 409 |
| `DataIntegrityViolationException` (constraint única/estrangeira do banco — documento/email duplicado, exclusão com vínculo) | 409, mensagem genérica (qual coluna/valor colidiu é detalhe de implementação do banco) |
| `IllegalArgumentException` / `IllegalStateException` (validações de domínio) | 400 |
| `MethodArgumentNotValidException` (Bean Validation) | 400, com `campo: mensagem` por erro de campo |

**401 (sem sessão válida)** vem do `AuthenticationEntryPoint` de `SecurityConfig` (`adapter/in/web/security/`), não do `GlobalExceptionHandler` — corpo no formato padrão do Spring Boot (`{timestamp, status, error, path}`), não `ErrorResponse`, já que é tratado na camada de segurança (antes do `DispatcherServlet`), não num `@ExceptionHandler` de controller. Pelo mesmo motivo, uma falha de CSRF (header `X-XSRF-TOKEN` ausente ou incorreto) também passa pelo `AuthenticationEntryPoint` quando o chamador é anônimo — Spring Security roteia qualquer `AccessDeniedException` (CSRF incluído) pro entry point de autenticação nesse caso, então aparece como **401**, não 403.

`/error` está liberado em `permitAll` de propósito: sem isso, uma exceção não tratada (e portanto um status 500 de verdade) apareceria como um 401 enganoso, porque o redespacho interno do Tomcat pro `/error` passa pelo filtro de segurança de novo e esbarraria em `anyRequest().authenticated()`.
