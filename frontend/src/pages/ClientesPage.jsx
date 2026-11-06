import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';

export default function ClientesPage() {
  const { prestador } = useAuth();
  const [clientes, setClientes] = useState([]);
  const [form, setForm] = useState({ nome: '', email: '', telefone: '', documentoNumero: '', documentoTipo: 'CPF', canalPreferido: 'AMBOS', senha: '' });
  const [sucesso, setSucesso] = useState(false);
  const [erro, setErro] = useState(null);
  const [carregando, setCarregando] = useState(false);

  async function carregar() {
    try {
      setClientes(await api.get('/api/clientes'));
    } catch (e) {
      setErro(e.message);
    }
  }

  useEffect(() => {
    if (prestador) {
      carregar();
    }
  }, [prestador]);

  async function handleSubmit(e) {
    e.preventDefault();
    setErro(null);
    setSucesso(false);
    setCarregando(true);
    try {
      await api.post('/api/clientes', form);
      setForm({ nome: '', email: '', telefone: '', documentoNumero: '', documentoTipo: 'CPF', canalPreferido: 'AMBOS', senha: '' });
      setSucesso(true);
      if (prestador) {
        await carregar();
      }
    } catch (e) {
      setErro(e.message);
    } finally {
      setCarregando(false);
    }
  }

  return (
    <div>
      <h1>Clientes</h1>

      <div className="tela-split">
        <aside className="painel-form">
          <h2>Novo cliente</h2>
          <form onSubmit={handleSubmit} className="form">
            <label>
              Nome
              <input value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} required />
            </label>
            <label>
              Email
              <input
                type="email"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                required
              />
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
                <option value="CPF">CPF</option>
                <option value="CNPJ">CNPJ</option>
              </select>
            </label>
            <label>
              Senha
              <input
                type="password"
                value={form.senha}
                onChange={(e) => setForm({ ...form, senha: e.target.value })}
                autoComplete="new-password"
                minLength={8}
                required
              />
            </label>
            <label>
              Receber avisos por
              <select
                value={form.canalPreferido}
                onChange={(e) => setForm({ ...form, canalPreferido: e.target.value })}
              >
                <option value="AMBOS">Email e WhatsApp</option>
                <option value="EMAIL">Só email</option>
                <option value="WHATSAPP">Só WhatsApp</option>
              </select>
            </label>
            <button type="submit" disabled={carregando}>
              Cadastrar
            </button>
          </form>

          {erro && <p className="erro">{erro}</p>}
          {sucesso && (
            <div className="aviso-sucesso">
              Cliente cadastrado! Agora é só <Link to="/login">entrar</Link> como cliente para agendar.
            </div>
          )}
        </aside>

        {prestador && (
        <section className="painel-conteudo">
          <h2>Clientes cadastrados</h2>
          <table className="tabela">
            <thead>
              <tr>
                <th>Nome</th>
                <th>Email</th>
                <th>Telefone</th>
              </tr>
            </thead>
            <tbody>
              {clientes.map((c) => (
                <tr key={c.id}>
                  <td>{c.nome}</td>
                  <td>{c.email}</td>
                  <td>{c.telefone}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
        )}
      </div>
    </div>
  );
}
