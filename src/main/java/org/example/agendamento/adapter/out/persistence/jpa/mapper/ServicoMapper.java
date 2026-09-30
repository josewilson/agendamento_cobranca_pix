package org.example.agendamento.adapter.out.persistence.jpa.mapper;

import org.example.agendamento.adapter.out.persistence.jpa.entity.ServicoJpaEntity;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.Servico;
import org.example.agendamento.domain.model.servico.ServicoId;
import org.example.agendamento.domain.model.shared.Dinheiro;

import java.time.Duration;

public final class ServicoMapper {

    private ServicoMapper() {
    }

    public static ServicoJpaEntity paraJpa(Servico servico) {
        ServicoJpaEntity entity = new ServicoJpaEntity();
        entity.setId(servico.id().valor());
        entity.setPrestadorId(servico.prestadorId().valor());
        entity.setNome(servico.nome());
        entity.setDuracaoMinutos(servico.duracao().toMinutes());
        entity.setPreco(servico.preco().valor());
        entity.setPercentualSinal(servico.percentualSinal());
        return entity;
    }

    public static Servico paraDominio(ServicoJpaEntity entity) {
        return new Servico(new ServicoId(entity.getId()), new PrestadorId(entity.getPrestadorId()), entity.getNome(),
                Duration.ofMinutes(entity.getDuracaoMinutos()), Dinheiro.de(entity.getPreco()),
                entity.getPercentualSinal());
    }
}
