package org.example.agendamento.adapter.in.web;

import jakarta.validation.Valid;
import org.example.agendamento.adapter.in.web.dto.CadastrarPrestadorRequest;
import org.example.agendamento.adapter.in.web.dto.PrestadorResponse;
import org.example.agendamento.application.port.in.CadastrarPrestadorCommand;
import org.example.agendamento.application.port.in.CadastrarPrestadorUseCase;
import org.example.agendamento.application.port.in.ListarPrestadoresUseCase;
import org.example.agendamento.domain.model.prestador.Prestador;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/prestadores")
public class PrestadorController {

    private final CadastrarPrestadorUseCase cadastrarPrestadorUseCase;
    private final ListarPrestadoresUseCase listarPrestadoresUseCase;

    public PrestadorController(CadastrarPrestadorUseCase cadastrarPrestadorUseCase,
                                ListarPrestadoresUseCase listarPrestadoresUseCase) {
        this.cadastrarPrestadorUseCase = cadastrarPrestadorUseCase;
        this.listarPrestadoresUseCase = listarPrestadoresUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrestadorResponse cadastrar(@Valid @RequestBody CadastrarPrestadorRequest request) {
        CadastrarPrestadorCommand command = new CadastrarPrestadorCommand(
                request.nome(), request.telefone(), request.documentoNumero(), request.documentoTipo());
        Prestador prestador = cadastrarPrestadorUseCase.executar(command);
        return PrestadorResponse.de(prestador);
    }

    @GetMapping
    public List<PrestadorResponse> listar() {
        return listarPrestadoresUseCase.executar().stream().map(PrestadorResponse::de).toList();
    }
}
