package org.example.agendamento.adapter.in.web;

import jakarta.validation.Valid;
import org.example.agendamento.adapter.in.web.security.PrestadorPrincipal;
import org.example.agendamento.adapter.in.web.dto.CadastrarServicoRequest;
import org.example.agendamento.adapter.in.web.dto.ServicoResponse;
import org.example.agendamento.application.port.in.CadastrarServicoCommand;
import org.example.agendamento.application.port.in.CadastrarServicoUseCase;
import org.example.agendamento.application.port.in.ListarServicosPorPrestadorQuery;
import org.example.agendamento.application.port.in.ListarServicosPorPrestadorUseCase;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.Servico;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/servicos")
public class ServicoController {

    private final CadastrarServicoUseCase cadastrarServicoUseCase;
    private final ListarServicosPorPrestadorUseCase listarServicosPorPrestadorUseCase;

    public ServicoController(CadastrarServicoUseCase cadastrarServicoUseCase,
                              ListarServicosPorPrestadorUseCase listarServicosPorPrestadorUseCase) {
        this.cadastrarServicoUseCase = cadastrarServicoUseCase;
        this.listarServicosPorPrestadorUseCase = listarServicosPorPrestadorUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicoResponse cadastrar(@Valid @RequestBody CadastrarServicoRequest request,
                                      @AuthenticationPrincipal PrestadorPrincipal principal) {
        CadastrarServicoCommand command = new CadastrarServicoCommand(
                principal.prestadorId(), request.nome(), request.duracaoMinutos(),
                request.preco(), request.percentualSinal());
        Servico servico = cadastrarServicoUseCase.executar(command);
        return ServicoResponse.de(servico);
    }

    @GetMapping
    public List<ServicoResponse> listarPorPrestador(@RequestParam UUID prestadorId) {
        ListarServicosPorPrestadorQuery query = new ListarServicosPorPrestadorQuery(new PrestadorId(prestadorId));
        return listarServicosPorPrestadorUseCase.executar(query).stream().map(ServicoResponse::de).toList();
    }
}
