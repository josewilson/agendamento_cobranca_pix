package br.com.josewilson.agendamento.adapter.out.persistence.jpa;

import br.com.josewilson.agendamento.adapter.out.persistence.jpa.entity.EstornoPendenteJpaEntity;
import br.com.josewilson.agendamento.adapter.out.persistence.jpa.repository.EstornoPendenteJpaRepository;
import br.com.josewilson.agendamento.application.port.out.FilaDeEstornos;
import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
@Profile("!dev")
public class FilaDeEstornosAdapter implements FilaDeEstornos {

    private final EstornoPendenteJpaRepository jpaRepository;

    public FilaDeEstornosAdapter(EstornoPendenteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void registrar(AgendamentoId agendamentoId, String referencia, Dinheiro valor, String erro, Instant agora) {
        EstornoPendenteJpaEntity e = new EstornoPendenteJpaEntity();
        e.setId(UUID.randomUUID());
        e.setAgendamentoId(agendamentoId.valor());
        e.setReferencia(referencia);
        e.setValor(valor.valor());
        e.setTentativas(1);
        e.setUltimoErro(erro);
        e.setCriadoEm(agora);
        jpaRepository.save(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstornoPendente> buscarPendentes(int maxTentativas) {
        return jpaRepository.findByTentativasLessThanOrderByCriadoEmAsc(maxTentativas).stream()
                .map(e -> new EstornoPendente(e.getId(), new AgendamentoId(e.getAgendamentoId()),
                        e.getReferencia(), Dinheiro.de(e.getValor()), e.getTentativas()))
                .toList();
    }

    @Override
    @Transactional
    public void registrarNovaFalha(UUID id, String erro) {
        jpaRepository.findById(id).ifPresent(e -> {
            e.setTentativas(e.getTentativas() + 1);
            e.setUltimoErro(erro);
        });
    }

    @Override
    @Transactional
    public void concluir(UUID id) {
        jpaRepository.deleteById(id);
    }
}
