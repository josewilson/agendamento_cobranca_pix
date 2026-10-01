package org.example.agendamento.application.service;

import org.example.agendamento.application.exception.RecursoNaoEncontradoException;
import org.example.agendamento.application.port.in.CadastrarServicoCommand;
import org.example.agendamento.application.port.in.CadastrarServicoUseCase;
import org.example.agendamento.application.port.out.PrestadorRepository;
import org.example.agendamento.application.port.out.ServicoRepository;
import org.example.agendamento.domain.model.servico.Servico;
import org.example.agendamento.domain.model.servico.ServicoId;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CadastrarServicoService implements CadastrarServicoUseCase {

    private final ServicoRepository servicoRepository;
    private final PrestadorRepository prestadorRepository;

    public CadastrarServicoService(ServicoRepository servicoRepository, PrestadorRepository prestadorRepository) {
        this.servicoRepository = servicoRepository;
        this.prestadorRepository = prestadorRepository;
    }

    @Override
    public Servico executar(CadastrarServicoCommand command) {
        prestadorRepository.buscarPorId(command.prestadorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prestador nao encontrado: " + command.prestadorId()));

        Servico servico = new Servico(ServicoId.novo(), command.prestadorId(), command.nome(),
                Duration.ofMinutes(command.duracaoMinutos()), Dinheiro.de(command.preco()), command.percentualSinal());
        return servicoRepository.salvar(servico);
    }
}
