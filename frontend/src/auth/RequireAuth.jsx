import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

/** tipo: 'PRESTADOR' (padrao), 'CLIENTE' ou 'QUALQUER'. Papel errado ou deslogado vai para /login. */
export default function RequireAuth({ children, tipo = 'PRESTADOR' }) {
  const { prestador, cliente } = useAuth();

  // prestador e cliente ficam undefined juntos enquanto a sessao carrega
  if (prestador === undefined || cliente === undefined) {
    return <p>Carregando...</p>;
  }

  const permitido = tipo === 'QUALQUER' ? Boolean(prestador || cliente) : Boolean(tipo === 'CLIENTE' ? cliente : prestador);
  if (!permitido) {
    return <Navigate to="/login" replace />;
  }

  return children;
}
