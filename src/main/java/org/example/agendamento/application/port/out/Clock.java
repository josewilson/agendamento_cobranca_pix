package org.example.agendamento.application.port.out;

import java.time.Instant;

public interface Clock {

    Instant agora();
}
