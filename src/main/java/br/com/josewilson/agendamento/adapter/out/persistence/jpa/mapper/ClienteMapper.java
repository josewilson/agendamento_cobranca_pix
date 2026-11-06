package br.com.josewilson.agendamento.adapter.out.persistence.jpa.mapper;

import br.com.josewilson.agendamento.adapter.out.persistence.jpa.entity.ClienteJpaEntity;
import br.com.josewilson.agendamento.domain.model.cliente.CanalPreferido;
import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;
import br.com.josewilson.agendamento.domain.model.shared.Contato;
import br.com.josewilson.agendamento.domain.model.shared.DocumentoFiscal;

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
        entity.setCanalPreferido(cliente.canalPreferido().name());
        entity.setSenhaHash(cliente.senhaHash());
        return entity;
    }

    public static Cliente paraDominio(ClienteJpaEntity entity) {
        Contato contato = new Contato(entity.getEmail(), entity.getTelefone());
        DocumentoFiscal documento = new DocumentoFiscal(entity.getDocumentoNumero(),
                DocumentoFiscal.TipoDocumento.valueOf(entity.getDocumentoTipo()));
        Cliente cliente = new Cliente(new ClienteId(entity.getId()), entity.getNome(), contato, documento,
                entity.getQuantidadeNoShow(), CanalPreferido.valueOf(entity.getCanalPreferido()));
        cliente.definirSenhaHash(entity.getSenhaHash());
        return cliente;
    }
}
