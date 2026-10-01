import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

export default function RequireAuth({ children }) {
  const { prestador } = useAuth();

  if (prestador === undefined) {
    return <p>Carregando...</p>;
  }

  if (prestador === null) {
    return <Navigate to="/login" replace />;
  }

  return children;
}
