import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import AgendaPage from './AgendaPage.jsx';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';

vi.mock('../api/client', () => ({ api: { get: vi.fn() } }));
vi.mock('../auth/AuthContext', () => ({ useAuth: vi.fn() }));

const PRESTADOR_LOGADO = { prestadorId: 'p-1', nome: 'Clinica A' };
const PRESTADORES = [
  { id: 'p-1', nome: 'Clinica A' },
  { id: 'p-2', nome: 'Barbearia B' },
];
const CLIENTES = [{ id: 'c-1', nome: 'Maria' }];
const SERVICOS = [{ id: 's-1', nome: 'Corte' }];
const PROFISSIONAIS = [{ id: 'pf-1', nome: 'Dra. Ana' }];
const AGENDAMENTO = {
  id: 'ag-uuid-1',
  clienteId: 'c-1',
  servicoId: 's-1',
  profissionalId: 'pf-1',
  inicio: '2030-01-10T13:00:00Z',
  valorServico: '80.00',
  status: 'PENDENTE_PAGAMENTO',
};

function mockApi({ agendamentos = [AGENDAMENTO] } = {}) {
  api.get.mockImplementation((url) => {
    if (url === '/api/prestadores') return Promise.resolve(PRESTADORES);
    if (url.startsWith('/api/agendamentos')) return Promise.resolve(agendamentos);
    if (url === '/api/clientes') return Promise.resolve(CLIENTES);
    if (url.startsWith('/api/profissionais')) return Promise.resolve(PROFISSIONAIS);
    if (url.startsWith('/api/servicos')) return Promise.resolve(SERVICOS);
    return Promise.reject(new Error(`url inesperada: ${url}`));
  });
}

function renderPagina() {
  return render(
    <MemoryRouter initialEntries={['/agenda']}>
      <Routes>
        <Route path="/agenda" element={<AgendaPage />} />
        <Route path="/agendamentos/:id" element={<p>detalhe do agendamento</p>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('AgendaPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuth.mockReturnValue({ prestador: PRESTADOR_LOGADO });
  });

  it('abre na agenda do prestador logado, com nomes de cliente/servico e sem id', async () => {
    mockApi();
    renderPagina();

    expect(await screen.findByText('Maria')).toBeInTheDocument();
    expect(screen.getByText('Corte')).toBeInTheDocument();
    expect(screen.getByText('Dra. Ana')).toBeInTheDocument();
    expect(screen.getByText('R$ 80.00')).toBeInTheDocument();
    expect(screen.getByText('PENDENTE_PAGAMENTO', { selector: '.status-badge' })).toBeInTheDocument();
    expect(api.get).toHaveBeenCalledWith('/api/agendamentos?prestadorId=p-1');
    expect(screen.queryByText(/ag-uuid-1/)).not.toBeInTheDocument();
  });

  it('mostra o resumo (total e aguardando pagamento)', async () => {
    mockApi({ agendamentos: [AGENDAMENTO, { ...AGENDAMENTO, id: 'ag-2', status: 'CONFIRMADO' }] });
    renderPagina();

    const total = (await screen.findByText('No total')).parentElement;
    expect(total).toHaveTextContent('2');
    expect(screen.getByText('Aguardando pagamento').parentElement).toHaveTextContent('1');
  });

  it('avisa quando nao ha agendamentos', async () => {
    mockApi({ agendamentos: [] });
    renderPagina();

    expect(await screen.findByText('Nenhum agendamento ainda.')).toBeInTheDocument();
  });

  it('troca para a agenda de outro prestador pelo seletor', async () => {
    mockApi();
    const usuario = userEvent.setup();
    renderPagina();
    await screen.findByText('Maria');

    await usuario.selectOptions(screen.getByLabelText('Prestador'), 'Barbearia B');

    expect(api.get).toHaveBeenCalledWith('/api/agendamentos?prestadorId=p-2');
  });

  it('na agenda de outro prestador mostra so horario/status, sem detalhes nem botao Abrir', async () => {
    const ocupacao = { inicio: AGENDAMENTO.inicio, status: 'CONFIRMADO' };
    api.get.mockImplementation((url) => {
      if (url === '/api/prestadores') return Promise.resolve(PRESTADORES);
      if (url === '/api/agendamentos?prestadorId=p-2') return Promise.resolve([ocupacao]);
      if (url.startsWith('/api/agendamentos')) return Promise.resolve([AGENDAMENTO]);
      if (url === '/api/clientes') return Promise.resolve(CLIENTES);
      return Promise.resolve(SERVICOS);
    });
    const usuario = userEvent.setup();
    renderPagina();
    await screen.findByText('Maria');

    await usuario.selectOptions(screen.getByLabelText('Prestador'), 'Barbearia B');

    expect(await screen.findByText('CONFIRMADO', { selector: '.status-badge' })).toBeInTheDocument();
    expect(screen.queryByText('Maria')).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Abrir' })).not.toBeInTheDocument();
  });

  it('abre o detalhe do agendamento ao clicar em Abrir', async () => {
    mockApi();
    const usuario = userEvent.setup();
    renderPagina();

    await usuario.click(await screen.findByRole('button', { name: 'Abrir' }));

    expect(await screen.findByText('detalhe do agendamento')).toBeInTheDocument();
  });

  it('mostra o erro quando a API falha', async () => {
    api.get.mockImplementation((url) =>
      url === '/api/prestadores' ? Promise.resolve(PRESTADORES) : Promise.reject(new Error('Falha na API')),
    );
    renderPagina();

    expect(await screen.findByText('Falha na API')).toBeInTheDocument();
  });
});
