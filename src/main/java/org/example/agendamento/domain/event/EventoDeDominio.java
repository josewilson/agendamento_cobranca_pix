package org.example.agendamento.domain.event;

import java.time.Instant;

public sealed interface EventoDeDominio permits AgendamentoCriado, AgendamentoConfirmado, AgendamentoCancelado {
    Instant ocorridoEm();
}
