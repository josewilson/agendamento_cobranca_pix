package br.com.josewilson.agendamento.adapter.out.persistence.jpa;

import br.com.josewilson.agendamento.adapter.out.persistence.jpa.mapper.AgendamentoMapper;
import br.com.josewilson.agendamento.adapter.out.persistence.jpa.repository.AgendamentoJpaRepository;
import br.com.josewilson.agendamento.application.port.out.AgendamentoRepository;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.agendamento.StatusAgendamento;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("!dev")
public class AgendamentoRepositoryAdapter implements AgendamentoRepository {

    private static final List<String> STATUS_ATIVOS = List.of(
            StatusAgendamento.PENDENTE_PAGAMENTO.name(), StatusAgendamento.CONFIRMADO.name());

    private final AgendamentoJpaRepository jpaRepository;

    public AgendamentoRepositoryAdapter(AgendamentoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Agendamento salvar(Agendamento agendamento) {
        return AgendamentoMapper.paraDominio(jpaRepository.saveAndFlush(AgendamentoMapper.paraJpa(agendamento)));
    }

    @Override
    public Optional<Agendamento> buscarPorId(AgendamentoId id) {
        return jpaRepository.findById(id.valor()).map(AgendamentoMapper::paraDominio);
    }

    @Override
    public List<Agendamento> buscarAtivosPorPrestador(PrestadorId prestadorId) {
        return jpaRepository.findByPrestadorIdAndStatusIn(prestadorId.valor(), STATUS_ATIVOS).stream()
                .map(AgendamentoMapper::paraDominio)
                .toList();
    }

    @Override
    public List<Agendamento> buscarTodosPendentesPagamento() {
        return jpaRepository.findByStatus(StatusAgendamento.PENDENTE_PAGAMENTO.name()).stream()
                .map(AgendamentoMapper::paraDominio)
                .toList();
    }

    @Override
    public List<Agendamento> buscarPorPrestador(PrestadorId prestadorId) {
        return jpaRepository.findByPrestadorIdOrderByPeriodoInicioAsc(prestadorId.valor()).stream()
                .map(AgendamentoMapper::paraDominio)
                .toList();
    }
}
