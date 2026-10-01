import { useState } from 'react';
import { api } from '../api/client';

export default function ClientesPage() {
  const [form, setForm] = useState({ nome: '', email: '', telefone: '', documentoNumero: '', documentoTipo: 'CPF' });
  const [clienteCriado, setClienteCriado] = useState(null);
  const [erro, setErro] = useState(null);
  const [carregando, setCarregando] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setErro(null);
    setCarregando(true);
    try {
      const cliente = await api.post('/api/clientes', form);
      setClienteCriado(cliente);
      setForm({ nome: '', email: '', telefone: '', documentoNumero: '', documentoTipo: 'CPF' });
    } catch (e) {
      setErro(e.message);
    } finally {
      setCarregando(false);
    }
  }

  return (
    <div>
      <h1>Clientes</h1>

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
        <button type="submit" disabled={carregando}>
          Cadastrar
        </button>
      </form>

      {erro && <p className="erro">{erro}</p>}

      {clienteCriado && (
        <div className="aviso-sucesso">
          Cliente cadastrado! Copie o id para usar no agendamento: <code>{clienteCriado.id}</code>
        </div>
      )}
    </div>
  );
}
