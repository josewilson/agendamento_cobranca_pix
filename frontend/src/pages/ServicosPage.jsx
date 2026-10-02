import { useEffect, useState } from 'react';
import { useAuth } from '../auth/AuthContext';
import { api } from '../api/client';

export default function ServicosPage() {
  const { prestador } = useAuth();
  const [servicos, setServicos] = useState([]);
  const [form, setForm] = useState({ nome: '', duracaoMinutos: 30, preco: '', percentualSinal: 0 });
  const [erro, setErro] = useState(null);
  const [carregando, setCarregando] = useState(false);

  async function carregarServicos() {
    try {
      setServicos(await api.get(`/api/servicos?prestadorId=${prestador.prestadorId}`));
    } catch (e) {
      setErro(e.message);
    }
  }

  useEffect(() => {
    carregarServicos();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    setErro(null);
    setCarregando(true);
    try {
      await api.post('/api/servicos', {
        nome: form.nome,
        duracaoMinutos: Number(form.duracaoMinutos),
        preco: Number(form.preco),
        percentualSinal: Number(form.percentualSinal),
      });
      setForm({ nome: '', duracaoMinutos: 30, preco: '', percentualSinal: 0 });
      await carregarServicos();
    } catch (e) {
      setErro(e.message);
    } finally {
      setCarregando(false);
    }
  }

  return (
    <div>
      <h1>Meus serviços</h1>

      <div className="tela-split">
        <aside className="painel-form">
          <h2>Novo serviço</h2>
          <form onSubmit={handleSubmit} className="form">
            <label>
              Nome
              <input value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} required />
            </label>
            <label>
              Duração (min)
              <input
                type="number"
                min="1"
                value={form.duracaoMinutos}
                onChange={(e) => setForm({ ...form, duracaoMinutos: e.target.value })}
                required
              />
            </label>
            <label>
              Preço (R$)
              <input
                type="number"
                step="0.01"
                min="0"
                value={form.preco}
                onChange={(e) => setForm({ ...form, preco: e.target.value })}
                required
              />
            </label>
            <label>
              Sinal (%)
              <input
                type="number"
                step="1"
                min="0"
                max="100"
                value={form.percentualSinal}
                onChange={(e) => setForm({ ...form, percentualSinal: e.target.value })}
                required
              />
            </label>
            <button type="submit" disabled={carregando}>
              Cadastrar
            </button>
          </form>

          {erro && <p className="erro">{erro}</p>}
        </aside>

        <section className="painel-conteudo">
          <h2>Serviços cadastrados</h2>
          <table className="tabela">
            <thead>
              <tr>
                <th>Nome</th>
                <th>Duração</th>
                <th>Preço</th>
                <th>Sinal</th>
              </tr>
            </thead>
            <tbody>
              {servicos.map((s) => (
                <tr key={s.id}>
                  <td>{s.nome}</td>
                  <td>{s.duracaoMinutos} min</td>
                  <td>R$ {s.preco}</td>
                  <td>{s.percentualSinal}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      </div>
    </div>
  );
}
