import { productApi } from './api';
import type { Product } from '../types';

export const wishlistService = {
  getWishlist: async (userId: string): Promise<Product[]> => {
    const response = await productApi.get<Product[]>(`/wishlist/${userId}`);
    return response.data;
  },

  addItem: async (userId: string, productId: string): Promise<void> => {
    await productApi.post(`/wishlist/${userId}/${productId}`);
  },

  removeItem: async (userId: string, productId: string): Promise<void> => {
    await productApi.delete(`/wishlist/${userId}/${productId}`);
  },
};
