import { productApi } from './api';
import type { Product, CreateProductRequest } from '../types';

export const productService = {
  getAllProducts: async (): Promise<Product[]> => {
    const response = await productApi.get<Product[]>('/products');
    return response.data;
  },

  getProduct: async (productId: string): Promise<Product> => {
    const response = await productApi.get<Product>(`/products/${productId}`);
    return response.data;
  },

  createProduct: async (productData: CreateProductRequest): Promise<Product> => {
    const response = await productApi.post<Product>('/products', productData);
    return response.data;
  },
};

