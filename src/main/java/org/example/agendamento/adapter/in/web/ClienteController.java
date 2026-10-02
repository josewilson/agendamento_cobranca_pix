package org.example.agendamento.adapter.in.web;

import jakarta.validation.Valid;
import org.example.agendamento.adapter.in.web.dto.AtualizarClienteRequest;
import org.example.agendamento.adapter.in.web.dto.CadastrarClienteRequest;
import org.example.agendamento.adapter.in.web.dto.ClienteResponse;
import org.example.agendamento.application.port.in.AtualizarClienteCommand;
import org.example.agendamento.application.port.in.AtualizarClienteUseCase;
import org.example.agendamento.application.port.in.CadastrarClienteCommand;
import org.example.agendamento.application.port.in.CadastrarClienteUseCase;
import org.example.agendamento.application.port.in.ExcluirClienteCommand;
import org.example.agendamento.application.port.in.ExcluirClienteUseCase;
import org.example.agendamento.application.port.in.ListarClientesUseCase;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/clientes")
public class ClienteController {

    private final CadastrarClienteUseCase cadastrarClienteUseCase;
    private final ListarClientesUseCase listarClientesUseCase;
    private final AtualizarClienteUseCase atualizarClienteUseCase;
    private final ExcluirClienteUseCase excluirClienteUseCase;

    public ClienteController(CadastrarClienteUseCase cadastrarClienteUseCase,
                              ListarClientesUseCase listarClientesUseCase,
                              AtualizarClienteUseCase atualizarClienteUseCase,
                              ExcluirClienteUseCase excluirClienteUseCase) {
        this.cadastrarClienteUseCase = cadastrarClienteUseCase;
        this.listarClientesUseCase = listarClientesUseCase;
        this.atualizarClienteUseCase = atualizarClienteUseCase;
        this.excluirClienteUseCase = excluirClienteUseCase;
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

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar(@RequestParam(required = false) Integer page,
                                                          @RequestParam(required = false) Integer size) {
        List<ClienteResponse> todos = listarClientesUseCase.executar().stream().map(ClienteResponse::de).toList();
        return Paginacao.aplicar(todos, page, size);
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(@PathVariable UUID id, @Valid @RequestBody AtualizarClienteRequest request) {
        AtualizarClienteCommand command = new AtualizarClienteCommand(
                new ClienteId(id), request.nome(), request.email(), request.telefone());
        Cliente cliente = atualizarClienteUseCase.executar(command);
        return ClienteResponse.de(cliente);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        excluirClienteUseCase.executar(new ExcluirClienteCommand(new ClienteId(id)));
    }
}
