package org.example.agendamento.application.service;

import org.example.agendamento.application.exception.AcessoNaoAutorizadoException;
import org.example.agendamento.application.exception.RecursoNaoEncontradoException;
import org.example.agendamento.application.port.in.ExcluirServicoCommand;
import org.example.agendamento.application.port.in.ExcluirServicoUseCase;
import org.example.agendamento.application.port.out.ServicoRepository;
import org.example.agendamento.domain.model.servico.Servico;
import org.springframework.stereotype.Service;

@Service
public class ExcluirServicoService implements ExcluirServicoUseCase {

    private final ServicoRepository servicoRepository;

    public ExcluirServicoService(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @Override
    public void executar(ExcluirServicoCommand command) {
        Servico servico = servicoRepository.buscarPorId(command.servicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Servico nao encontrado: " + command.servicoId()));
        if (!servico.prestadorId().equals(command.prestadorAutenticado())) {
            throw new AcessoNaoAutorizadoException("Servico nao pertence ao prestador autenticado");
        }
        servicoRepository.excluir(command.servicoId());
    }
}
