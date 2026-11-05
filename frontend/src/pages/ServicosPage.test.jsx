import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ServicosPage from './ServicosPage.jsx';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';

vi.mock('../api/client', () => ({ api: { get: vi.fn(), post: vi.fn() } }));
vi.mock('../auth/AuthContext', () => ({ useAuth: vi.fn() }));

describe('ServicosPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuth.mockReturnValue({ prestador: { prestadorId: 'p-1', nome: 'Clinica' } });
  });

  it('lista os servicos do prestador logado, sem mostrar o id', async () => {
    api.get.mockResolvedValue([
      { id: 's-uuid-1', nome: 'Corte', duracaoMinutos: 30, preco: '50.00', percentualSinal: 20 },
    ]);
    render(<ServicosPage />);

    expect(await screen.findByText('Corte')).toBeInTheDocument();
    expect(screen.getByText('30 min')).toBeInTheDocument();
    expect(screen.getByText('R$ 50.00')).toBeInTheDocument();
    expect(screen.getByText('20%')).toBeInTheDocument();
    expect(api.get).toHaveBeenCalledWith('/api/servicos?prestadorId=p-1');
    expect(screen.queryByText(/s-uuid-1/)).not.toBeInTheDocument();
  });

  it('cadastra convertendo para numero e sem enviar prestadorId', async () => {
    api.get.mockResolvedValue([]);
    api.post.mockResolvedValue({});
    const usuario = userEvent.setup();
    render(<ServicosPage />);

    await usuario.type(screen.getByLabelText('Nome'), 'Barba');
    await usuario.type(screen.getByLabelText('Preço (R$)'), '40');
    await usuario.click(screen.getByRole('button', { name: 'Cadastrar' }));

    expect(api.post).toHaveBeenCalledWith('/api/servicos', {
      nome: 'Barba',
      duracaoMinutos: 30,
      preco: 40,
      percentualSinal: 0,
    });
    expect(api.get).toHaveBeenCalledTimes(2);
  });

  it('mostra o erro do backend quando o cadastro falha', async () => {
    api.get.mockResolvedValue([]);
    api.post.mockRejectedValue(new Error('Nao autenticado'));
    const usuario = userEvent.setup();
    render(<ServicosPage />);

    await usuario.type(screen.getByLabelText('Nome'), 'Barba');
    await usuario.type(screen.getByLabelText('Preço (R$)'), '40');
    await usuario.click(screen.getByRole('button', { name: 'Cadastrar' }));

    expect(await screen.findByText('Nao autenticado')).toBeInTheDocument();
  });
});
