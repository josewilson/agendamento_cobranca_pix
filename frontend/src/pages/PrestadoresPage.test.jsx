import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import PrestadoresPage from './PrestadoresPage.jsx';
import { api } from '../api/client';

vi.mock('../api/client', () => ({ api: { get: vi.fn(), post: vi.fn() } }));

function renderPagina() {
  return render(
    <MemoryRouter>
      <PrestadoresPage />
    </MemoryRouter>,
  );
}

async function preencher(usuario) {
  await usuario.type(screen.getByLabelText('Nome'), 'Barbearia Z');
  await usuario.type(screen.getByLabelText('Telefone'), '11977776666');
  await usuario.type(screen.getByLabelText('Email (login)'), 'z@x.com');
  await usuario.type(screen.getByLabelText('Senha'), 'senha1234');
  await usuario.type(screen.getByLabelText('Documento'), '12345678000199');
}

describe('PrestadoresPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('lista nome e telefone, sem mostrar o id', async () => {
    api.get.mockResolvedValue([{ id: '22222222-aaaa-bbbb-cccc-000000000001', nome: 'Clinica A', telefone: '1130001000' }]);
    renderPagina();

    expect(await screen.findByText('Clinica A')).toBeInTheDocument();
    expect(screen.getByText('1130001000')).toBeInTheDocument();
    expect(screen.queryByText(/22222222-aaaa/)).not.toBeInTheDocument();
  });

  it('cadastra com login/senha e mostra o convite para entrar', async () => {
    api.get.mockResolvedValue([]);
    api.post.mockResolvedValue({});
    const usuario = userEvent.setup();
    renderPagina();

    await preencher(usuario);
    await usuario.click(screen.getByRole('button', { name: 'Cadastrar' }));

    expect(api.post).toHaveBeenCalledWith('/api/prestadores', {
      nome: 'Barbearia Z',
      telefone: '11977776666',
      email: 'z@x.com',
      senha: 'senha1234',
      documentoNumero: '12345678000199',
      documentoTipo: 'CNPJ',
    });
    expect(await screen.findByText(/Prestador cadastrado!/)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'entrar' })).toHaveAttribute('href', '/login');
  });

  it('mostra o erro do backend (ex.: senha fraca) sem o convite', async () => {
    api.get.mockResolvedValue([]);
    api.post.mockRejectedValue(new Error('Senha fraca'));
    const usuario = userEvent.setup();
    renderPagina();

    await preencher(usuario);
    await usuario.click(screen.getByRole('button', { name: 'Cadastrar' }));

    expect(await screen.findByText('Senha fraca')).toBeInTheDocument();
    expect(screen.queryByText(/Prestador cadastrado!/)).not.toBeInTheDocument();
  });
});
