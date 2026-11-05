package br.com.josewilson.agendamento.adapter.out.persistence.jpa;

import br.com.josewilson.agendamento.application.port.out.FilaDeEstornos;
import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class FilaDeEstornosAdapterIT extends AbstractPersistenceIT {

    @Autowired
    private FilaDeEstornos fila;

    @Test
    void deveEnfileirarContarTentativasEConcluir() {
        fila.registrar(AgendamentoId.novo(), "pay_1", Dinheiro.de("30.00"), "erro", Instant.now());

        FilaDeEstornos.EstornoPendente pendente = fila.buscarPendentes(3).get(0);
        assertThat(pendente.referencia()).isEqualTo("pay_1");
        assertThat(pendente.valor().valor()).isEqualByComparingTo("30.00");
        assertThat(pendente.tentativas()).isEqualTo(1);

        fila.registrarNovaFalha(pendente.id(), "erro 2");
        fila.registrarNovaFalha(pendente.id(), "erro 3");
        assertThat(fila.buscarPendentes(3)).isEmpty(); // esgotado

        fila.concluir(pendente.id());
        assertThat(fila.buscarPendentes(10)).isEmpty();
    }
}
