package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.application.exception.AcessoNaoAutorizadoException;
import br.com.josewilson.agendamento.application.exception.RecursoNaoEncontradoException;
import br.com.josewilson.agendamento.application.port.in.AtualizarServicoCommand;
import br.com.josewilson.agendamento.application.port.in.AtualizarServicoUseCase;
import br.com.josewilson.agendamento.application.port.out.ServicoRepository;
import br.com.josewilson.agendamento.domain.model.servico.Servico;
import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AtualizarServicoService implements AtualizarServicoUseCase {

    private final ServicoRepository servicoRepository;

    public AtualizarServicoService(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @Override
    public Servico executar(AtualizarServicoCommand command) {
        Servico servico = servicoRepository.buscarPorId(command.servicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Servico nao encontrado: " + command.servicoId()));
        if (!servico.prestadorId().equals(command.prestadorAutenticado())) {
            throw new AcessoNaoAutorizadoException("Servico nao pertence ao prestador autenticado");
        }
        servico.atualizarDados(command.nome(), Duration.ofMinutes(command.duracaoMinutos()),
                Dinheiro.de(command.preco()), command.percentualSinal());
        return servicoRepository.salvar(servico);
    }
}
