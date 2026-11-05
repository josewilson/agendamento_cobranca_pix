package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.application.port.in.ConfirmarAgendamentoCommand;
import br.com.josewilson.agendamento.application.port.in.ConfirmarAgendamentoUseCase;
import br.com.josewilson.agendamento.application.port.in.ProcessarWebhookPagamentoUseCase;
import br.com.josewilson.agendamento.application.port.in.WebhookPagamentoCommand;
import org.springframework.stereotype.Service;

@Service
public class ProcessarWebhookPagamentoService implements ProcessarWebhookPagamentoUseCase {

    private final ConfirmarAgendamentoUseCase confirmarAgendamentoUseCase;

    public ProcessarWebhookPagamentoService(ConfirmarAgendamentoUseCase confirmarAgendamentoUseCase) {
        this.confirmarAgendamentoUseCase = confirmarAgendamentoUseCase;
    }

    @Override
    public void executar(WebhookPagamentoCommand command) {
        if (command.pagamentoConfirmado()) {
            confirmarAgendamentoUseCase.executar(new ConfirmarAgendamentoCommand(command.agendamentoId()));
        }
    }
}
