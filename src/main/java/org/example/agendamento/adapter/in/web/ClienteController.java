package org.example.agendamento.adapter.in.web;

import jakarta.validation.Valid;
import org.example.agendamento.adapter.in.web.dto.CadastrarClienteRequest;
import org.example.agendamento.adapter.in.web.dto.ClienteResponse;
import org.example.agendamento.application.port.in.CadastrarClienteCommand;
import org.example.agendamento.application.port.in.CadastrarClienteUseCase;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final CadastrarClienteUseCase cadastrarClienteUseCase;

    public ClienteController(CadastrarClienteUseCase cadastrarClienteUseCase) {
        this.cadastrarClienteUseCase = cadastrarClienteUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse cadastrar(@Valid @RequestBody CadastrarClienteRequest request) {
        CadastrarClienteCommand command = new CadastrarClienteCommand(
                request.nome(), request.email(), request.telefone(),
                request.documentoNumero(), request.documentoTipo());
        Cliente cliente = cadastrarClienteUseCase.executar(command);
        return ClienteResponse.de(cliente);
    }
}
