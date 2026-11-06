import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';

export default function AgendaPage() {
  const navigate = useNavigate();
  const { prestador } = useAuth();
  const [prestadores, setPrestadores] = useState([]);
  const [prestadorId, setPrestadorId] = useState('');
  const [agendamentos, setAgendamentos] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [servicos, setServicos] = useState([]);
  const [profissionais, setProfissionais] = useState([]);
  const [erro, setErro] = useState(null);
  const [carregando, setCarregando] = useState(true);

  // Lista de prestadores pro seletor + a propria sessao como selecao inicial (prestador ve a
  // propria agenda por padrao, mas pode trocar pra ver a de qualquer outro).
  useEffect(() => {
    api.get('/api/prestadores').then(setPrestadores).catch((e) => setErro(e.message));
    if (prestador?.prestadorId) {
      setPrestadorId(prestador.prestadorId);
    }
  }, [prestador]);

  useEffect(() => {
    if (!prestadorId) {
      return;
    }
    setErro(null);
    setCarregando(true);
    Promise.all([
      api.get(`/api/agendamentos?prestadorId=${prestadorId}`),
      api.get('/api/clientes'),
      api.get(`/api/servicos?prestadorId=${prestadorId}`),
      api.get(`/api/profissionais?prestadorId=${prestadorId}`),
    ])
      .then(([listaAgendamentos, listaClientes, listaServicos, listaProfissionais]) => {
        setAgendamentos(listaAgendamentos);
        setClientes(listaClientes);
        setServicos(listaServicos);
        setProfissionais(listaProfissionais);
      })
      .catch((e) => setErro(e.message))
      .finally(() => setCarregando(false));
  }, [prestadorId]);

  function nomeCliente(clienteId) {
    return clientes.find((c) => c.id === clienteId)?.nome ?? '(cliente removido)';
  }

  function nomeProfissional(profissionalId) {
    return profissionais.find((p) => p.id === profissionalId)?.nome ?? '—';
  }

  function nomeServico(servicoId) {
    return servicos.find((s) => s.id === servicoId)?.nome ?? '(serviço removido)';
  }

  const hoje = new Date();
  const agendamentosHoje = agendamentos.filter((a) => {
    const inicio = new Date(a.inicio);
    return (
      inicio.getFullYear() === hoje.getFullYear() &&
      inicio.getMonth() === hoje.getMonth() &&
      inicio.getDate() === hoje.getDate()
    );
  }).length;
  const pendentes = agendamentos.filter((a) => a.status === 'PENDENTE_PAGAMENTO').length;

  return (
    <div>
      <h1>Agenda</h1>

      <label className="seletor-prestador">
        Prestador
        <select value={prestadorId} onChange={(e) => setPrestadorId(e.target.value)}>
          <option value="">Selecione...</option>
          {prestadores.map((p) => (
            <option key={p.id} value={p.id}>
              {p.nome}
            </option>
          ))}
        </select>
      </label>

      {erro && <p className="erro">{erro}</p>}

      {!erro && !carregando && prestadorId && agendamentos.length === 0 && <p>Nenhum agendamento ainda.</p>}

      {agendamentos.length > 0 && (
        <div className="resumo">
          <div className="resumo-card">
            <strong>{agendamentos.length}</strong>
            <span>No total</span>
          </div>
          <div className="resumo-card">
            <strong>{agendamentosHoje}</strong>
            <span>Hoje</span>
          </div>
          <div className="resumo-card">
            <strong>{pendentes}</strong>
            <span>Aguardando pagamento</span>
          </div>
        </div>
      )}

      {agendamentos.length > 0 && (
        <table className="tabela">
          <thead>
            <tr>
              <th>Início</th>
              <th>Profissional</th>
              <th>Cliente</th>
              <th>Serviço</th>
              <th>Valor</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {agendamentos.map((a) => (
              <tr key={a.id ?? a.inicio}>
                <td>{new Date(a.inicio).toLocaleString('pt-BR')}</td>
                <td>{nomeProfissional(a.profissionalId)}</td>
                <td>{a.id ? nomeCliente(a.clienteId) : "—"}</td>
                <td>{a.id ? nomeServico(a.servicoId) : "—"}</td>
                <td>{a.id ? `R$ ${a.valorServico}` : "—"}</td>
                <td>
                  <span className="status-badge" data-status={a.status}>
                    {a.status}
                  </span>
                </td>
                <td>
                  {a.id && <button onClick={() => navigate(`/agendamentos/${a.id}`)}>Abrir</button>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
