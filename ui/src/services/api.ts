import axios, { type AxiosInstance } from 'axios';

// Helper to safely get environment variables in both Vite and Jest
function getEnvVar(key: string, defaultValue: string): string {
  // In Vite, this will be replaced at build time
  // In Jest, this will throw and we catch it
  try {
    // This is replaced by Vite's build process, but Jest can't parse it
    // We'll use eval to defer the parse error to runtime
    const metaEnv = (globalThis as any).__MOCK_VITE_ENV__;
    if (metaEnv && metaEnv[key]) {
      return metaEnv[key];
    }
  } catch {
    // Not in Vite environment
  }
  
  // Fall back to process.env (Jest/Node) or default
  if (typeof process !== 'undefined' && process.env[key]) {
    return process.env[key] as string;
  }
  return defaultValue;
}

// Get API base URLs from environment variables
const getUserBaseUrl = () => getEnvVar('VITE_USER_API_URL', 'https://joaz-func-user-9021-test.azurewebsites.net/api');
const getProductBaseUrl = () => getEnvVar('VITE_PRODUCT_API_URL', 'https://joaz-func-product-9021-test.azurewebsites.net/api');
const getOrderBaseUrl = () => getEnvVar('VITE_ORDER_API_URL', 'https://joaz-func-order-9021-test.azurewebsites.net/api');

// Create axios instances for each service
const userApi = axios.create({
  baseURL: getUserBaseUrl(),
  headers: {
    'Content-Type': 'application/json',
  },
});

const productApi = axios.create({
  baseURL: getProductBaseUrl(),
  headers: {
    'Content-Type': 'application/json',
  },
});

const orderApi = axios.create({
  baseURL: getOrderBaseUrl(),
  headers: {
    'Content-Type': 'application/json',
  },
});

// Response interceptors for error handling
const setupInterceptors = (apiInstance: AxiosInstance) => {
  apiInstance.interceptors.response.use(
    (response) => response,
    (error) => {
      const message = error.response?.data?.message || error.message || 'An error occurred';
      return Promise.reject(new Error(message));
    }
  );
};

setupInterceptors(userApi);
setupInterceptors(productApi);
setupInterceptors(orderApi);

export { userApi, productApi, orderApi };
