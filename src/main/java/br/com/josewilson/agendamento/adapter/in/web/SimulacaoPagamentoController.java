package br.com.josewilson.agendamento.adapter.in.web;

import br.com.josewilson.agendamento.adapter.in.web.security.AutorizacaoAgendamento;
import br.com.josewilson.agendamento.application.port.in.ConsultarAgendamentoQuery;
import br.com.josewilson.agendamento.application.port.in.ConsultarAgendamentoUseCase;
import br.com.josewilson.agendamento.application.port.in.ProcessarWebhookPagamentoUseCase;
import br.com.josewilson.agendamento.application.port.in.WebhookPagamentoCommand;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * "Simular pagamento" para demonstracao/testes sem gateway de verdade. Substitui o antigo webhook
 * generico publico (POST /api/webhooks/pagamento), que deixava qualquer um confirmar qualquer
 * agendamento so com o id: aqui o endpoint
 * <ul>
 *   <li>so existe com {@code app.simulacao-pagamento.habilitada=true} (padrao: desligado, nao ha rota);</li>
 *   <li>fica fora de /api/webhooks/** — exige sessao e CSRF como qualquer outra chamada;</li>
 *   <li>so vale para o prestador dono do agendamento.</li>
 * </ul>
 * A confirmacao real vem dos webhooks assinados do Asaas/Mercado Pago.
 */
@RestController
@RequestMapping("/api/agendamentos/{id}/simular-pagamento")
@ConditionalOnProperty(name = "app.simulacao-pagamento.habilitada", havingValue = "true")
public class SimulacaoPagamentoController {

    private final ConsultarAgendamentoUseCase consultarAgendamentoUseCase;
    private final ProcessarWebhookPagamentoUseCase processarWebhookPagamentoUseCase;

    public SimulacaoPagamentoController(ConsultarAgendamentoUseCase consultarAgendamentoUseCase,
                                        ProcessarWebhookPagamentoUseCase processarWebhookPagamentoUseCase) {
        this.consultarAgendamentoUseCase = consultarAgendamentoUseCase;
        this.processarWebhookPagamentoUseCase = processarWebhookPagamentoUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void simular(@PathVariable UUID id, Authentication authentication) {
        AgendamentoId agendamentoId = new AgendamentoId(id);
        Agendamento agendamento = consultarAgendamentoUseCase.executar(new ConsultarAgendamentoQuery(agendamentoId));
        AutorizacaoAgendamento.exigirPrestadorDono(agendamento, authentication);
        processarWebhookPagamentoUseCase.executar(new WebhookPagamentoCommand(agendamentoId, true));
    }
}
