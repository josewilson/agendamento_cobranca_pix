package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.application.port.in.ListarPrestadoresUseCase;
import br.com.josewilson.agendamento.application.port.out.PrestadorRepository;
import br.com.josewilson.agendamento.domain.model.prestador.Prestador;
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
