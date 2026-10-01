import { createContext, useContext, useEffect, useState } from 'react';
import { api } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // undefined = verificando sessao, null = deslogado, objeto = logado ({ prestadorId, nome })
  const [prestador, setPrestador] = useState(undefined);

  async function carregar() {
    try {
      setPrestador(await api.get('/api/auth/me'));
    } catch {
      setPrestador(null);
    }
  }

  useEffect(() => {
    carregar();
  }, []);

  async function login(email, senha) {
    const resultado = await api.post('/api/auth/login', { email, senha });
    setPrestador(resultado);
  }

  async function logout() {
    try {
      await api.post('/api/auth/logout', {});
    } finally {
      setPrestador(null);
    }
  }

  return <AuthContext.Provider value={{ prestador, login, logout }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
