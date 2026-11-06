package br.com.josewilson.agendamento.adapter.out.persistence.jpa;

import br.com.josewilson.agendamento.application.port.out.BloqueioRepository;
import br.com.josewilson.agendamento.application.port.out.PrestadorRepository;
import br.com.josewilson.agendamento.application.port.out.ProfissionalRepository;
import br.com.josewilson.agendamento.domain.model.prestador.Prestador;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.profissional.BloqueioDeAgenda;
import br.com.josewilson.agendamento.domain.model.profissional.BloqueioId;
import br.com.josewilson.agendamento.domain.model.profissional.Profissional;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;
import br.com.josewilson.agendamento.domain.model.shared.DocumentoFiscal;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;
import br.com.josewilson.agendamento.domain.model.shared.PoliticaCancelamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BloqueioRepositoryAdapterIT extends AbstractPersistenceIT {

    @Autowired
    private PrestadorRepository prestadorRepository;
    @Autowired
    private ProfissionalRepository profissionalRepository;
    @Autowired
    private BloqueioRepository bloqueioRepository;

    private Profissional ana;
    private Profissional beto;

    @BeforeEach
    void setUp() {
        Prestador prestador = prestadorRepository.salvar(new Prestador(PrestadorId.novo(), "Clinica", "11987654321",
                "clinica@exemplo.com", "hash-fake", DocumentoFiscal.cnpj("11.222.333/0001-81"),
                PoliticaCancelamento.padrao()));
        ana = profissionalRepository.salvar(new Profissional(ProfissionalId.novo(), prestador.id(), "Ana"));
        beto = profissionalRepository.salvar(new Profissional(ProfissionalId.novo(), prestador.id(), "Beto"));
    }

    private BloqueioDeAgenda bloqueio(Profissional p, int minutosAposInicio, int duracaoMin) {
        Instant base = Instant.now().plus(Duration.ofDays(1));
        Instant inicio = base.plus(Duration.ofMinutes(minutosAposInicio));
        return new BloqueioDeAgenda(BloqueioId.novo(), p.id(), new Periodo(inicio, inicio.plus(Duration.ofMinutes(duracaoMin))), "Almoco");
    }

    @Test
    void deveSalvarListarEExcluir() {
        BloqueioDeAgenda salvo = bloqueioRepository.salvar(bloqueio(ana, 0, 60));

        assertThat(bloqueioRepository.buscarPorProfissional(ana.id())).extracting(BloqueioDeAgenda::id).containsExactly(salvo.id());
        assertThat(bloqueioRepository.buscarPorProfissional(beto.id())).isEmpty();

        bloqueioRepository.excluir(salvo.id());
        assertThat(bloqueioRepository.buscarPorId(salvo.id())).isEmpty();
    }

    @Test
    void bancoRecusaBloqueiosSobrepostosDoMesmoProfissionalMasAceitaDeOutro() {
        bloqueioRepository.salvar(bloqueio(ana, 0, 60));
        bloqueioRepository.salvar(bloqueio(beto, 30, 60)); // outro profissional: livre (antes da violacao, que aborta a transacao do teste)

        assertThatThrownBy(() -> bloqueioRepository.salvar(bloqueio(ana, 30, 60)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void listaProfissionaisDoEstabelecimentoPorNome() {
        assertThat(profissionalRepository.buscarPorEstabelecimento(ana.estabelecimentoId()))
                .extracting(Profissional::nome).containsExactly("Ana", "Beto");
    }
}
