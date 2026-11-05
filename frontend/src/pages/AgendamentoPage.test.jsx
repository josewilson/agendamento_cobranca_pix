import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import AgendamentoPage from './AgendamentoPage.jsx';
import { api } from '../api/client';

vi.mock('../api/client', () => ({ api: { get: vi.fn(), post: vi.fn() } }));

const ID = 'abc-123';

function agendamento(status) {
  return {
    status,
    inicio: '2026-12-01T13:00:00Z',
    fim: '2026-12-01T13:30:00Z',
    valorServico: '100.00',
    valorSinal: '30.00',
  };
}

function renderPagina() {
  return render(
    <MemoryRouter initialEntries={[`/agendamentos/${ID}`]}>
      <Routes>
        <Route path="/agendamentos/:id" element={<AgendamentoPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('AgendamentoPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('mostra o status e valores, nunca o id', async () => {
    api.get.mockResolvedValue(agendamento('PENDENTE_PAGAMENTO'));
    renderPagina();

    expect(await screen.findByText('PENDENTE_PAGAMENTO')).toBeInTheDocument();
    expect(screen.getByText(/Valor do sinal: R\$ 30.00/)).toBeInTheDocument();
    expect(screen.queryByText(new RegExp(ID))).not.toBeInTheDocument();
  });

  it('mostra o erro quando o agendamento nao e encontrado', async () => {
    api.get.mockRejectedValue(new Error('Agendamento nao encontrado'));
    renderPagina();

    expect(await screen.findByText('Agendamento nao encontrado')).toBeInTheDocument();
  });

  it('so permite simular pagamento enquanto pendente', async () => {
    api.get.mockResolvedValue(agendamento('CONFIRMADO'));
    renderPagina();

    expect(await screen.findByRole('button', { name: 'Simular pagamento' })).toBeDisabled();
  });

  it('simula o pagamento e recarrega o agendamento', async () => {
    api.get.mockResolvedValueOnce(agendamento('PENDENTE_PAGAMENTO')).mockResolvedValueOnce(agendamento('CONFIRMADO'));
    api.post.mockResolvedValue({});
    const usuario = userEvent.setup();
    renderPagina();

    await usuario.click(await screen.findByRole('button', { name: 'Simular pagamento' }));

    expect(api.post).toHaveBeenCalledWith('/api/webhooks/pagamento', { agendamentoId: ID, pago: true });
    expect(await screen.findByText('CONFIRMADO')).toBeInTheDocument();
  });

  it('cancela e mostra valor retido e reembolsado', async () => {
    api.get.mockResolvedValue(agendamento('CONFIRMADO'));
    api.post.mockResolvedValue({ valorRetido: '30.00', valorReembolsado: '0.00' });
    const usuario = userEvent.setup();
    renderPagina();

    await usuario.click(await screen.findByRole('button', { name: 'Cancelar' }));

    expect(api.post).toHaveBeenCalledWith(`/api/agendamentos/${ID}/cancelar`, {});
    expect(await screen.findByText('Valor retido: R$ 30.00')).toBeInTheDocument();
    expect(screen.getByText('Valor reembolsado: R$ 0.00')).toBeInTheDocument();
  });

  it('marca no-show', async () => {
    api.get.mockResolvedValue(agendamento('CONFIRMADO'));
    api.post.mockResolvedValue({});
    const usuario = userEvent.setup();
    renderPagina();

    await usuario.click(await screen.findByRole('button', { name: 'Marcar no-show' }));

    expect(api.post).toHaveBeenCalledWith(`/api/agendamentos/${ID}/no-show`, {});
  });
});
