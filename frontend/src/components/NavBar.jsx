import { NavLink } from 'react-router-dom';

export default function NavBar() {
  return (
    <nav className="navbar">
      <span className="navbar-title">Agendamento Pix</span>
      <NavLink to="/prestadores">Prestadores</NavLink>
      <NavLink to="/servicos">Serviços</NavLink>
      <NavLink to="/clientes">Clientes</NavLink>
      <NavLink to="/agendamentos/novo">Novo agendamento</NavLink>
    </nav>
  );
}
