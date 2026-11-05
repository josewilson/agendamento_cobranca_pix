package br.com.josewilson.agendamento.application.port.out;

import java.time.Instant;

public interface Clock {

    Instant agora();
}
