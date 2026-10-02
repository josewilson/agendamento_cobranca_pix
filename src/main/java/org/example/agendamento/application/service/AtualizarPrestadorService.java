package org.example.agendamento.application.service;

import org.example.agendamento.application.exception.AcessoNaoAutorizadoException;
import org.example.agendamento.application.exception.RecursoNaoEncontradoException;
import org.example.agendamento.application.port.in.AtualizarPrestadorCommand;
import org.example.agendamento.application.port.in.AtualizarPrestadorUseCase;
import org.example.agendamento.application.port.out.PrestadorRepository;
import org.example.agendamento.domain.model.prestador.Prestador;
import org.springframework.stereotype.Service;

@Service
public class AtualizarPrestadorService implements AtualizarPrestadorUseCase {

    private final PrestadorRepository prestadorRepository;

    public AtualizarPrestadorService(PrestadorRepository prestadorRepository) {
        this.prestadorRepository = prestadorRepository;
    }

    @Override
    public Prestador executar(AtualizarPrestadorCommand command) {
        if (!command.prestadorId().equals(command.prestadorAutenticado())) {
            throw new AcessoNaoAutorizadoException("Nao e possivel editar o cadastro de outro prestador");
        }
        Prestador prestador = prestadorRepository.buscarPorId(command.prestadorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prestador nao encontrado: " + command.prestadorId()));
        prestador.atualizarPerfil(command.nome(), command.telefone());
        return prestadorRepository.salvar(prestador);
    }
}
