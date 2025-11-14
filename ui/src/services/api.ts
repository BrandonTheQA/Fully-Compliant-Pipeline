import axios from 'axios';

// Use relative path - nginx will proxy /api requests to the backend
// The backend URL is configured via Azure App Service BACKEND_URL setting
const apiBaseUrl = '/api';

// Unified API instance - all requests go through nginx reverse proxy
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
