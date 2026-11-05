import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ClientesPage from './ClientesPage.jsx';
import { api } from '../api/client';

vi.mock('../api/client', () => ({ api: { get: vi.fn(), post: vi.fn() } }));

describe('ClientesPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('lista os clientes por nome/email/telefone, sem mostrar o id', async () => {
    api.get.mockResolvedValue([
      { id: '11111111-aaaa-bbbb-cccc-000000000001', nome: 'Maria', email: 'm@x.com', telefone: '11999990000' },
    ]);

    render(<ClientesPage />);

    expect(await screen.findByText('Maria')).toBeInTheDocument();
    expect(screen.getByText('m@x.com')).toBeInTheDocument();
    expect(screen.queryByText(/11111111-aaaa/)).not.toBeInTheDocument();
  });

  it('cadastra, recarrega a lista e avisa o sucesso', async () => {
    api.get.mockResolvedValue([]);
    api.post.mockResolvedValue({});
    const usuario = userEvent.setup();
    render(<ClientesPage />);

    await usuario.type(screen.getByLabelText('Nome'), 'Joao');
    await usuario.type(screen.getByLabelText('Email'), 'j@x.com');
    await usuario.type(screen.getByLabelText('Telefone'), '11988887777');
    await usuario.type(screen.getByLabelText('Documento'), '12345678901');
    await usuario.click(screen.getByRole('button', { name: 'Cadastrar' }));

    expect(api.post).toHaveBeenCalledWith('/api/clientes', {
      nome: 'Joao',
      email: 'j@x.com',
      telefone: '11988887777',
      documentoNumero: '12345678901',
      documentoTipo: 'CPF',
    });
    expect(await screen.findByText(/Cliente cadastrado!/)).toBeInTheDocument();
    expect(api.get).toHaveBeenCalledTimes(2);
  });

  it('mostra o erro do backend quando o cadastro falha', async () => {
    api.get.mockResolvedValue([]);
    api.post.mockRejectedValue(new Error('Documento ja cadastrado'));
    const usuario = userEvent.setup();
    render(<ClientesPage />);

    await usuario.type(screen.getByLabelText('Nome'), 'Joao');
    await usuario.type(screen.getByLabelText('Email'), 'j@x.com');
    await usuario.type(screen.getByLabelText('Telefone'), '11988887777');
    await usuario.type(screen.getByLabelText('Documento'), '12345678901');
    await usuario.click(screen.getByRole('button', { name: 'Cadastrar' }));

    expect(await screen.findByText('Documento ja cadastrado')).toBeInTheDocument();
    expect(screen.queryByText(/Cliente cadastrado!/)).not.toBeInTheDocument();
  });
});
