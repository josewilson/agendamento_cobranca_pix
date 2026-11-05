package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.application.exception.RecursoNaoEncontradoException;
import br.com.josewilson.agendamento.application.port.in.MarcarNoShowCommand;
import br.com.josewilson.agendamento.application.port.in.MarcarNoShowUseCase;
import br.com.josewilson.agendamento.application.port.out.AgendamentoRepository;
import br.com.josewilson.agendamento.application.port.out.Clock;
import br.com.josewilson.agendamento.application.port.out.ClienteRepository;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
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
