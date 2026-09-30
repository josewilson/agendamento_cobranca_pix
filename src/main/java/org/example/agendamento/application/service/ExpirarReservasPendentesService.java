package org.example.agendamento.application.service;

import org.example.agendamento.application.port.in.ExpirarReservasPendentesUseCase;
import org.example.agendamento.application.port.out.AgendamentoRepository;
import org.example.agendamento.application.port.out.Clock;
import org.example.agendamento.domain.model.agendamento.Agendamento;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class ExpirarReservasPendentesService implements ExpirarReservasPendentesUseCase {

    private static final Duration PRAZO_EXPIRACAO = Duration.ofMinutes(15);

    private final AgendamentoRepository agendamentoRepository;
    private final Clock clock;

    public ExpirarReservasPendentesService(AgendamentoRepository agendamentoRepository, Clock clock) {
        this.agendamentoRepository = agendamentoRepository;
        this.clock = clock;
    }

    @Override
    public int executar() {
        List<Agendamento> pendentes = agendamentoRepository.buscarTodosPendentesPagamento();
        Instant agora = clock.agora();

        int expirados = 0;
        for (Agendamento agendamento : pendentes) {
            if (agendamento.expirarSeNecessario(agora, PRAZO_EXPIRACAO)) {
                agendamentoRepository.salvar(agendamento);
                expirados++;
            }
        }
        return expirados;
    }
}
