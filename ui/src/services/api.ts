import axios from 'axios';

type AppEnv = {
  VITE_API_BASE_URL?: string;
};

const getEnv = (): AppEnv => {
  if (typeof globalThis !== 'undefined' && (globalThis as { __APP_ENV__?: AppEnv }).__APP_ENV__) {
    return (globalThis as { __APP_ENV__?: AppEnv }).__APP_ENV__ as AppEnv;
  }

  if (typeof process !== 'undefined') {
    return {
      VITE_API_BASE_URL: process.env.VITE_API_BASE_URL,
    };
  }

  return {};
};

const normalizeBaseUrl = (value?: string): string => {
  if (!value || value.trim().length === 0) {
    return '/api';
  }

  return value.replace(/\/+$/, '') || '/api';
};

const apiBaseUrl = normalizeBaseUrl(getEnv().VITE_API_BASE_URL);

// Unified API instance using BFF pattern - all requests go through configured API base URL
const api = axios.create({
  baseURL: apiBaseUrl,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error.response?.data?.message || error.message || 'An error occurred';
    return Promise.reject(new Error(message));
  }
);

// Export the unified API instance
// Service layer will use this for all requests (userApi, productApi, orderApi are now aliases)
export const userApi = api;
export const productApi = api;
export const orderApi = api;
