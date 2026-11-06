import { createContext, useContext, useEffect, useState } from 'react';
import { api } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // undefined = verificando sessao, null = deslogado, objeto = logado
  // ({ tipo: 'PRESTADOR' | 'CLIENTE', prestadorId | clienteId, nome })
  const [sessao, setSessao] = useState(undefined);

  async function carregar() {
    try {
      setSessao(await api.get('/api/auth/me'));
    } catch {
      setSessao(null);
    }
  }

  useEffect(() => {
    carregar();
  }, []);

  // tipo: 'PRESTADOR' (padrao) ou 'CLIENTE' — cada um tem o proprio endpoint de login
  async function login(email, senha, tipo = 'PRESTADOR') {
    const caminho = tipo === 'CLIENTE' ? '/api/auth/cliente/login' : '/api/auth/login';
    setSessao(await api.post(caminho, { email, senha }));
  }

  async function logout() {
    try {
      await api.post('/api/auth/logout', {});
    } finally {
      setSessao(null);
    }
  }

  // Cada tela so olha o papel que lhe interessa: prestador/cliente ficam undefined enquanto a sessao
  // carrega, null se deslogado OU logado com o outro papel. (Resposta sem "tipo" = prestador.)
  const ehCliente = sessao?.tipo === 'CLIENTE';
  const prestador = sessao === undefined ? undefined : sessao && !ehCliente ? sessao : null;
  const cliente = sessao === undefined ? undefined : ehCliente ? sessao : null;

  return <AuthContext.Provider value={{ sessao, prestador, cliente, login, logout }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
