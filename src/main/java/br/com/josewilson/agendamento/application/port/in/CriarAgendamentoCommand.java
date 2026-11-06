package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;
import br.com.josewilson.agendamento.domain.model.servico.ServicoId;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;

/** {@code profissionalId} opcional: omitido, so vale se o estabelecimento tem um unico profissional. */
public record CriarAgendamentoCommand(PrestadorId prestadorId, ClienteId clienteId, ServicoId servicoId,
                                       Periodo periodo, ProfissionalId profissionalId) {

    public CriarAgendamentoCommand(PrestadorId prestadorId, ClienteId clienteId, ServicoId servicoId, Periodo periodo) {
        this(prestadorId, clienteId, servicoId, periodo, null);
    }
}
