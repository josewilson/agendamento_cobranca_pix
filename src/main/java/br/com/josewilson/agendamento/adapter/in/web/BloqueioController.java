package br.com.josewilson.agendamento.adapter.in.web;

import jakarta.validation.Valid;
import br.com.josewilson.agendamento.adapter.in.web.dto.BloqueioRequest;
import br.com.josewilson.agendamento.adapter.in.web.dto.BloqueioResponse;
import br.com.josewilson.agendamento.adapter.in.web.security.PrestadorPrincipal;
import br.com.josewilson.agendamento.application.port.in.GerenciarBloqueiosUseCase;
import br.com.josewilson.agendamento.domain.model.profissional.BloqueioId;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Horarios bloqueados na agenda de um profissional (so o estabelecimento dono). */
@RestController
@RequestMapping("/api/profissionais/{profissionalId}/bloqueios")
public class BloqueioController {

    private final GerenciarBloqueiosUseCase useCase;

    public BloqueioController(GerenciarBloqueiosUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public List<BloqueioResponse> listar(@PathVariable UUID profissionalId,
                                         @AuthenticationPrincipal PrestadorPrincipal principal) {
        return useCase.listar(principal.prestadorId(), new ProfissionalId(profissionalId)).stream()
                .map(BloqueioResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BloqueioResponse bloquear(@PathVariable UUID profissionalId, @Valid @RequestBody BloqueioRequest request,
                                     @AuthenticationPrincipal PrestadorPrincipal principal) {
        return BloqueioResponse.de(useCase.bloquear(principal.prestadorId(), new ProfissionalId(profissionalId),
                new Periodo(request.inicio(), request.fim()), request.motivo()));
    }

    @DeleteMapping("/{bloqueioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID profissionalId, @PathVariable UUID bloqueioId,
                        @AuthenticationPrincipal PrestadorPrincipal principal) {
        useCase.remover(principal.prestadorId(), new ProfissionalId(profissionalId), new BloqueioId(bloqueioId));
    }
}
