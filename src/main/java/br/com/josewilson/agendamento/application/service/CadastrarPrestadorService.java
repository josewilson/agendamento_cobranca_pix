package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.application.port.in.CadastrarPrestadorCommand;
import br.com.josewilson.agendamento.application.port.in.CadastrarPrestadorUseCase;
import br.com.josewilson.agendamento.application.port.out.PrestadorRepository;
import br.com.josewilson.agendamento.domain.model.prestador.Prestador;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.shared.DocumentoFiscal;
import br.com.josewilson.agendamento.domain.model.shared.PoliticaCancelamento;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CadastrarPrestadorService implements CadastrarPrestadorUseCase {

    private final PrestadorRepository prestadorRepository;
    private final PasswordEncoder passwordEncoder;

    public CadastrarPrestadorService(PrestadorRepository prestadorRepository, PasswordEncoder passwordEncoder) {
        this.prestadorRepository = prestadorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Prestador executar(CadastrarPrestadorCommand command) {
        DocumentoFiscal.TipoDocumento tipo = DocumentoFiscal.TipoDocumento.valueOf(command.documentoTipo().toUpperCase());
        DocumentoFiscal documento = new DocumentoFiscal(command.documentoNumero(), tipo);
        String senhaHash = passwordEncoder.encode(command.senha());
        Prestador prestador = new Prestador(PrestadorId.novo(), command.nome(), command.telefone(),
                command.email(), senhaHash, documento, PoliticaCancelamento.padrao());
        return prestadorRepository.salvar(prestador);
    }
}
