import '@testing-library/jest-dom';

// Mock Vite's import.meta.env for Jest tests
(global as any).import = { meta: { env: {} } };

