import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider, useAuth } from './AuthContext.jsx';
import { api } from '../api/client';

vi.mock('../api/client', () => ({
  api: { get: vi.fn(), post: vi.fn() },
}));

function Consumidor() {
  const { prestador, login, logout } = useAuth();
  return (
    <div>
      <span data-testid="estado">
        {prestador === undefined ? 'carregando' : prestador === null ? 'deslogado' : prestador.nome}
      </span>
      <button onClick={() => login('prestador@exemplo.com', 'senha123')}>entrar</button>
      <button onClick={() => logout().catch(() => {})}>sair</button>
    </div>
  );
}

describe('AuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('comeca deslogado quando GET /api/auth/me falha (sem sessao)', async () => {
    api.get.mockRejectedValue(new Error('nao autenticado'));

    render(<AuthProvider><Consumidor /></AuthProvider>);

    await waitFor(() => expect(screen.getByTestId('estado')).toHaveTextContent('deslogado'));
  });

  it('login grava o prestador retornado pela API', async () => {
    api.get.mockRejectedValue(new Error('nao autenticado'));
    api.post.mockResolvedValue({ prestadorId: 'id-1', nome: 'Clinica Bem-Estar' });
    const usuario = userEvent.setup();

    render(<AuthProvider><Consumidor /></AuthProvider>);
    await waitFor(() => expect(screen.getByTestId('estado')).toHaveTextContent('deslogado'));

    await usuario.click(screen.getByText('entrar'));

    await waitFor(() => expect(screen.getByTestId('estado')).toHaveTextContent('Clinica Bem-Estar'));
    expect(api.post).toHaveBeenCalledWith('/api/auth/login', { email: 'prestador@exemplo.com', senha: 'senha123' });
  });

  it('logout limpa o prestador mesmo se a chamada ao backend falhar', async () => {
    api.get.mockResolvedValue({ prestadorId: 'id-1', nome: 'Clinica Bem-Estar' });
    api.post.mockRejectedValue(new Error('erro de rede'));
    const usuario = userEvent.setup();

    render(<AuthProvider><Consumidor /></AuthProvider>);
    await waitFor(() => expect(screen.getByTestId('estado')).toHaveTextContent('Clinica Bem-Estar'));

    await usuario.click(screen.getByText('sair'));

    await waitFor(() => expect(screen.getByTestId('estado')).toHaveTextContent('deslogado'));
  });
});
