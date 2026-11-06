import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

export default function NavBar() {
  const { prestador, cliente, logout } = useAuth();
  const navigate = useNavigate();
  const usuario = prestador || cliente;

  async function handleLogout() {
    await logout();
    navigate('/login');
  }

  return (
    <nav className="navbar">
      <span className="navbar-title">Agendamento Pix</span>
      {prestador && <NavLink to="/agenda">Agenda</NavLink>}
      <NavLink to="/prestadores">Prestadores</NavLink>
      {prestador && <NavLink to="/servicos">Serviços</NavLink>}
      {prestador && <NavLink to="/profissionais">Profissionais</NavLink>}
      {prestador && <NavLink to="/clientes">Clientes</NavLink>}
      {!usuario && <NavLink to="/clientes">Cadastrar cliente</NavLink>}
      {cliente && <NavLink to="/agendamentos/novo">Novo agendamento</NavLink>}

      <span className="navbar-sessao">
        {usuario ? (
          <>
            <span className="navbar-usuario">{usuario.nome}</span>
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
