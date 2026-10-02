import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { api } from './client';

function mockResponse({ status = 200, body = null }) {
  return {
    status,
    ok: status >= 200 && status < 300,
    text: () => Promise.resolve(body === null ? '' : JSON.stringify(body)),
  };
}

describe('api client', () => {
  beforeEach(() => {
    document.cookie = '';
    global.fetch = vi.fn();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('GET nao envia o header X-XSRF-TOKEN', async () => {
    document.cookie = 'XSRF-TOKEN=token-de-teste';
    global.fetch.mockResolvedValue(mockResponse({ body: { ok: true } }));

    await api.get('/api/prestadores');

    const [, options] = global.fetch.mock.calls[0];
    expect(options.headers['X-XSRF-TOKEN']).toBeUndefined();
  });

  it('POST le o cookie XSRF-TOKEN e manda como header', async () => {
    document.cookie = 'XSRF-TOKEN=token-de-teste';
    global.fetch.mockResolvedValue(mockResponse({ status: 201, body: { id: '1' } }));

    await api.post('/api/prestadores', { nome: 'Teste' });

    const [, options] = global.fetch.mock.calls[0];
    expect(options.headers['X-XSRF-TOKEN']).toBe('token-de-teste');
    expect(options.method).toBe('POST');
    expect(options.credentials).toBe('include');
  });

  it('devolve null para resposta 204', async () => {
    global.fetch.mockResolvedValue(mockResponse({ status: 204 }));

    const resultado = await api.delete('/api/clientes/1');

    expect(resultado).toBeNull();
  });

  it('lanca Error com a mensagem do backend quando a resposta nao e ok', async () => {
    global.fetch.mockResolvedValue(mockResponse({ status: 409, body: { mensagem: 'Documento duplicado' } }));

    await expect(api.post('/api/prestadores', {})).rejects.toThrow('Documento duplicado');
  });

  it('lanca Error generico quando o backend nao manda mensagem', async () => {
    global.fetch.mockResolvedValue(mockResponse({ status: 500, body: null }));

    await expect(api.get('/api/prestadores')).rejects.toThrow('Erro 500');
  });
});
