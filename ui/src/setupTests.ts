/// <reference types="@testing-library/jest-dom" />
import '@testing-library/jest-dom';
/// <reference path="./test-setup.d.ts" />

// Expose a mock of the Vite environment for Jest tests so modules relying on
// configuration (like the shared Axios instance) can resolve values without
// touching `import.meta`.
if (typeof globalThis !== 'undefined') {
  const existingEnv = (globalThis as { __APP_ENV__?: Record<string, string | undefined> }).__APP_ENV__ ?? {};
  (globalThis as { __APP_ENV__?: Record<string, string | undefined> }).__APP_ENV__ = {
    ...existingEnv,
    VITE_API_BASE_URL: process.env.VITE_API_BASE_URL || '/api',
  };
}

