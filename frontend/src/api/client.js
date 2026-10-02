const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

function lerCookie(nome) {
  const prefixo = `${nome}=`;
  const valor = document.cookie.split('; ').find((linha) => linha.startsWith(prefixo));
  return valor ? decodeURIComponent(valor.slice(prefixo.length)) : null;
}

async function request(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...options.headers };

  // CSRF: o backend usa CookieCsrfTokenRepository (cookie XSRF-TOKEN, legível por JS de propósito).
  // Metodos seguros (GET) nao exigem o header; o cookie em si so existe depois da primeira resposta
  // do backend (CsrfCookieFilter o grava em toda requisicao, inclusive GET).
  const metodo = (options.method || 'GET').toUpperCase();
  if (metodo !== 'GET') {
    const token = lerCookie('XSRF-TOKEN');
    if (token) {
      headers['X-XSRF-TOKEN'] = token;
    }
  }

  const response = await fetch(`${BASE_URL}${path}`, {
    headers,
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
  put: (path, body) => request(path, { method: 'PUT', body: JSON.stringify(body) }),
  delete: (path) => request(path, { method: 'DELETE' }),
};
