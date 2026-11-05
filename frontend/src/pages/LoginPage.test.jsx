import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import LoginPage from './LoginPage.jsx';
import { useAuth } from '../auth/AuthContext';

vi.mock('../auth/AuthContext', () => ({ useAuth: vi.fn() }));

function renderPagina() {
  return render(
    <MemoryRouter initialEntries={['/login']}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/agenda" element={<p>tela da agenda</p>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('LoginPage', () => {
  let login;

  beforeEach(() => {
    login = vi.fn();
    useAuth.mockReturnValue({ login });
  });

  it('faz login com email/senha e vai para a agenda', async () => {
    login.mockResolvedValue(undefined);
    const usuario = userEvent.setup();
    renderPagina();

    await usuario.type(screen.getByLabelText('Email'), 'a@b.com');
    await usuario.type(screen.getByLabelText('Senha'), 'senha1234');
    await usuario.click(screen.getByRole('button', { name: 'Entrar' }));

    expect(login).toHaveBeenCalledWith('a@b.com', 'senha1234');
    expect(await screen.findByText('tela da agenda')).toBeInTheDocument();
  });

  it('mostra a mensagem de erro e continua na tela quando o login falha', async () => {
    login.mockRejectedValue(new Error('Credenciais invalidas'));
    const usuario = userEvent.setup();
    renderPagina();

    await usuario.type(screen.getByLabelText('Email'), 'a@b.com');
    await usuario.type(screen.getByLabelText('Senha'), 'errada123');
    await usuario.click(screen.getByRole('button', { name: 'Entrar' }));

    expect(await screen.findByText('Credenciais invalidas')).toBeInTheDocument();
    expect(screen.queryByText('tela da agenda')).not.toBeInTheDocument();
  });
});
