package br.com.josewilson.agendamento.adapter.out.persistence.jpa.mapper;

import br.com.josewilson.agendamento.adapter.out.persistence.jpa.entity.PrestadorJpaEntity;
import br.com.josewilson.agendamento.domain.model.prestador.Prestador;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.shared.DocumentoFiscal;
import br.com.josewilson.agendamento.domain.model.shared.PoliticaCancelamento;

import java.time.Duration;

public final class PrestadorMapper {

    private PrestadorMapper() {
    }

    public static PrestadorJpaEntity paraJpa(Prestador prestador) {
        PrestadorJpaEntity entity = new PrestadorJpaEntity();
        entity.setId(prestador.id().valor());
        entity.setNome(prestador.nome());
        entity.setTelefone(prestador.telefone());
        entity.setEmail(prestador.email());
        entity.setSenhaHash(prestador.senhaHash());
        entity.setDocumentoNumero(prestador.documento().numero());
        entity.setDocumentoTipo(prestador.documento().tipo().name());
        entity.setPoliticaAntecedenciaMinimaSegundos(prestador.politicaCancelamentoPadrao().antecedenciaMinima().getSeconds());
        entity.setPoliticaPercentualRetido(prestador.politicaCancelamentoPadrao().percentualRetido());
        return entity;
    }

    public static Prestador paraDominio(PrestadorJpaEntity entity) {
        DocumentoFiscal documento = new DocumentoFiscal(entity.getDocumentoNumero(),
                DocumentoFiscal.TipoDocumento.valueOf(entity.getDocumentoTipo()));
        PoliticaCancelamento politica = new PoliticaCancelamento(
                Duration.ofSeconds(entity.getPoliticaAntecedenciaMinimaSegundos()),
                entity.getPoliticaPercentualRetido());
        return new Prestador(new PrestadorId(entity.getId()), entity.getNome(), entity.getTelefone(),
                entity.getEmail(), entity.getSenhaHash(), documento, politica);
    }
}
