package org.example.agendamento.adapter.in.web;

import jakarta.validation.Valid;
import org.example.agendamento.adapter.in.web.dto.AtualizarPrestadorRequest;
import org.example.agendamento.adapter.in.web.dto.CadastrarPrestadorRequest;
import org.example.agendamento.adapter.in.web.dto.PrestadorResponse;
import org.example.agendamento.adapter.in.web.security.PrestadorPrincipal;
import org.example.agendamento.application.port.in.AtualizarPrestadorCommand;
import org.example.agendamento.application.port.in.AtualizarPrestadorUseCase;
import org.example.agendamento.application.port.in.CadastrarPrestadorCommand;
import org.example.agendamento.application.port.in.CadastrarPrestadorUseCase;
import org.example.agendamento.application.port.in.ListarPrestadoresUseCase;
import org.example.agendamento.domain.model.prestador.Prestador;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/prestadores")
public class PrestadorController {

    private final CadastrarPrestadorUseCase cadastrarPrestadorUseCase;
    private final ListarPrestadoresUseCase listarPrestadoresUseCase;
    private final AtualizarPrestadorUseCase atualizarPrestadorUseCase;

    public PrestadorController(CadastrarPrestadorUseCase cadastrarPrestadorUseCase,
                                ListarPrestadoresUseCase listarPrestadoresUseCase,
                                AtualizarPrestadorUseCase atualizarPrestadorUseCase) {
        this.cadastrarPrestadorUseCase = cadastrarPrestadorUseCase;
        this.listarPrestadoresUseCase = listarPrestadoresUseCase;
        this.atualizarPrestadorUseCase = atualizarPrestadorUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrestadorResponse cadastrar(@Valid @RequestBody CadastrarPrestadorRequest request) {
        CadastrarPrestadorCommand command = new CadastrarPrestadorCommand(
                request.nome(), request.telefone(), request.email(), request.senha(),
                request.documentoNumero(), request.documentoTipo());
        Prestador prestador = cadastrarPrestadorUseCase.executar(command);
        return PrestadorResponse.de(prestador);
    }

    @GetMapping
    public List<PrestadorResponse> listar() {
        return listarPrestadoresUseCase.executar().stream().map(PrestadorResponse::de).toList();
    }

    @PutMapping("/{id}")
    public PrestadorResponse atualizar(@PathVariable UUID id, @Valid @RequestBody AtualizarPrestadorRequest request,
                                        @AuthenticationPrincipal PrestadorPrincipal principal) {
        AtualizarPrestadorCommand command = new AtualizarPrestadorCommand(
                new PrestadorId(id), principal.prestadorId(), request.nome(), request.telefone());
        Prestador prestador = atualizarPrestadorUseCase.executar(command);
        return PrestadorResponse.de(prestador);
    }
}
