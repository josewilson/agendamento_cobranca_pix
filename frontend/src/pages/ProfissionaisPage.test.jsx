import { fireEvent, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ProfissionaisPage from './ProfissionaisPage.jsx';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';

vi.mock('../api/client', () => ({ api: { get: vi.fn(), post: vi.fn(), delete: vi.fn() } }));
vi.mock('../auth/AuthContext', () => ({ useAuth: vi.fn() }));

const PROFISSIONAIS = [
  { id: 'pf-uuid-1', nome: 'Ana' },
  { id: 'pf-uuid-2', nome: 'Beto' },
];
const BLOQUEIO = { id: 'bl-uuid-1', inicio: '2030-01-10T15:00:00Z', fim: '2030-01-10T16:00:00Z', motivo: 'Almoco' };

function mockApi({ bloqueios = [BLOQUEIO] } = {}) {
  api.get.mockImplementation((url) => {
    if (url === '/api/profissionais?prestadorId=p-1') return Promise.resolve(PROFISSIONAIS);
    if (url.endsWith('/bloqueios')) return Promise.resolve(bloqueios);
    return Promise.reject(new Error(`url inesperada: ${url}`));
  });
}

describe('ProfissionaisPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuth.mockReturnValue({ prestador: { prestadorId: 'p-1', nome: 'Clinica' } });
    mockApi();
  });

  it('lista os profissionais do estabelecimento, sem mostrar ids', async () => {
    render(<ProfissionaisPage />);

    expect(await screen.findByRole('cell', { name: 'Ana' })).toBeInTheDocument();
    expect(screen.getByRole('cell', { name: 'Beto' })).toBeInTheDocument();
    expect(screen.queryByText(/pf-uuid/)).not.toBeInTheDocument();
  });

  it('cadastra um profissional e recarrega a lista', async () => {
    api.post.mockResolvedValue({});
    const usuario = userEvent.setup();
    render(<ProfissionaisPage />);
    await screen.findByRole('cell', { name: 'Ana' });

    await usuario.type(screen.getByLabelText('Nome'), 'Carla');
    await usuario.click(screen.getByRole('button', { name: 'Cadastrar' }));

    expect(api.post).toHaveBeenCalledWith('/api/profissionais', { nome: 'Carla' });
    expect(api.get).toHaveBeenCalledTimes(2);
  });

  it('remove um profissional e mostra o erro do backend (ex.: ultimo profissional)', async () => {
    api.delete.mockRejectedValue(new Error('O estabelecimento precisa ter pelo menos um profissional'));
    const usuario = userEvent.setup();
    render(<ProfissionaisPage />);

    await usuario.click(await screen.findByRole('button', { name: 'Remover Ana' }));

    expect(api.delete).toHaveBeenCalledWith('/api/profissionais/pf-uuid-1');
    expect(await screen.findByText('O estabelecimento precisa ter pelo menos um profissional')).toBeInTheDocument();
  });

  it('lista os bloqueios do profissional escolhido', async () => {
    const usuario = userEvent.setup();
    render(<ProfissionaisPage />);
    await screen.findByRole('cell', { name: 'Ana' });

    await usuario.selectOptions(screen.getByLabelText('Profissional'), 'Ana');

    expect(await screen.findByText('Almoco')).toBeInTheDocument();
    expect(api.get).toHaveBeenCalledWith('/api/profissionais/pf-uuid-1/bloqueios');
    expect(screen.queryByText(/bl-uuid/)).not.toBeInTheDocument();
  });

  it('bloqueia um horario enviando inicio/fim em ISO', async () => {
    api.post.mockResolvedValue({});
    const usuario = userEvent.setup();
    render(<ProfissionaisPage />);
    await screen.findByRole('cell', { name: 'Ana' });
    await usuario.selectOptions(screen.getByLabelText('Profissional'), 'Beto');

    fireEvent.change(await screen.findByLabelText('Início do bloqueio'), { target: { value: '2030-01-10T12:00' } });
    fireEvent.change(screen.getByLabelText('Fim do bloqueio'), { target: { value: '2030-01-10T13:00' } });
    await usuario.type(screen.getByLabelText('Motivo'), 'Folga');
    await usuario.click(screen.getByRole('button', { name: 'Bloquear horário' }));

    expect(api.post).toHaveBeenCalledWith('/api/profissionais/pf-uuid-2/bloqueios', {
      inicio: new Date('2030-01-10T12:00').toISOString(),
      fim: new Date('2030-01-10T13:00').toISOString(),
      motivo: 'Folga',
    });
  });

  it('desbloqueia um horario', async () => {
    api.delete.mockResolvedValue(null);
    const usuario = userEvent.setup();
    render(<ProfissionaisPage />);
    await screen.findByRole('cell', { name: 'Ana' });
    await usuario.selectOptions(screen.getByLabelText('Profissional'), 'Ana');

    await usuario.click(await screen.findByRole('button', { name: 'Desbloquear' }));

    expect(api.delete).toHaveBeenCalledWith('/api/profissionais/pf-uuid-1/bloqueios/bl-uuid-1');
  });
});
