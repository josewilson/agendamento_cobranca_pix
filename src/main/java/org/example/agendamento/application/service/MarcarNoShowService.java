package org.example.agendamento.application.service;

import org.example.agendamento.application.exception.RecursoNaoEncontradoException;
import org.example.agendamento.application.port.in.MarcarNoShowCommand;
import org.example.agendamento.application.port.in.MarcarNoShowUseCase;
import org.example.agendamento.application.port.out.AgendamentoRepository;
import org.example.agendamento.application.port.out.Clock;
import org.example.agendamento.application.port.out.ClienteRepository;
import org.example.agendamento.domain.model.agendamento.Agendamento;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.springframework.stereotype.Service;

@Service
public class MarcarNoShowService implements MarcarNoShowUseCase {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final Clock clock;

    public MarcarNoShowService(AgendamentoRepository agendamentoRepository, ClienteRepository clienteRepository,
                                Clock clock) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.clock = clock;
    }

    @Override
    public void executar(MarcarNoShowCommand command) {
        Agendamento agendamento = agendamentoRepository.buscarPorId(command.agendamentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento nao encontrado: " + command.agendamentoId()));
        Cliente cliente = clienteRepository.buscarPorId(agendamento.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado: " + agendamento.clienteId()));

        agendamento.marcarNoShow(clock.agora());
        cliente.registrarNoShow();

        agendamentoRepository.salvar(agendamento);
        clienteRepository.salvar(cliente);
    }
}
