package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.application.port.in.CadastrarClienteCommand;
import br.com.josewilson.agendamento.application.port.in.CadastrarClienteUseCase;
import br.com.josewilson.agendamento.application.port.out.ClienteRepository;
import br.com.josewilson.agendamento.domain.model.cliente.CanalPreferido;
import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;
import br.com.josewilson.agendamento.domain.model.shared.Contato;
import br.com.josewilson.agendamento.domain.model.shared.DocumentoFiscal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CadastrarClienteService implements CadastrarClienteUseCase {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public CadastrarClienteService(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Cliente executar(CadastrarClienteCommand command) {
        DocumentoFiscal.TipoDocumento tipo = DocumentoFiscal.TipoDocumento.valueOf(command.documentoTipo().toUpperCase());
        DocumentoFiscal documento = new DocumentoFiscal(command.documentoNumero(), tipo);
        Contato contato = new Contato(command.email(), command.telefone());
        Cliente cliente = new Cliente(ClienteId.novo(), command.nome(), contato, documento);
        if (command.canalPreferido() != null) {
            cliente.definirCanalPreferido(CanalPreferido.valueOf(command.canalPreferido().toUpperCase()));
        }
        if (command.senha() != null) {
            cliente.definirSenhaHash(passwordEncoder.encode(command.senha()));
        }
        return clienteRepository.salvar(cliente);
    }
}
