import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach } from 'vitest';

// RTL limpa o DOM automaticamente entre testes só quando detecta "afterEach" global
// (padrão do Jest). Sem "test.globals" no vite.config.js, o Vitest não expõe isso
// globalmente, então o cleanup precisa ser registrado explicitamente aqui.
afterEach(() => {
  cleanup();
});
