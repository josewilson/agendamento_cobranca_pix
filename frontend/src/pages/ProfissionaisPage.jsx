import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';

const FORM_BLOQUEIO_VAZIO = { inicio: '', fim: '', motivo: '' };

export default function ProfissionaisPage() {
  const { prestador } = useAuth();
  const [profissionais, setProfissionais] = useState([]);
  const [nome, setNome] = useState('');
  const [profissionalId, setProfissionalId] = useState('');
  const [bloqueios, setBloqueios] = useState([]);
  const [formBloqueio, setFormBloqueio] = useState(FORM_BLOQUEIO_VAZIO);
  const [erro, setErro] = useState(null);
  const [carregando, setCarregando] = useState(false);

  async function carregarProfissionais() {
    try {
      setProfissionais(await api.get(`/api/profissionais?prestadorId=${prestador.prestadorId}`));
    } catch (e) {
      setErro(e.message);
    }
  }

  async function carregarBloqueios(id) {
    try {
      setBloqueios(await api.get(`/api/profissionais/${id}/bloqueios`));
    } catch (e) {
      setErro(e.message);
    }
  }

  useEffect(() => {
    carregarProfissionais();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (profissionalId) {
      carregarBloqueios(profissionalId);
    } else {
      setBloqueios([]);
    }
  }, [profissionalId]);

  // Executa uma chamada de escrita e recarrega; mostra o erro do backend (ex.: "ultimo profissional").
  async function executar(acao) {
    setErro(null);
    setCarregando(true);
    try {
      await acao();
    } catch (e) {
      setErro(e.message);
    } finally {
      setCarregando(false);
    }
  }

  function cadastrar(e) {
    e.preventDefault();
    return executar(async () => {
      await api.post('/api/profissionais', { nome });
      setNome('');
      await carregarProfissionais();
    });
  }

  function remover(id) {
    return executar(async () => {
      await api.delete(`/api/profissionais/${id}`);
      if (id === profissionalId) {
        setProfissionalId('');
      }
      await carregarProfissionais();
    });
  }

  function bloquear(e) {
    e.preventDefault();
    return executar(async () => {
      await api.post(`/api/profissionais/${profissionalId}/bloqueios`, {
        inicio: new Date(formBloqueio.inicio).toISOString(),
        fim: new Date(formBloqueio.fim).toISOString(),
        motivo: formBloqueio.motivo || null,
      });
      setFormBloqueio(FORM_BLOQUEIO_VAZIO);
      await carregarBloqueios(profissionalId);
    });
  }

  function removerBloqueio(id) {
    return executar(async () => {
      await api.delete(`/api/profissionais/${profissionalId}/bloqueios/${id}`);
      await carregarBloqueios(profissionalId);
    });
  }

  return (
    <div>
      <h1>Profissionais</h1>

      <div className="tela-split">
        <aside className="painel-form">
          <h2>Novo profissional</h2>
          <form onSubmit={cadastrar} className="form">
            <label>
              Nome
              <input value={nome} onChange={(e) => setNome(e.target.value)} required />
            </label>
            <button type="submit" disabled={carregando}>
              Cadastrar
            </button>
          </form>
        </aside>

        <section className="painel-conteudo">
          <h2>Profissionais cadastrados</h2>
          <table className="tabela">
            <thead>
              <tr>
                <th>Nome</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {profissionais.map((p) => (
                <tr key={p.id}>
                  <td>{p.nome}</td>
                  <td>
                    <button onClick={() => remover(p.id)} disabled={carregando} aria-label={`Remover ${p.nome}`}>
                      Remover
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      </div>

      {erro && <p className="erro">{erro}</p>}

      <h2>Horários bloqueados</h2>
      <label className="seletor-prestador">
        Profissional
        <select value={profissionalId} onChange={(e) => setProfissionalId(e.target.value)}>
          <option value="">Selecione...</option>
          {profissionais.map((p) => (
            <option key={p.id} value={p.id}>
              {p.nome}
            </option>
          ))}
        </select>
      </label>

      {profissionalId && (
        <div className="tela-split">
          <aside className="painel-form">
            <form onSubmit={bloquear} className="form">
              <label>
                Início do bloqueio
                <input
                  type="datetime-local"
                  value={formBloqueio.inicio}
                  onChange={(e) => setFormBloqueio({ ...formBloqueio, inicio: e.target.value })}
                  required
                />
              </label>
              <label>
                Fim do bloqueio
                <input
                  type="datetime-local"
                  value={formBloqueio.fim}
                  onChange={(e) => setFormBloqueio({ ...formBloqueio, fim: e.target.value })}
                  required
                />
              </label>
              <label>
                Motivo
                <input
                  value={formBloqueio.motivo}
                  onChange={(e) => setFormBloqueio({ ...formBloqueio, motivo: e.target.value })}
                />
              </label>
              <button type="submit" disabled={carregando}>
                Bloquear horário
              </button>
            </form>
          </aside>

          <section className="painel-conteudo">
            {bloqueios.length === 0 ? (
              <p>Nenhum horário bloqueado.</p>
            ) : (
              <table className="tabela">
                <thead>
                  <tr>
                    <th>Início</th>
                    <th>Fim</th>
                    <th>Motivo</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {bloqueios.map((b) => (
                    <tr key={b.id}>
                      <td>{new Date(b.inicio).toLocaleString('pt-BR')}</td>
                      <td>{new Date(b.fim).toLocaleString('pt-BR')}</td>
                      <td>{b.motivo ?? '—'}</td>
                      <td>
                        <button onClick={() => removerBloqueio(b.id)} disabled={carregando}>
                          Desbloquear
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </section>
        </div>
      )}
    </div>
  );
}
