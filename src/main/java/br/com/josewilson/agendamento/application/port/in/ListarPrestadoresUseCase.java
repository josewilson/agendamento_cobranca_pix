package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.prestador.Prestador;

import br.com.josewilson.agendamento.application.port.out.Pagina;
import java.util.List;

public interface ListarPrestadoresUseCase {
    List<Prestador> executar();
    Pagina<Prestador> executar(int page, int size);
}
