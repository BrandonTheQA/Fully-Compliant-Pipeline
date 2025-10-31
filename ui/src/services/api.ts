import axios from 'axios';

// Unified API instance using BFF pattern - all requests go through /api
const api = axios.create({
  baseURL: '/api',
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
