/// <reference types="@testing-library/jest-dom" />
import '@testing-library/jest-dom';
import { toHaveNoViolations } from 'jest-axe';
/// <reference path="./test-setup.d.ts" />

// Extend Jest expect with jest-axe matchers
expect.extend(toHaveNoViolations);

// Polyfill for TextEncoder/TextDecoder for react-router compatibility
import { TextEncoder, TextDecoder } from 'util';
global.TextEncoder = TextEncoder;
global.TextDecoder = TextDecoder as typeof global.TextDecoder;

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

