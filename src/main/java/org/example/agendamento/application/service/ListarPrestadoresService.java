package org.example.agendamento.application.service;

import org.example.agendamento.application.port.in.ListarPrestadoresUseCase;
import org.example.agendamento.application.port.out.PrestadorRepository;
import org.example.agendamento.domain.model.prestador.Prestador;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarPrestadoresService implements ListarPrestadoresUseCase {

    private final PrestadorRepository prestadorRepository;

    public ListarPrestadoresService(PrestadorRepository prestadorRepository) {
        this.prestadorRepository = prestadorRepository;
    }

    @Override
    public List<Prestador> executar() {
        return prestadorRepository.buscarTodos();
    }
}
