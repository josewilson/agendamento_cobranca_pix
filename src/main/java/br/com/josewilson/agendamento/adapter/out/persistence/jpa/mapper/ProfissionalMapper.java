package br.com.josewilson.agendamento.adapter.out.persistence.jpa.mapper;

import br.com.josewilson.agendamento.adapter.out.persistence.jpa.entity.ProfissionalJpaEntity;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.profissional.Profissional;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;

public final class ProfissionalMapper {

    private ProfissionalMapper() {
    }

    public static ProfissionalJpaEntity paraJpa(Profissional profissional) {
        ProfissionalJpaEntity entity = new ProfissionalJpaEntity();
        entity.setId(profissional.id().valor());
        entity.setEstabelecimentoId(profissional.estabelecimentoId().valor());
        entity.setNome(profissional.nome());
        return entity;
    }

    public static Profissional paraDominio(ProfissionalJpaEntity entity) {
        return new Profissional(new ProfissionalId(entity.getId()),
                new PrestadorId(entity.getEstabelecimentoId()), entity.getNome());
    }
}
