package org.example.agendamento.adapter.in.web;

import jakarta.validation.Valid;
import org.example.agendamento.adapter.in.web.security.PrestadorPrincipal;
import org.example.agendamento.adapter.in.web.dto.AgendamentoResponse;
import org.example.agendamento.adapter.in.web.dto.CriarAgendamentoRequest;
import org.example.agendamento.adapter.in.web.dto.CriarAgendamentoResponse;
import org.example.agendamento.adapter.in.web.dto.ResultadoCancelamentoResponse;
import org.example.agendamento.application.port.in.CancelarAgendamentoCommand;
import org.example.agendamento.application.port.in.CancelarAgendamentoUseCase;
import org.example.agendamento.application.port.in.ConsultarAgendamentoQuery;
import org.example.agendamento.application.port.in.ConsultarAgendamentoUseCase;
import org.example.agendamento.application.port.in.CriarAgendamentoCommand;
import org.example.agendamento.application.port.in.CriarAgendamentoUseCase;
import org.example.agendamento.application.port.in.ListarAgendamentosPorPrestadorQuery;
import org.example.agendamento.application.port.in.ListarAgendamentosPorPrestadorUseCase;
import org.example.agendamento.application.port.in.MarcarNoShowCommand;
import org.example.agendamento.application.port.in.MarcarNoShowUseCase;
import org.example.agendamento.application.port.in.ResultadoCriacaoAgendamento;
import org.example.agendamento.domain.model.agendamento.Agendamento;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.agendamento.ResultadoCancelamento;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.ServicoId;
import org.example.agendamento.domain.model.shared.Periodo;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public CriarAgendamentoResponse criar(@Valid @RequestBody CriarAgendamentoRequest request) {
        Periodo periodo = new Periodo(request.inicio(), request.fim());
        CriarAgendamentoCommand command = new CriarAgendamentoCommand(
                new PrestadorId(request.prestadorId()),
                new ClienteId(request.clienteId()),
                new ServicoId(request.servicoId()),
                periodo);
        ResultadoCriacaoAgendamento resultado = criarAgendamentoUseCase.executar(command);
        return CriarAgendamentoResponse.de(resultado);
    }

    @GetMapping("/{id}")
    public AgendamentoResponse consultar(@PathVariable UUID id) {
        Agendamento agendamento = consultarAgendamentoUseCase.executar(new ConsultarAgendamentoQuery(new AgendamentoId(id)));
        return AgendamentoResponse.de(agendamento);
    }

    @GetMapping
    public List<AgendamentoResponse> listarDoPrestadorLogado(@AuthenticationPrincipal PrestadorPrincipal principal) {
        ListarAgendamentosPorPrestadorQuery query = new ListarAgendamentosPorPrestadorQuery(principal.prestadorId());
        return listarAgendamentosPorPrestadorUseCase.executar(query).stream().map(AgendamentoResponse::de).toList();
    }

    @PostMapping("/{id}/cancelar")
    public ResultadoCancelamentoResponse cancelar(@PathVariable UUID id) {
        ResultadoCancelamento resultado = cancelarAgendamentoUseCase.executar(new CancelarAgendamentoCommand(new AgendamentoId(id)));
        return ResultadoCancelamentoResponse.de(resultado);
    }

    @PostMapping("/{id}/no-show")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarNoShow(@PathVariable UUID id) {
        marcarNoShowUseCase.executar(new MarcarNoShowCommand(new AgendamentoId(id)));
    }
}
