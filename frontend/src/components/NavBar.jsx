import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

export default function NavBar() {
  const { prestador, logout } = useAuth();
  const navigate = useNavigate();

  async function handleLogout() {
    await logout();
    navigate('/login');
  }

  return (
    <nav className="navbar">
      <span className="navbar-title">Agendamento Pix</span>
      <NavLink to="/agenda">Agenda</NavLink>
      <NavLink to="/prestadores">Prestadores</NavLink>
      <NavLink to="/servicos">Serviços</NavLink>
      <NavLink to="/clientes">Clientes</NavLink>
      <NavLink to="/agendamentos/novo">Novo agendamento</NavLink>

      <span className="navbar-sessao">
        {prestador ? (
          <>
            <span className="navbar-usuario">{prestador.nome}</span>
            <button type="button" className="navbar-botao-sair" onClick={handleLogout}>
              Sair
            </button>
          </>
        ) : (
          <NavLink to="/login">Entrar</NavLink>
        )}
      </span>
    </nav>
  );
}
