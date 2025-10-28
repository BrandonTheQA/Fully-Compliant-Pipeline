/// <reference types="@testing-library/jest-dom" />
import '@testing-library/jest-dom';
/// <reference path="./test-setup.d.ts" />

// Mock Vite's import.meta.env for Jest tests
if (typeof globalThis !== 'undefined') {
  (globalThis as any).__MOCK_VITE_ENV__ = {};
}

