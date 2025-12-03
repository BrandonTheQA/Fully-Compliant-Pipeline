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

// Suppress expected XMLHttpRequest errors in test environment
// These occur when components try to make API calls but the backend isn't available
// Tests should mock API calls, but some components may still attempt real requests during mount
const originalError = console.error;
console.error = (...args: unknown[]) => {
  // Filter out expected XMLHttpRequest AggregateErrors from jsdom
  if (
    args.length > 0 &&
    typeof args[0] === 'object' &&
    args[0] !== null &&
    'type' in args[0] &&
    args[0].type === 'XMLHttpRequest'
  ) {
    // Suppress these expected errors
    return;
  }
  // Suppress AggregateError messages related to XMLHttpRequest
  const errorMessage = args[0]?.toString() || '';
  if (
    errorMessage.includes('AggregateError') &&
    (errorMessage.includes('XMLHttpRequest') || errorMessage.includes('xhr-utils'))
  ) {
    // Suppress these expected errors
    return;
  }
  // Pass through all other errors
  originalError.apply(console, args);
};

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

