import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';

export default function NovoAgendamentoPage() {
  const navigate = useNavigate();
  const [prestadores, setPrestadores] = useState([]);
  const [servicos, setServicos] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [prestadorId, setPrestadorId] = useState('');
  const [servicoId, setServicoId] = useState('');
  const [clienteId, setClienteId] = useState('');
  const [inicio, setInicio] = useState('');
  const [resultado, setResultado] = useState(null);
  const [erro, setErro] = useState(null);
  const [carregando, setCarregando] = useState(false);

  useEffect(() => {
    api.get('/api/prestadores').then(setPrestadores).catch((e) => setErro(e.message));
    api.get('/api/clientes').then(setClientes).catch((e) => setErro(e.message));
  }, []);

  useEffect(() => {
    if (!prestadorId) {
      setServicos([]);
      return;
    }
    api
      .get(`/api/servicos?prestadorId=${prestadorId}`)
      .then(setServicos)
      .catch((e) => setErro(e.message));
  }, [prestadorId]);

  const servicoSelecionado = servicos.find((s) => s.id === servicoId);

  async function handleSubmit(e) {
    e.preventDefault();
    setErro(null);
    setResultado(null);
    setCarregando(true);
    try {
      const inicioIso = new Date(inicio).toISOString();
      const fimIso = new Date(new Date(inicio).getTime() + servicoSelecionado.duracaoMinutos * 60000).toISOString();

      const resposta = await api.post('/api/agendamentos', {
        prestadorId,
        clienteId,
        servicoId,
        inicio: inicioIso,
        fim: fimIso,
      });
      setResultado(resposta);
    } catch (e) {
      setErro(e.message);
    } finally {
      setCarregando(false);
    }
  }

  return (
    <div>
      <h1>Novo agendamento</h1>

      <form onSubmit={handleSubmit} className="form">
        <label>
          Prestador
          <select
            value={prestadorId}
            onChange={(e) => {
              setPrestadorId(e.target.value);
              setServicoId('');
            }}
            required
          >
            <option value="">Selecione...</option>
            {prestadores.map((p) => (
              <option key={p.id} value={p.id}>
                {p.nome}
              </option>
            ))}
          </select>
        </label>

        <label>
          Serviço
          <select value={servicoId} onChange={(e) => setServicoId(e.target.value)} required disabled={!prestadorId}>
            <option value="">Selecione...</option>
            {servicos.map((s) => (
              <option key={s.id} value={s.id}>
                {s.nome} ({s.duracaoMinutos} min, R$ {s.preco})
              </option>
            ))}
          </select>
        </label>

        <label>
          Cliente
          <select value={clienteId} onChange={(e) => setClienteId(e.target.value)} required>
            <option value="">Selecione...</option>
            {clientes.map((c) => (
              <option key={c.id} value={c.id}>
                {c.nome}
              </option>
            ))}
          </select>
        </label>

        <label>
          Início
          <input type="datetime-local" value={inicio} onChange={(e) => setInicio(e.target.value)} required />
        </label>

        <button type="submit" disabled={carregando || !servicoSelecionado}>
          Agendar
        </button>
      </form>

      {erro && <p className="erro">{erro}</p>}

      {resultado && (
        <div className="aviso-sucesso">
          <p>
            Agendamento criado! Status: <strong>{resultado.agendamento.status}</strong>
          </p>
          {resultado.cobranca && (
            <div>
              <p>Sinal: R$ {resultado.agendamento.valorSinal}</p>
              <p>
                Copia e cola Pix: <code>{resultado.cobranca.copiaECola}</code>
              </p>
            </div>
          )}
          <button onClick={() => navigate(`/agendamentos/${resultado.agendamento.id}`)}>
            Gerenciar este agendamento
          </button>
        </div>
      )}
    </div>
  );
}
