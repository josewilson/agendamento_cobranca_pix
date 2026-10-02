import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';

export default function PrestadoresPage() {
  const [prestadores, setPrestadores] = useState([]);
  const [form, setForm] = useState({
    nome: '',
    telefone: '',
    email: '',
    senha: '',
    documentoNumero: '',
    documentoTipo: 'CNPJ',
  });
  const [cadastrado, setCadastrado] = useState(false);
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
    setCadastrado(false);
    setCarregando(true);
    try {
      await api.post('/api/prestadores', form);
      setForm({ nome: '', telefone: '', email: '', senha: '', documentoNumero: '', documentoTipo: 'CNPJ' });
      setCadastrado(true);
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

      <form onSubmit={handleSubmit} className="form" autoComplete="off">
        <label>
          Nome
          <input
            value={form.nome}
            onChange={(e) => setForm({ ...form, nome: e.target.value })}
            autoComplete="off"
            required
          />
        </label>
        <label>
          Telefone
          <input
            value={form.telefone}
            onChange={(e) => setForm({ ...form, telefone: e.target.value })}
            autoComplete="off"
            required
          />
        </label>
        <label>
          Email (login)
          <input
            type="email"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            autoComplete="off"
            required
          />
        </label>
        <label>
          Senha
          <input
            type="password"
            value={form.senha}
            onChange={(e) => setForm({ ...form, senha: e.target.value })}
            autoComplete="new-password"
            minLength={6}
            required
          />
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

      {cadastrado && (
        <div className="aviso-sucesso">
          <p>
            Prestador cadastrado! Agora é só <Link to="/login">entrar</Link> com o email e a senha escolhidos.
          </p>
        </div>
      )}

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
