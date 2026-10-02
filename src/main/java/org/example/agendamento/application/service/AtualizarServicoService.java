package org.example.agendamento.application.service;

import org.example.agendamento.application.exception.AcessoNaoAutorizadoException;
import org.example.agendamento.application.exception.RecursoNaoEncontradoException;
import org.example.agendamento.application.port.in.AtualizarServicoCommand;
import org.example.agendamento.application.port.in.AtualizarServicoUseCase;
import org.example.agendamento.application.port.out.ServicoRepository;
import org.example.agendamento.domain.model.servico.Servico;
import org.example.agendamento.domain.model.shared.Dinheiro;
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
