import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ClientesPage from './ClientesPage.jsx';
import { MemoryRouter } from 'react-router-dom';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';

vi.mock('../api/client', () => ({ api: { get: vi.fn(), post: vi.fn() } }));
vi.mock('../auth/AuthContext', () => ({ useAuth: vi.fn() }));

const renderPagina = () => render(<MemoryRouter><ClientesPage /></MemoryRouter>);

describe('ClientesPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuth.mockReturnValue({ prestador: { prestadorId: 'p-1', nome: 'Clinica' }, cliente: null });
  });

  it('lista os clientes por nome/email/telefone, sem mostrar o id', async () => {
    api.get.mockResolvedValue([
      { id: '11111111-aaaa-bbbb-cccc-000000000001', nome: 'Maria', email: 'm@x.com', telefone: '11999990000' },
    ]);

    renderPagina();

    expect(await screen.findByText('Maria')).toBeInTheDocument();
    expect(screen.getByText('m@x.com')).toBeInTheDocument();
    expect(screen.queryByText(/11111111-aaaa/)).not.toBeInTheDocument();
  });

  it('cadastra, recarrega a lista e avisa o sucesso', async () => {
    api.get.mockResolvedValue([]);
    api.post.mockResolvedValue({});
    const usuario = userEvent.setup();
    renderPagina();

    await usuario.type(screen.getByLabelText('Nome'), 'Joao');
    await usuario.type(screen.getByLabelText('Email'), 'j@x.com');
    await usuario.type(screen.getByLabelText('Telefone'), '11988887777');
    await usuario.type(screen.getByLabelText('Documento'), '12345678901');
    await usuario.type(screen.getByLabelText('Senha'), 'senha1234');
    await usuario.selectOptions(screen.getByLabelText('Receber avisos por'), 'Só WhatsApp');
    await usuario.click(screen.getByRole('button', { name: 'Cadastrar' }));

    expect(api.post).toHaveBeenCalledWith('/api/clientes', {
      nome: 'Joao',
      email: 'j@x.com',
      telefone: '11988887777',
      documentoNumero: '12345678901',
      documentoTipo: 'CPF',
      canalPreferido: 'WHATSAPP',
      senha: 'senha1234',
    });
    expect(await screen.findByText(/Cliente cadastrado!/)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'entrar' })).toHaveAttribute('href', '/login');
    expect(api.get).toHaveBeenCalledTimes(2);
  });

  it('sem prestador logado nao busca nem mostra a lista de clientes (so o cadastro)', async () => {
    useAuth.mockReturnValue({ prestador: null, cliente: null });
    renderPagina();

    expect(screen.getByRole('button', { name: 'Cadastrar' })).toBeInTheDocument();
    expect(screen.queryByText('Clientes cadastrados')).not.toBeInTheDocument();
    expect(api.get).not.toHaveBeenCalled();
  });

  it('mostra o erro do backend quando o cadastro falha', async () => {
    api.get.mockResolvedValue([]);
    api.post.mockRejectedValue(new Error('Documento ja cadastrado'));
    const usuario = userEvent.setup();
    renderPagina();

    await usuario.type(screen.getByLabelText('Nome'), 'Joao');
    await usuario.type(screen.getByLabelText('Email'), 'j@x.com');
    await usuario.type(screen.getByLabelText('Telefone'), '11988887777');
    await usuario.type(screen.getByLabelText('Documento'), '12345678901');
    await usuario.type(screen.getByLabelText('Senha'), 'senha1234');
    await usuario.click(screen.getByRole('button', { name: 'Cadastrar' }));

    expect(await screen.findByText('Documento ja cadastrado')).toBeInTheDocument();
    expect(screen.queryByText(/Cliente cadastrado!/)).not.toBeInTheDocument();
  });
});
