# Domínio

Todo o conteúdo deste documento está implementado em `src/main/java/org/example/agendamento/domain/` — Java puro, sem dependência de framework (garantido por `ArchitectureTest`).

## Entidades (identidade por ID, mutáveis)

| Entidade | Responsabilidade |
|---|---|
| `Agendamento` | Aggregate root. Reserva de um `Periodo` para um `Cliente`, com um `Prestador`, para um `Servico`. Dona da máquina de estados e das regras de cancelamento/sinal/no-show/expiração. |
| `Cliente` | Dados de contato + documento + contador de no-shows. Decide se exige sinal obrigatório (`exigeSinalObrigatorio()`). `atualizarDadosCadastrais(nome, contato)` edita nome/contato mantendo documento e histórico de no-show intactos (documento é imutável — ver `03-api-rest.md`, `PUT /api/clientes/{id}`). Não tem login — ver `02-casos-de-uso.md`, seção "Autenticação do Prestador". |
| `Prestador` | Dados de contato + documento + política de cancelamento padrão aplicada aos seus agendamentos + credenciais de login (`email` único, `senhaHash` — nunca a senha em texto puro, ver `02-casos-de-uso.md`). `atualizarPerfil(nome, telefone)` edita só esses dois campos; documento/email/senha são imutáveis por essa via (trocar identidade de login ou documento fiscal exigiria um fluxo próprio, fora de escopo). |
| `Servico` | Nome, duração, preço e percentual de sinal exigido pelo próprio serviço. `atualizarDados(...)` edita todos os quatro campos — agendamentos já criados guardam sua própria cópia de `valorServico`/política no momento da criação, então editar um serviço nunca muda retroativamente um agendamento passado. |

## Value Objects (imutáveis, `record`, validação no construtor compacto)

| VO | Regras |
|---|---|
| `Periodo` | `fim` deve ser estritamente posterior a `inicio`. `sobrepoe(outro)` usa comparação estrita — dois períodos que apenas se tocam (fim de um == início do outro) **não** conflitam. |
| `Dinheiro` | Envolve `BigDecimal`, nunca `double`/`float`. Escala sempre normalizada para 2 casas com `RoundingMode.HALF_EVEN` no construtor compacto. Não aceita valores negativos. |
| `Contato` | E-mail e telefone validados por regex (`domain/model/shared/FormatoContato`, compartilhado com a validação de telefone/email de `Prestador` — os dois precisam da mesma regra de formato, mas `Prestador` não reaproveita este VO, já que telefone de prestador é um campo isolado, sem o par email+telefone que `Contato` representa); telefone é normalizado removendo espaços/parênteses/hífen. |
| `DocumentoFiscal` | CPF ou CNPJ com validação real de dígito verificador (algoritmo completo, não apenas tamanho). Rejeita sequências de dígitos repetidos (ex. `111.111.111-11`). |
| `PoliticaCancelamento` | `antecedenciaMinima` (Duration) + `percentualRetido` (0–100). `dentroDaJanelaLivre(inicioAgendamento, agora)` — no limite exato da antecedência mínima, o cancelamento é considerado livre (sem retenção). |

## Máquina de estados (`StatusAgendamento`)

Transições válidas, centralizadas em `StatusAgendamento.transicoesValidas()`:

```
PENDENTE_PAGAMENTO → CONFIRMADO | CANCELADO | EXPIRADO
CONFIRMADO          → EM_ANDAMENTO | CANCELADO | NO_SHOW
EM_ANDAMENTO         → CONCLUIDO
CONCLUIDO, CANCELADO, EXPIRADO, NO_SHOW  → (terminais, nenhuma transição)
```

Qualquer transição fora dessa tabela lança `TransicaoDeStatusInvalidaException`.

## Eventos de domínio (`domain/event/`)

`EventoDeDominio` é uma `sealed interface` com três implementações-`record`: `AgendamentoCriado`, `AgendamentoConfirmado`, `AgendamentoCancelado`. Publicados pela camada de aplicação via a porta `PublicadorDeEventos` (ver `02-casos-de-uso.md`).

## Regras de negócio críticas

1. **Detecção de conflito de horário** — `VerificadorDeConflito.verificarDisponibilidade(periodo, agendamentosAtivos)` lança `ConflitoDeHorarioException` se o período desejado se sobrepõe a qualquer agendamento ativo (`PENDENTE_PAGAMENTO` ou `CONFIRMADO`) do mesmo prestador.
2. **Máquina de estados** — toda transição passa por `Agendamento.transicionarPara`, que consulta `StatusAgendamento.podeTransicionarPara`. Impossível colocar o agregado num estado inválido pela API pública.
3. **Cancelamento com retenção de sinal** — `Agendamento.cancelar(agora)` consulta `politicaAplicada.dentroDaJanelaLivre(...)`: dentro da janela, retenção zero; fora da janela, retém `valorSinal.percentual(politicaAplicada.percentualRetido())`. Devolve um `ResultadoCancelamento(valorRetido, valorReembolsado)`.
4. **Exigência de sinal** — decidida na criação (camada de aplicação, não no agregado): sinal é exigido se `servico.exigeSinal()` (percentual > 0 configurado no serviço) **ou** `cliente.exigeSinalObrigatorio()` (2+ no-shows registrados). Quando é o histórico do cliente — não o serviço — que exige a cobrança, aplica-se um percentual mínimo de sinal de **50%** sobre o preço do serviço (serviços com 0% de sinal configurado não ficariam com cobrança nenhuma sem essa regra; corrigido durante a Fase 2 — ver commit "casos de uso de agendamento com adapters em memoria").
5. **Expiração de reserva pendente** — `Agendamento.expirarSeNecessario(agora, prazoExpiracao)` transiciona `PENDENTE_PAGAMENTO → EXPIRADO` se `agora - criadoEm >= prazoExpiracao`. O prazo usado em produção é **15 minutos** (constante em `ExpirarReservasPendentesService`).
6. **Bloqueio por no-show** — `Cliente.registrarNoShow()` incrementa o contador; a partir de **2 no-shows**, `exigeSinalObrigatorio()` passa a `true` (ver regra 4).
7. **Não agendar no passado** — validado exclusivamente em `Agendamento.criar(...)` (`periodo.estaNoPassado(agora)`). O factory de reconstituição `Agendamento.reconstituir(...)`, usado pelos adapters de persistência para reidratar um agendamento já existente, **não** reaplica essa validação — um agendamento concluído há anos precisa continuar carregando do banco mesmo com período no passado.
