package br.com.josewilson.agendamento.adapter.in.web;

import br.com.josewilson.agendamento.adapter.in.web.security.SecurityConfig;
import br.com.josewilson.agendamento.adapter.in.web.security.TestAutenticacao;
import br.com.josewilson.agendamento.application.exception.AcessoNaoAutorizadoException;
import br.com.josewilson.agendamento.application.port.in.GerenciarBloqueiosUseCase;
import br.com.josewilson.agendamento.application.port.in.GerenciarProfissionaisUseCase;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.profissional.BloqueioDeAgenda;
import br.com.josewilson.agendamento.domain.model.profissional.BloqueioId;
import br.com.josewilson.agendamento.domain.model.profissional.Profissional;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ProfissionalController.class, BloqueioController.class})
@Import(SecurityConfig.class)
class ProfissionalControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private GerenciarProfissionaisUseCase profissionais;
    @MockitoBean
    private GerenciarBloqueiosUseCase bloqueios;

    private final PrestadorId dono = PrestadorId.novo();

    @Test
    void listagemDeProfissionaisEPublica() throws Exception {
        given(profissionais.listar(dono)).willReturn(List.of(new Profissional(ProfissionalId.novo(), dono, "Ana")));

        mockMvc.perform(get("/api/profissionais").param("prestadorId", dono.valor().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Ana"));
    }

    @Test
    void prestadorCadastraAtualizaEExcluiProfissional() throws Exception {
        Profissional ana = new Profissional(ProfissionalId.novo(), dono, "Ana");
        given(profissionais.cadastrar(dono, "Ana")).willReturn(ana);
        given(profissionais.atualizar(dono, ana.id(), "Ana Paula")).willReturn(new Profissional(ana.id(), dono, "Ana Paula"));
        var login = authentication(TestAutenticacao.doPrestador(dono));

        mockMvc.perform(post("/api/profissionais").with(csrf()).with(login)
                        .contentType("application/json").content("{\"nome\": \"Ana\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Ana"));
        mockMvc.perform(put("/api/profissionais/" + ana.id().valor()).with(csrf()).with(login)
                        .contentType("application/json").content("{\"nome\": \"Ana Paula\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Paula"));
        mockMvc.perform(delete("/api/profissionais/" + ana.id().valor()).with(csrf()).with(login))
                .andExpect(status().isNoContent());
        verify(profissionais).excluir(dono, ana.id());
    }

    @Test
    void alterarExigePrestadorLogado() throws Exception {
        mockMvc.perform(post("/api/profissionais").with(csrf()).contentType("application/json").content("{\"nome\": \"Ana\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/profissionais").with(csrf()).with(authentication(TestAutenticacao.doCliente(ClienteId.novo())))
                        .contentType("application/json").content("{\"nome\": \"Ana\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void naoPermiteExcluirProfissionalDeOutroEstabelecimento() throws Exception {
        ProfissionalId id = ProfissionalId.novo();
        willThrow(new AcessoNaoAutorizadoException("alheio")).given(profissionais).excluir(any(), any());

        mockMvc.perform(delete("/api/profissionais/" + id.valor()).with(csrf())
                        .with(authentication(TestAutenticacao.doPrestador(dono))))
                .andExpect(status().isForbidden());
    }

    @Test
    void bloqueiosSaoSoDoPrestadorDono() throws Exception {
        ProfissionalId id = ProfissionalId.novo();
        Instant inicio = Instant.parse("2030-01-10T12:00:00Z");
        BloqueioDeAgenda bloqueio = new BloqueioDeAgenda(BloqueioId.novo(), id,
                new Periodo(inicio, inicio.plusSeconds(3600)), "Almoco");
        given(bloqueios.bloquear(any(), any(), any(), any())).willReturn(bloqueio);
        given(bloqueios.listar(dono, id)).willReturn(List.of(bloqueio));
        var login = authentication(TestAutenticacao.doPrestador(dono));

        mockMvc.perform(post("/api/profissionais/" + id.valor() + "/bloqueios").with(csrf()).with(login)
                        .contentType("application/json")
                        .content("{\"inicio\": \"2030-01-10T12:00:00Z\", \"fim\": \"2030-01-10T13:00:00Z\", \"motivo\": \"Almoco\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.motivo").value("Almoco"));
        mockMvc.perform(get("/api/profissionais/" + id.valor() + "/bloqueios").with(login))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].motivo").value("Almoco"));
        mockMvc.perform(delete("/api/profissionais/" + id.valor() + "/bloqueios/" + bloqueio.id().valor()).with(csrf()).with(login))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/profissionais/" + id.valor() + "/bloqueios")).andExpect(status().isUnauthorized());
    }
}
