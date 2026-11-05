package br.com.josewilson.agendamento.adapter.out.clock;

import br.com.josewilson.agendamento.application.port.out.Clock;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SystemClockAdapter implements Clock {

    @Override
    public Instant agora() {
        return Instant.now();
    }
}
