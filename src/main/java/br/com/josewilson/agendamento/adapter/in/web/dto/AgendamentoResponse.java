package br.com.josewilson.agendamento.adapter.in.web.dto;

import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AgendamentoResponse(
        UUID id,
        UUID prestadorId,
        UUID clienteId,
        UUID servicoId,
        Instant inicio,
        Instant fim,
        BigDecimal valorServico,
        BigDecimal valorSinal,
        String status,
        Instant criadoEm) {

    /**
     * Visao de "ocupacao" da agenda de OUTRO prestador: so horario e status. Id, cliente, servico e
     * valores ficam nulos - quem nao e dono nao precisa (nem deve) ver nem abrir o agendamento.
     */
    public static AgendamentoResponse ocupacao(Agendamento agendamento) {
        return new AgendamentoResponse(
                null,
                agendamento.prestadorId().valor(),
                null,
                null,
                agendamento.periodo().inicio(),
                agendamento.periodo().fim(),
                null,
                null,
                agendamento.status().name(),
                null);
    }

    public static AgendamentoResponse de(Agendamento agendamento) {
        return new AgendamentoResponse(
                agendamento.id().valor(),
                agendamento.prestadorId().valor(),
                agendamento.clienteId().valor(),
                agendamento.servicoId().valor(),
                agendamento.periodo().inicio(),
                agendamento.periodo().fim(),
                agendamento.valorServico().valor(),
                agendamento.valorSinal().valor(),
                agendamento.status().name(),
                agendamento.criadoEm());
    }
}
