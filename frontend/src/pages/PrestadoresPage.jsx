import { useEffect, useState } from 'react';
import { api } from '../api/client';

export default function PrestadoresPage() {
  const [prestadores, setPrestadores] = useState([]);
  const [form, setForm] = useState({ nome: '', telefone: '', documentoNumero: '', documentoTipo: 'CNPJ' });
  const [erro, setErro] = useState(null);
  const [carregando, setCarregando] = useState(false);

  async function carregar() {
    try {
      setPrestadores(await api.get('/api/prestadores'));
    } catch (e) {
      setErro(e.message);
    }
  }

  useEffect(() => {
    carregar();
  }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    setErro(null);
    setCarregando(true);
    try {
      await api.post('/api/prestadores', form);
      setForm({ nome: '', telefone: '', documentoNumero: '', documentoTipo: 'CNPJ' });
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setCarregando(false);
    }
  }

  return (
    <div>
      <h1>Prestadores</h1>

      <form onSubmit={handleSubmit} className="form">
        <label>
          Nome
          <input value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} required />
        </label>
        <label>
          Telefone
          <input value={form.telefone} onChange={(e) => setForm({ ...form, telefone: e.target.value })} required />
        </label>
        <label>
          Documento
          <input
            value={form.documentoNumero}
            onChange={(e) => setForm({ ...form, documentoNumero: e.target.value })}
            required
          />
        </label>
        <label>
          Tipo
          <select value={form.documentoTipo} onChange={(e) => setForm({ ...form, documentoTipo: e.target.value })}>
            <option value="CNPJ">CNPJ</option>
            <option value="CPF">CPF</option>
          </select>
        </label>
        <button type="submit" disabled={carregando}>
          Cadastrar
        </button>
      </form>

      {erro && <p className="erro">{erro}</p>}

      <table className="tabela">
        <thead>
          <tr>
            <th>Nome</th>
            <th>Telefone</th>
          </tr>
        </thead>
        <tbody>
          {prestadores.map((p) => (
            <tr key={p.id}>
              <td>{p.nome}</td>
              <td>{p.telefone}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
