package br.com.josewilson.agendamento.adapter.in.web;

import jakarta.validation.Valid;
import br.com.josewilson.agendamento.adapter.in.web.dto.ProfissionalRequest;
import br.com.josewilson.agendamento.adapter.in.web.dto.ProfissionalResponse;
import br.com.josewilson.agendamento.adapter.in.web.security.PrestadorPrincipal;
import br.com.josewilson.agendamento.application.port.in.GerenciarProfissionaisUseCase;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;
import org.springframework.http.HttpStatus;
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

/** Profissionais de um estabelecimento: listagem publica (a reserva escolhe o profissional), edicao so do dono. */
@RestController
@RequestMapping("/api/profissionais")
public class ProfissionalController {

    private final GerenciarProfissionaisUseCase useCase;

    public ProfissionalController(GerenciarProfissionaisUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public List<ProfissionalResponse> listar(@RequestParam UUID prestadorId) {
        return useCase.listar(new PrestadorId(prestadorId)).stream().map(ProfissionalResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfissionalResponse cadastrar(@Valid @RequestBody ProfissionalRequest request,
                                          @AuthenticationPrincipal PrestadorPrincipal principal) {
        return ProfissionalResponse.de(useCase.cadastrar(principal.prestadorId(), request.nome()));
    }

    @PutMapping("/{id}")
    public ProfissionalResponse atualizar(@PathVariable UUID id, @Valid @RequestBody ProfissionalRequest request,
                                          @AuthenticationPrincipal PrestadorPrincipal principal) {
        return ProfissionalResponse.de(useCase.atualizar(principal.prestadorId(), new ProfissionalId(id), request.nome()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id, @AuthenticationPrincipal PrestadorPrincipal principal) {
        useCase.excluir(principal.prestadorId(), new ProfissionalId(id));
    }
}
