package org.example.agendamento.adapter.out.persistence.jpa.mapper;

import org.example.agendamento.adapter.out.persistence.jpa.entity.ClienteJpaEntity;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.shared.Contato;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;

public final class ClienteMapper {

    private ClienteMapper() {
    }

    public static ClienteJpaEntity paraJpa(Cliente cliente) {
        ClienteJpaEntity entity = new ClienteJpaEntity();
        entity.setId(cliente.id().valor());
        entity.setNome(cliente.nome());
        entity.setEmail(cliente.contato().email());
        entity.setTelefone(cliente.contato().telefone());
        entity.setDocumentoNumero(cliente.documento().numero());
        entity.setDocumentoTipo(cliente.documento().tipo().name());
        entity.setQuantidadeNoShow(cliente.quantidadeNoShow());
        return entity;
    }

    public static Cliente paraDominio(ClienteJpaEntity entity) {
        Contato contato = new Contato(entity.getEmail(), entity.getTelefone());
        DocumentoFiscal documento = new DocumentoFiscal(entity.getDocumentoNumero(),
                DocumentoFiscal.TipoDocumento.valueOf(entity.getDocumentoTipo()));
        return new Cliente(new ClienteId(entity.getId()), entity.getNome(), contato, documento,
                entity.getQuantidadeNoShow());
    }
}
