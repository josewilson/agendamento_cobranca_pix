package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.application.exception.AcessoNaoAutorizadoException;
import br.com.josewilson.agendamento.application.exception.RecursoNaoEncontradoException;
import br.com.josewilson.agendamento.application.port.in.GerenciarBloqueiosUseCase;
import br.com.josewilson.agendamento.application.port.out.AgendamentoRepository;
import br.com.josewilson.agendamento.application.port.out.BloqueioRepository;
import br.com.josewilson.agendamento.application.port.out.Clock;
import br.com.josewilson.agendamento.application.port.out.ProfissionalRepository;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.profissional.BloqueioDeAgenda;
import br.com.josewilson.agendamento.domain.model.profissional.BloqueioId;
import br.com.josewilson.agendamento.domain.model.profissional.Profissional;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;
import br.com.josewilson.agendamento.domain.service.VerificadorDeConflito;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GerenciarBloqueiosService implements GerenciarBloqueiosUseCase {

    private final BloqueioRepository bloqueioRepository;
    private final ProfissionalRepository profissionalRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final Clock clock;

    public GerenciarBloqueiosService(BloqueioRepository bloqueioRepository, ProfissionalRepository profissionalRepository,
                                     AgendamentoRepository agendamentoRepository, Clock clock) {
        this.bloqueioRepository = bloqueioRepository;
        this.profissionalRepository = profissionalRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.clock = clock;
    }

    @Override
    public BloqueioDeAgenda bloquear(PrestadorId solicitante, ProfissionalId profissionalId, Periodo periodo, String motivo) {
        exigirDono(solicitante, profissionalId);
        if (periodo.estaNoPassado(clock.agora())) {
            throw new IllegalArgumentException("nao e possivel bloquear um periodo no passado");
        }
        // nao deixa bloquear por cima de um agendamento ativo (cancele/remarque antes);
        // bloqueio x bloqueio e garantido pela constraint de exclusao do banco
        VerificadorDeConflito.verificarDisponibilidade(periodo,
                agendamentoRepository.buscarAtivosPorProfissional(profissionalId));
        return bloqueioRepository.salvar(new BloqueioDeAgenda(BloqueioId.novo(), profissionalId, periodo, motivo));
    }

    @Override
    public List<BloqueioDeAgenda> listar(PrestadorId solicitante, ProfissionalId profissionalId) {
        exigirDono(solicitante, profissionalId);
        return bloqueioRepository.buscarPorProfissional(profissionalId);
    }

    @Override
    public void remover(PrestadorId solicitante, ProfissionalId profissionalId, BloqueioId bloqueioId) {
        exigirDono(solicitante, profissionalId);
        BloqueioDeAgenda bloqueio = bloqueioRepository.buscarPorId(bloqueioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Bloqueio nao encontrado: " + bloqueioId));
        if (!bloqueio.profissionalId().equals(profissionalId)) {
            throw new RecursoNaoEncontradoException("Bloqueio nao encontrado: " + bloqueioId);
        }
        bloqueioRepository.excluir(bloqueioId);
    }

    private void exigirDono(PrestadorId solicitante, ProfissionalId profissionalId) {
        Profissional profissional = profissionalRepository.buscarPorId(profissionalId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional nao encontrado: " + profissionalId));
        if (!profissional.estabelecimentoId().equals(solicitante)) {
            throw new AcessoNaoAutorizadoException("Profissional nao pertence ao estabelecimento autenticado");
        }
    }
}
