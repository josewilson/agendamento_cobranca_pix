package br.com.josewilson.agendamento.adapter.out.persistence.jpa.mapper;

import br.com.josewilson.agendamento.adapter.out.persistence.jpa.entity.BloqueioJpaEntity;
import br.com.josewilson.agendamento.domain.model.profissional.BloqueioDeAgenda;
import br.com.josewilson.agendamento.domain.model.profissional.BloqueioId;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;

public final class BloqueioMapper {

    private BloqueioMapper() {
    }

    public static BloqueioJpaEntity paraJpa(BloqueioDeAgenda bloqueio) {
        BloqueioJpaEntity entity = new BloqueioJpaEntity();
        entity.setId(bloqueio.id().valor());
        entity.setProfissionalId(bloqueio.profissionalId().valor());
        entity.setPeriodoInicio(bloqueio.periodo().inicio());
        entity.setPeriodoFim(bloqueio.periodo().fim());
        entity.setMotivo(bloqueio.motivo());
        return entity;
    }

    public static BloqueioDeAgenda paraDominio(BloqueioJpaEntity entity) {
        return new BloqueioDeAgenda(new BloqueioId(entity.getId()), new ProfissionalId(entity.getProfissionalId()),
                new Periodo(entity.getPeriodoInicio(), entity.getPeriodoFim()), entity.getMotivo());
    }
}
