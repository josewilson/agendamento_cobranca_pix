package org.example.agendamento.adapter.in.web;

import jakarta.validation.Valid;
import org.example.agendamento.adapter.in.web.security.PrestadorPrincipal;
import org.example.agendamento.adapter.in.web.dto.CadastrarServicoRequest;
import org.example.agendamento.adapter.in.web.dto.ServicoResponse;
import org.example.agendamento.application.port.in.AtualizarServicoCommand;
import org.example.agendamento.application.port.in.AtualizarServicoUseCase;
import org.example.agendamento.application.port.in.CadastrarServicoCommand;
import org.example.agendamento.application.port.in.CadastrarServicoUseCase;
import org.example.agendamento.application.port.in.ExcluirServicoCommand;
import org.example.agendamento.application.port.in.ExcluirServicoUseCase;
import org.example.agendamento.application.port.in.ListarServicosPorPrestadorQuery;
import org.example.agendamento.application.port.in.ListarServicosPorPrestadorUseCase;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.Servico;
import org.example.agendamento.domain.model.servico.ServicoId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final AtualizarServicoUseCase atualizarServicoUseCase;
    private final ExcluirServicoUseCase excluirServicoUseCase;

    public ServicoController(CadastrarServicoUseCase cadastrarServicoUseCase,
                              ListarServicosPorPrestadorUseCase listarServicosPorPrestadorUseCase,
                              AtualizarServicoUseCase atualizarServicoUseCase,
                              ExcluirServicoUseCase excluirServicoUseCase) {
        this.cadastrarServicoUseCase = cadastrarServicoUseCase;
        this.listarServicosPorPrestadorUseCase = listarServicosPorPrestadorUseCase;
        this.atualizarServicoUseCase = atualizarServicoUseCase;
        this.excluirServicoUseCase = excluirServicoUseCase;
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
    public ResponseEntity<List<ServicoResponse>> listarPorPrestador(@RequestParam UUID prestadorId,
                                                                      @RequestParam(required = false) Integer page,
                                                                      @RequestParam(required = false) Integer size) {
        ListarServicosPorPrestadorQuery query = new ListarServicosPorPrestadorQuery(new PrestadorId(prestadorId));
        List<ServicoResponse> todos = listarServicosPorPrestadorUseCase.executar(query).stream().map(ServicoResponse::de).toList();
        return Paginacao.aplicar(todos, page, size);
    }

    @PutMapping("/{id}")
    public ServicoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody CadastrarServicoRequest request,
                                      @AuthenticationPrincipal PrestadorPrincipal principal) {
        AtualizarServicoCommand command = new AtualizarServicoCommand(new ServicoId(id), principal.prestadorId(),
                request.nome(), request.duracaoMinutos(), request.preco(), request.percentualSinal());
        Servico servico = atualizarServicoUseCase.executar(command);
        return ServicoResponse.de(servico);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id, @AuthenticationPrincipal PrestadorPrincipal principal) {
        excluirServicoUseCase.executar(new ExcluirServicoCommand(new ServicoId(id), principal.prestadorId()));
    }
}
