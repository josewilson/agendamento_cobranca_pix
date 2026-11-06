package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.profissional.BloqueioDeAgenda;
import br.com.josewilson.agendamento.domain.model.profissional.BloqueioId;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;

import java.util.List;

/** Horarios bloqueados na agenda de um profissional; so o estabelecimento dono mexe. */
public interface GerenciarBloqueiosUseCase {

    BloqueioDeAgenda bloquear(PrestadorId solicitante, ProfissionalId profissionalId, Periodo periodo, String motivo);

    List<BloqueioDeAgenda> listar(PrestadorId solicitante, ProfissionalId profissionalId);

    void remover(PrestadorId solicitante, ProfissionalId profissionalId, BloqueioId bloqueioId);
}
