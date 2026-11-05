import { fireEvent, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import NovoAgendamentoPage from './NovoAgendamentoPage.jsx';
import { api } from '../api/client';

vi.mock('../api/client', () => ({ api: { get: vi.fn(), post: vi.fn() } }));

function mockListas() {
  api.get.mockImplementation((url) => {
    if (url === '/api/prestadores') return Promise.resolve([{ id: 'p-1', nome: 'Clinica A' }]);
    if (url === '/api/clientes') return Promise.resolve([{ id: 'c-1', nome: 'Maria' }]);
    if (url === '/api/servicos?prestadorId=p-1') {
      return Promise.resolve([{ id: 's-1', nome: 'Corte', duracaoMinutos: 45, preco: '80.00' }]);
    }
    return Promise.reject(new Error(`url inesperada: ${url}`));
  });
}

function renderPagina() {
  return render(
    <MemoryRouter initialEntries={['/agendamentos/novo']}>
      <Routes>
        <Route path="/agendamentos/novo" element={<NovoAgendamentoPage />} />
        <Route path="/agendamentos/:id" element={<p>detalhe do agendamento</p>} />
      </Routes>
    </MemoryRouter>,
  );
}

async function preencherFormulario(usuario) {
  await usuario.selectOptions(await screen.findByLabelText('Prestador'), 'Clinica A');
  await usuario.selectOptions(screen.getByLabelText('Serviço'), await screen.findByRole('option', { name: /Corte/ }));
  await usuario.selectOptions(screen.getByLabelText('Cliente'), 'Maria');
  fireEvent.change(screen.getByLabelText('Início'), { target: { value: '2030-01-10T10:00' } });
}

describe('NovoAgendamentoPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockListas();
  });

  it('mantem Serviço desabilitado e Agendar bloqueado ate escolher prestador e serviço', async () => {
    renderPagina();

    expect(await screen.findByLabelText('Serviço')).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Agendar' })).toBeDisabled();
  });

  it('cria o agendamento calculando o fim pela duracao do servico', async () => {
    api.post.mockResolvedValue({ agendamento: { id: 'ag-1', status: 'CONFIRMADO', valorSinal: '0.00' }, cobranca: null });
    const usuario = userEvent.setup();
    renderPagina();

    await preencherFormulario(usuario);
    await usuario.click(screen.getByRole('button', { name: 'Agendar' }));

    const inicio = new Date('2030-01-10T10:00');
    expect(api.post).toHaveBeenCalledWith('/api/agendamentos', {
      prestadorId: 'p-1',
      clienteId: 'c-1',
      servicoId: 's-1',
      inicio: inicio.toISOString(),
      fim: new Date(inicio.getTime() + 45 * 60000).toISOString(),
    });
    expect(await screen.findByText(/Agendamento criado!/)).toBeInTheDocument();
    expect(screen.queryByText(/Copia e cola Pix/)).not.toBeInTheDocument();
  });

  it('mostra sinal e copia-e-cola Pix quando ha cobranca, e navega para o agendamento', async () => {
    api.post.mockResolvedValue({
      agendamento: { id: 'ag-1', status: 'PENDENTE_PAGAMENTO', valorSinal: '24.00' },
      cobranca: { copiaECola: 'PIX-COPIA-COLA-123' },
    });
    const usuario = userEvent.setup();
    renderPagina();

    await preencherFormulario(usuario);
    await usuario.click(screen.getByRole('button', { name: 'Agendar' }));

    expect(await screen.findByText('PIX-COPIA-COLA-123')).toBeInTheDocument();
    expect(screen.getByText('Sinal: R$ 24.00')).toBeInTheDocument();
    expect(screen.queryByText(/ag-1/)).not.toBeInTheDocument();

    await usuario.click(screen.getByRole('button', { name: 'Gerenciar este agendamento' }));
    expect(await screen.findByText('detalhe do agendamento')).toBeInTheDocument();
  });

  it('mostra o erro de conflito de horario', async () => {
    api.post.mockRejectedValue(new Error('Horario indisponivel'));
    const usuario = userEvent.setup();
    renderPagina();

    await preencherFormulario(usuario);
    await usuario.click(screen.getByRole('button', { name: 'Agendar' }));

    expect(await screen.findByText('Horario indisponivel')).toBeInTheDocument();
    expect(screen.queryByText(/Agendamento criado!/)).not.toBeInTheDocument();
  });
});
