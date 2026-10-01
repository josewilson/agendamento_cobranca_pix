const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

async function request(path, options = {}) {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    credentials: 'include',
    ...options,
  });

  if (response.status === 204) {
    return null;
  }

  const texto = await response.text();
  const corpo = texto ? JSON.parse(texto) : null;

  if (!response.ok) {
    const mensagem = corpo?.mensagem || `Erro ${response.status}`;
    throw new Error(mensagem);
  }

  return corpo;
}

export const api = {
  get: (path) => request(path),
  post: (path, body) => request(path, { method: 'POST', body: JSON.stringify(body) }),
};
