import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';

export default function NovoAgendamentoPage() {
  const navigate = useNavigate();
  const { cliente } = useAuth();
  const [prestadores, setPrestadores] = useState([]);
  const [servicos, setServicos] = useState([]);
  const [profissionais, setProfissionais] = useState([]);
  const [prestadorId, setPrestadorId] = useState('');
  const [servicoId, setServicoId] = useState('');
  const [profissionalId, setProfissionalId] = useState('');
  const [inicio, setInicio] = useState('');
  const [resultado, setResultado] = useState(null);
  const [erro, setErro] = useState(null);
  const [carregando, setCarregando] = useState(false);

  useEffect(() => {
    api.get('/api/prestadores').then(setPrestadores).catch((e) => setErro(e.message));
  }, []);

  useEffect(() => {
    if (!prestadorId) {
      setServicos([]);
      setProfissionais([]);
      return;
    }
    api
      .get(`/api/servicos?prestadorId=${prestadorId}`)
      .then(setServicos)
      .catch((e) => setErro(e.message));
    api
      .get(`/api/profissionais?prestadorId=${prestadorId}`)
      .then((lista) => {
        setProfissionais(lista);
        // um unico profissional: ja vem escolhido
        setProfissionalId(lista.length === 1 ? lista[0].id : '');
      })
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
        servicoId,
        profissionalId,
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
      {cliente && <p>Agendando como <strong>{cliente.nome}</strong>.</p>}

      <form onSubmit={handleSubmit} className="form">
        <label>
          Prestador
          <select
            value={prestadorId}
            onChange={(e) => {
              setPrestadorId(e.target.value);
              setServicoId('');
              setProfissionalId('');
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
          Profissional
          <select
            value={profissionalId}
            onChange={(e) => setProfissionalId(e.target.value)}
            required
            disabled={!prestadorId}
          >
            <option value="">Selecione...</option>
            {profissionais.map((p) => (
              <option key={p.id} value={p.id}>
                {p.nome}
              </option>
            ))}
          </select>
        </label>

        <label>
          Início
          <input type="datetime-local" value={inicio} onChange={(e) => setInicio(e.target.value)} required />
        </label>

        <button type="submit" disabled={carregando || !servicoSelecionado || !profissionalId}>
          Agendar
        </button>
      </form>

      {erro && <p className="erro">{erro}</p>}

      {resultado && (
        <div className="aviso-sucesso">
          <p>
            Agendamento criado! Status:{' '}
            <span className="status-badge" data-status={resultado.agendamento.status}>
              {resultado.agendamento.status}
            </span>
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
