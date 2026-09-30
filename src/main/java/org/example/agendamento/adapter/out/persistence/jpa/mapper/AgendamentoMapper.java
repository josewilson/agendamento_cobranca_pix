package org.example.agendamento.adapter.out.persistence.jpa.mapper;

import org.example.agendamento.adapter.out.persistence.jpa.entity.AgendamentoJpaEntity;
import org.example.agendamento.domain.model.agendamento.Agendamento;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.agendamento.StatusAgendamento;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.ServicoId;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.example.agendamento.domain.model.shared.Periodo;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;

import java.time.Duration;

public final class AgendamentoMapper {

    private AgendamentoMapper() {
    }

    public static AgendamentoJpaEntity paraJpa(Agendamento agendamento) {
        AgendamentoJpaEntity entity = new AgendamentoJpaEntity();
        entity.setId(agendamento.id().valor());
        entity.setPrestadorId(agendamento.prestadorId().valor());
        entity.setClienteId(agendamento.clienteId().valor());
        entity.setServicoId(agendamento.servicoId().valor());
        entity.setPeriodoInicio(agendamento.periodo().inicio());
        entity.setPeriodoFim(agendamento.periodo().fim());
        entity.setValorServico(agendamento.valorServico().valor());
        entity.setValorSinal(agendamento.valorSinal().valor());
        entity.setPoliticaAntecedenciaMinimaSegundos(agendamento.politicaAplicada().antecedenciaMinima().getSeconds());
        entity.setPoliticaPercentualRetido(agendamento.politicaAplicada().percentualRetido());
        entity.setStatus(agendamento.status().name());
        entity.setCriadoEm(agendamento.criadoEm());
        entity.setReferenciaPagamento(agendamento.referenciaPagamento().orElse(null));
        return entity;
    }

    public static Agendamento paraDominio(AgendamentoJpaEntity entity) {
        Periodo periodo = new Periodo(entity.getPeriodoInicio(), entity.getPeriodoFim());
        PoliticaCancelamento politica = new PoliticaCancelamento(
                Duration.ofSeconds(entity.getPoliticaAntecedenciaMinimaSegundos()),
                entity.getPoliticaPercentualRetido());
        return Agendamento.reconstituir(
                new AgendamentoId(entity.getId()),
                new PrestadorId(entity.getPrestadorId()),
                new ClienteId(entity.getClienteId()),
                new ServicoId(entity.getServicoId()),
                periodo,
                Dinheiro.de(entity.getValorServico()),
                Dinheiro.de(entity.getValorSinal()),
                politica,
                entity.getCriadoEm(),
                StatusAgendamento.valueOf(entity.getStatus()),
                entity.getReferenciaPagamento());
    }
}
