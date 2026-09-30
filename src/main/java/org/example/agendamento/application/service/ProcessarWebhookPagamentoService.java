package org.example.agendamento.application.service;

import org.example.agendamento.application.port.in.ConfirmarAgendamentoCommand;
import org.example.agendamento.application.port.in.ConfirmarAgendamentoUseCase;
import org.example.agendamento.application.port.in.ProcessarWebhookPagamentoUseCase;
import org.example.agendamento.application.port.in.WebhookPagamentoCommand;
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
