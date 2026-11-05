package br.com.josewilson.agendamento.application.port.out;

import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Estornos que falharam no gateway e aguardam reprocessamento (persistente, sobrevive a reinicio). */
public interface FilaDeEstornos {

    record EstornoPendente(UUID id, AgendamentoId agendamentoId, String referencia, Dinheiro valor, int tentativas) {
    }

    /** Enfileira um estorno cuja primeira tentativa ja falhou (conta como tentativa 1). */
    void registrar(AgendamentoId agendamentoId, String referencia, Dinheiro valor, String erro, Instant agora);

    /** Pendentes que ainda nao esgotaram {@code maxTentativas}. */
    List<EstornoPendente> buscarPendentes(int maxTentativas);

    void registrarNovaFalha(UUID id, String erro);

    /** Estorno concluido: remove da fila. */
    void concluir(UUID id);
}
