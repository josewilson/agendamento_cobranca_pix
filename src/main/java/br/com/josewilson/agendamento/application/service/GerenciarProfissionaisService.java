package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.application.exception.AcessoNaoAutorizadoException;
import br.com.josewilson.agendamento.application.exception.RecursoNaoEncontradoException;
import br.com.josewilson.agendamento.application.port.in.GerenciarProfissionaisUseCase;
import br.com.josewilson.agendamento.application.port.out.ProfissionalRepository;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.profissional.Profissional;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GerenciarProfissionaisService implements GerenciarProfissionaisUseCase {

    private final ProfissionalRepository profissionalRepository;

    public GerenciarProfissionaisService(ProfissionalRepository profissionalRepository) {
        this.profissionalRepository = profissionalRepository;
    }

    @Override
    public Profissional cadastrar(PrestadorId estabelecimentoId, String nome) {
        return profissionalRepository.salvar(new Profissional(ProfissionalId.novo(), estabelecimentoId, nome));
    }

    @Override
    public List<Profissional> listar(PrestadorId estabelecimentoId) {
        return profissionalRepository.buscarPorEstabelecimento(estabelecimentoId);
    }

    @Override
    public Profissional atualizar(PrestadorId solicitante, ProfissionalId id, String nome) {
        Profissional profissional = buscarDoSolicitante(solicitante, id);
        profissional.renomear(nome);
        return profissionalRepository.salvar(profissional);
    }

    @Override
    public void excluir(PrestadorId solicitante, ProfissionalId id) {
        buscarDoSolicitante(solicitante, id);
        if (profissionalRepository.buscarPorEstabelecimento(solicitante).size() <= 1) {
            throw new IllegalStateException("O estabelecimento precisa ter pelo menos um profissional");
        }
        profissionalRepository.excluir(id);
    }

    private Profissional buscarDoSolicitante(PrestadorId solicitante, ProfissionalId id) {
        Profissional profissional = profissionalRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional nao encontrado: " + id));
        if (!profissional.estabelecimentoId().equals(solicitante)) {
            throw new AcessoNaoAutorizadoException("Profissional nao pertence ao estabelecimento autenticado");
        }
        return profissional;
    }
}
