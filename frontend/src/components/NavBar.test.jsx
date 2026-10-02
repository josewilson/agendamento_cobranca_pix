import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import NavBar from './NavBar.jsx';
import { useAuth } from '../auth/AuthContext.jsx';

vi.mock('../auth/AuthContext.jsx', () => ({
  useAuth: vi.fn(),
}));

function renderNavBar() {
  return render(
    <MemoryRouter>
      <NavBar />
    </MemoryRouter>,
  );
}

describe('NavBar', () => {
  it('mostra o link "Entrar" quando ninguem esta logado', () => {
    useAuth.mockReturnValue({ prestador: null, logout: vi.fn() });

    renderNavBar();

    expect(screen.getByText('Entrar')).toBeInTheDocument();
    expect(screen.queryByText('Sair')).not.toBeInTheDocument();
  });

  it('mostra o nome do prestador e o botao Sair quando logado', () => {
    useAuth.mockReturnValue({ prestador: { nome: 'Clinica Bem-Estar' }, logout: vi.fn() });

    renderNavBar();

    expect(screen.getByText('Clinica Bem-Estar')).toBeInTheDocument();
    expect(screen.getByText('Sair')).toBeInTheDocument();
  });

  it('chama logout ao clicar em Sair', async () => {
    const logout = vi.fn().mockResolvedValue(undefined);
    useAuth.mockReturnValue({ prestador: { nome: 'Clinica Bem-Estar' }, logout });
    const usuario = userEvent.setup();

    renderNavBar();
    await usuario.click(screen.getByText('Sair'));

    expect(logout).toHaveBeenCalledTimes(1);
  });
});
