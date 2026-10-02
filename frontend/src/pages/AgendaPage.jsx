import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';

export default function AgendaPage() {
  const navigate = useNavigate();
  const [agendamentos, setAgendamentos] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [servicos, setServicos] = useState([]);
  const [erro, setErro] = useState(null);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    setErro(null);
    setCarregando(true);
    Promise.all([api.get('/api/agendamentos'), api.get('/api/clientes')])
      .then(([listaAgendamentos, listaClientes]) => {
        setAgendamentos(listaAgendamentos);
        setClientes(listaClientes);
        const prestadorId = listaAgendamentos[0]?.prestadorId;
        return prestadorId ? api.get(`/api/servicos?prestadorId=${prestadorId}`) : [];
      })
      .then(setServicos)
      .catch((e) => setErro(e.message))
      .finally(() => setCarregando(false));
  }, []);

  function nomeCliente(clienteId) {
    return clientes.find((c) => c.id === clienteId)?.nome ?? '(cliente removido)';
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
      <h1>Minha agenda</h1>

      {erro && <p className="erro">{erro}</p>}

      {!erro && !carregando && agendamentos.length === 0 && <p>Nenhum agendamento ainda.</p>}

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
