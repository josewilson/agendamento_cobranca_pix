package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.application.port.in.CadastrarPrestadorCommand;
import br.com.josewilson.agendamento.application.port.in.CadastrarPrestadorUseCase;
import br.com.josewilson.agendamento.application.port.out.PrestadorRepository;
import br.com.josewilson.agendamento.application.port.out.ProfissionalRepository;
import br.com.josewilson.agendamento.domain.model.prestador.Prestador;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.profissional.Profissional;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;
import br.com.josewilson.agendamento.domain.model.shared.DocumentoFiscal;
import br.com.josewilson.agendamento.domain.model.shared.PoliticaCancelamento;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CadastrarPrestadorService implements CadastrarPrestadorUseCase {

    private final PrestadorRepository prestadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfissionalRepository profissionalRepository;

    public CadastrarPrestadorService(PrestadorRepository prestadorRepository, PasswordEncoder passwordEncoder,
                                    ProfissionalRepository profissionalRepository) {
        this.profissionalRepository = profissionalRepository;
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
        Prestador salvo = prestadorRepository.salvar(prestador);
        // todo estabelecimento nasce com um profissional padrao (o proprio nome), para ja poder receber agendamentos
        profissionalRepository.salvar(new Profissional(ProfissionalId.novo(), salvo.id(), salvo.nome()));
        return salvo;
    }
}
