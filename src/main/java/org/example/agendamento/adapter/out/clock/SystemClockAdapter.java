package org.example.agendamento.adapter.out.clock;

import org.example.agendamento.application.port.out.Clock;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SystemClockAdapter implements Clock {

    @Override
    public Instant agora() {
        return Instant.now();
    }
}
