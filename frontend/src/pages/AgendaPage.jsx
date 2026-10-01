import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';

export default function AgendaPage() {
  const navigate = useNavigate();
  const [prestadores, setPrestadores] = useState([]);
  const [prestadorId, setPrestadorId] = useState('');
  const [agendamentos, setAgendamentos] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [servicos, setServicos] = useState([]);
  const [erro, setErro] = useState(null);
  const [carregando, setCarregando] = useState(false);

  useEffect(() => {
    api.get('/api/prestadores').then(setPrestadores).catch((e) => setErro(e.message));
    api.get('/api/clientes').then(setClientes).catch((e) => setErro(e.message));
  }, []);

  useEffect(() => {
    if (!prestadorId) {
      setAgendamentos([]);
      setServicos([]);
      return;
    }
    setErro(null);
    setCarregando(true);
    Promise.all([
      api.get(`/api/agendamentos?prestadorId=${prestadorId}`),
      api.get(`/api/servicos?prestadorId=${prestadorId}`),
    ])
      .then(([listaAgendamentos, listaServicos]) => {
        setAgendamentos(listaAgendamentos);
        setServicos(listaServicos);
      })
      .catch((e) => setErro(e.message))
      .finally(() => setCarregando(false));
  }, [prestadorId]);

  function nomeCliente(clienteId) {
    return clientes.find((c) => c.id === clienteId)?.nome ?? '(cliente removido)';
  }

  function nomeServico(servicoId) {
    return servicos.find((s) => s.id === servicoId)?.nome ?? '(serviço removido)';
  }

  return (
    <div>
      <h1>Agenda</h1>

      <label className="filtro">
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

      {!erro && prestadorId && !carregando && agendamentos.length === 0 && <p>Nenhum agendamento para este prestador.</p>}

      {agendamentos.length > 0 && (
        <table className="tabela">
          <thead>
            <tr>
              <th>Início</th>
              <th>Cliente</th>
              <th>Serviço</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {agendamentos.map((a) => (
              <tr key={a.id}>
                <td>{new Date(a.inicio).toLocaleString('pt-BR')}</td>
                <td>{nomeCliente(a.clienteId)}</td>
                <td>{nomeServico(a.servicoId)}</td>
                <td>
                  <span className="status-badge" data-status={a.status}>
                    {a.status}
                  </span>
                </td>
                <td>
                  <button onClick={() => navigate(`/agendamentos/${a.id}`)}>Abrir</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
