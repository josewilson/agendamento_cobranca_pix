package org.example.agendamento.application.service;

import org.example.agendamento.adapter.out.persistence.memory.InMemoryPrestadorRepository;
import org.example.agendamento.domain.model.prestador.Prestador;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ListarPrestadoresServiceTest {

    private final InMemoryPrestadorRepository prestadorRepository = new InMemoryPrestadorRepository();
    private final ListarPrestadoresService service = new ListarPrestadoresService(prestadorRepository);

    @Test
    void deveListarTodosOsPrestadoresCadastrados() {
        Prestador prestador1 = new Prestador(PrestadorId.novo(), "Clinica A", "11987654321",
                "clinica-a@exemplo.com", "hash-fake-de-teste", DocumentoFiscal.cnpj("11222333000181"), PoliticaCancelamento.padrao());
        Prestador prestador2 = new Prestador(PrestadorId.novo(), "Clinica B", "11988887777",
                "clinica-b@exemplo.com", "hash-fake-de-teste", DocumentoFiscal.cpf("11144477735"), PoliticaCancelamento.padrao());
        prestadorRepository.salvar(prestador1);
        prestadorRepository.salvar(prestador2);

        assertThat(service.executar()).containsExactlyInAnyOrder(prestador1, prestador2);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaPrestadores() {
        assertThat(service.executar()).isEmpty();
    }
}
