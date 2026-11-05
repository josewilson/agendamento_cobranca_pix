package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.application.exception.RecursoNaoEncontradoException;
import br.com.josewilson.agendamento.application.port.in.CadastrarServicoCommand;
import br.com.josewilson.agendamento.application.port.in.CadastrarServicoUseCase;
import br.com.josewilson.agendamento.application.port.out.PrestadorRepository;
import br.com.josewilson.agendamento.application.port.out.ServicoRepository;
import br.com.josewilson.agendamento.domain.model.servico.Servico;
import br.com.josewilson.agendamento.domain.model.servico.ServicoId;
import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;
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
