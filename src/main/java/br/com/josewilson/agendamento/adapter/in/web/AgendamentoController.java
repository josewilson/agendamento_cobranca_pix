package br.com.josewilson.agendamento.adapter.in.web;

import jakarta.validation.Valid;
import br.com.josewilson.agendamento.adapter.in.web.security.AutorizacaoAgendamento;
import br.com.josewilson.agendamento.adapter.in.web.security.ClientePrincipal;
import br.com.josewilson.agendamento.adapter.in.web.security.PrestadorPrincipal;
import br.com.josewilson.agendamento.adapter.in.web.dto.AgendamentoResponse;
import br.com.josewilson.agendamento.adapter.in.web.dto.CriarAgendamentoRequest;
import br.com.josewilson.agendamento.adapter.in.web.dto.CriarAgendamentoResponse;
import br.com.josewilson.agendamento.adapter.in.web.dto.ResultadoCancelamentoResponse;
import br.com.josewilson.agendamento.application.port.in.CancelarAgendamentoCommand;
import br.com.josewilson.agendamento.application.port.in.CancelarAgendamentoUseCase;
import br.com.josewilson.agendamento.application.port.in.ConsultarAgendamentoQuery;
import br.com.josewilson.agendamento.application.port.in.ConsultarAgendamentoUseCase;
import br.com.josewilson.agendamento.application.port.in.CriarAgendamentoCommand;
import br.com.josewilson.agendamento.application.port.in.CriarAgendamentoUseCase;
import br.com.josewilson.agendamento.application.port.in.ListarAgendamentosPorPrestadorQuery;
import br.com.josewilson.agendamento.application.port.in.ListarAgendamentosPorPrestadorUseCase;
import br.com.josewilson.agendamento.application.port.in.MarcarNoShowCommand;
import br.com.josewilson.agendamento.application.port.in.MarcarNoShowUseCase;
import br.com.josewilson.agendamento.application.port.in.ResultadoCriacaoAgendamento;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.agendamento.ResultadoCancelamento;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;
import br.com.josewilson.agendamento.domain.model.servico.ServicoId;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final CriarAgendamentoUseCase criarAgendamentoUseCase;
    private final ConsultarAgendamentoUseCase consultarAgendamentoUseCase;
    private final CancelarAgendamentoUseCase cancelarAgendamentoUseCase;
    private final MarcarNoShowUseCase marcarNoShowUseCase;
    private final ListarAgendamentosPorPrestadorUseCase listarAgendamentosPorPrestadorUseCase;

    public AgendamentoController(CriarAgendamentoUseCase criarAgendamentoUseCase,
                                  ConsultarAgendamentoUseCase consultarAgendamentoUseCase,
                                  CancelarAgendamentoUseCase cancelarAgendamentoUseCase,
                                  MarcarNoShowUseCase marcarNoShowUseCase,
                                  ListarAgendamentosPorPrestadorUseCase listarAgendamentosPorPrestadorUseCase) {
        this.criarAgendamentoUseCase = criarAgendamentoUseCase;
        this.consultarAgendamentoUseCase = consultarAgendamentoUseCase;
        this.cancelarAgendamentoUseCase = cancelarAgendamentoUseCase;
        this.marcarNoShowUseCase = marcarNoShowUseCase;
        this.listarAgendamentosPorPrestadorUseCase = listarAgendamentosPorPrestadorUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CriarAgendamentoResponse criar(@Valid @RequestBody CriarAgendamentoRequest request,
                                           @AuthenticationPrincipal ClientePrincipal cliente) {
        // o cliente vem da sessao, nunca do corpo: ninguem agenda em nome de outro
        Periodo periodo = new Periodo(request.inicio(), request.fim());
        CriarAgendamentoCommand command = new CriarAgendamentoCommand(
                new PrestadorId(request.prestadorId()),
                cliente.clienteId(),
                new ServicoId(request.servicoId()),
                periodo,
                request.profissionalId() == null ? null : new ProfissionalId(request.profissionalId()));
        ResultadoCriacaoAgendamento resultado = criarAgendamentoUseCase.executar(command);
        return CriarAgendamentoResponse.de(resultado);
    }

    @GetMapping("/{id}")
    public AgendamentoResponse consultar(@PathVariable UUID id, Authentication authentication) {
        Agendamento agendamento = consultarAgendamentoUseCase.executar(new ConsultarAgendamentoQuery(new AgendamentoId(id)));
        AutorizacaoAgendamento.exigirParticipante(agendamento, authentication);
        return AgendamentoResponse.de(agendamento);
    }

    @GetMapping
    public ResponseEntity<List<AgendamentoResponse>> listar(
            @AuthenticationPrincipal PrestadorPrincipal principal,
            @RequestParam(required = false) UUID prestadorId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        // prestadorId explicito permite ver a agenda de qualquer prestador (seletor na tela de
        // Agenda), mas so o dono ve os detalhes; para a agenda alheia devolve apenas a ocupacao
        // (horario + status), sem id/cliente/servico/valor.
        PrestadorId alvo = prestadorId != null ? new PrestadorId(prestadorId) : principal.prestadorId();
        boolean dono = alvo.equals(principal.prestadorId());
        ListarAgendamentosPorPrestadorQuery query = new ListarAgendamentosPorPrestadorQuery(alvo);
        java.util.function.Function<Agendamento, AgendamentoResponse> mapeador =
                dono ? AgendamentoResponse::de : AgendamentoResponse::ocupacao;
        if (Paginacao.paginado(page, size)) {
            return Paginacao.pagina(
                    listarAgendamentosPorPrestadorUseCase.executar(query, Paginacao.pagina(page), Paginacao.tamanho(size)),
                    mapeador);
        }
        return Paginacao.lista(listarAgendamentosPorPrestadorUseCase.executar(query).stream().map(mapeador).toList());
    }

    @PostMapping("/{id}/cancelar")
    public ResultadoCancelamentoResponse cancelar(@PathVariable UUID id, Authentication authentication) {
        AutorizacaoAgendamento.exigirParticipante(
                consultarAgendamentoUseCase.executar(new ConsultarAgendamentoQuery(new AgendamentoId(id))), authentication);
        ResultadoCancelamento resultado = cancelarAgendamentoUseCase.executar(new CancelarAgendamentoCommand(new AgendamentoId(id)));
        return ResultadoCancelamentoResponse.de(resultado);
    }

    @PostMapping("/{id}/no-show")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarNoShow(@PathVariable UUID id, Authentication authentication) {
        AutorizacaoAgendamento.exigirPrestadorDono(
                consultarAgendamentoUseCase.executar(new ConsultarAgendamentoQuery(new AgendamentoId(id))), authentication);
        marcarNoShowUseCase.executar(new MarcarNoShowCommand(new AgendamentoId(id)));
    }
}
