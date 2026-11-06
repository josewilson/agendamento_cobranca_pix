import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext.jsx';
import RequireAuth from './auth/RequireAuth.jsx';
import NavBar from './components/NavBar.jsx';
import AgendaPage from './pages/AgendaPage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import PrestadoresPage from './pages/PrestadoresPage.jsx';
import ServicosPage from './pages/ServicosPage.jsx';
import ProfissionaisPage from './pages/ProfissionaisPage.jsx';
import ClientesPage from './pages/ClientesPage.jsx';
import NovoAgendamentoPage from './pages/NovoAgendamentoPage.jsx';
import AgendamentoPage from './pages/AgendamentoPage.jsx';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <NavBar />
        <main className="container">
          <Routes>
            <Route path="/" element={<Navigate to="/agenda" replace />} />
            <Route path="/login" element={<LoginPage />} />
            <Route
              path="/agenda"
              element={
                <RequireAuth>
                  <AgendaPage />
                </RequireAuth>
              }
            />
            <Route
              path="/servicos"
              element={
                <RequireAuth>
                  <ServicosPage />
                </RequireAuth>
              }
            />
            <Route
              path="/profissionais"
              element={
                <RequireAuth>
                  <ProfissionaisPage />
                </RequireAuth>
              }
            />
            <Route path="/prestadores" element={<PrestadoresPage />} />
            <Route path="/clientes" element={<ClientesPage />} />
            <Route
              path="/agendamentos/novo"
              element={
                <RequireAuth tipo="CLIENTE">
                  <NovoAgendamentoPage />
                </RequireAuth>
              }
            />
            <Route
              path="/agendamentos/:id"
              element={
                <RequireAuth tipo="QUALQUER">
                  <AgendamentoPage />
                </RequireAuth>
              }
            />
          </Routes>
        </main>
      </BrowserRouter>
    </AuthProvider>
  );
}
