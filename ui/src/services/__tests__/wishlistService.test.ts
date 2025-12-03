import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { wishlistService } from '../wishlistService';
import { productApi } from '../api';
import type { Product } from '../../types';

jest.mock('../api');

describe('wishlistService', () => {
  const mockProductApi = productApi as jest.Mocked<typeof productApi>;
  const mockUserId = 'user-123';
  const mockProductId = 'product-1';

  const mockProduct: Product = {
    id: mockProductId,
    name: 'Test Product',
    description: 'Test Description',
    price: 29.99,
    quantity: 10,
    category: 'Electronics',
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('getWishlist', () => {
    it('should fetch wishlist successfully', async () => {
      const mockWishlist: Product[] = [mockProduct];

      mockProductApi.get.mockResolvedValue({ data: mockWishlist });

      const result = await wishlistService.getWishlist(mockUserId);

      expect(mockProductApi.get).toHaveBeenCalledWith(`/wishlist/${mockUserId}`);
      expect(result).toEqual(mockWishlist);
    });

    it('should return empty array when wishlist is empty', async () => {
      mockProductApi.get.mockResolvedValue({ data: [] });

      const result = await wishlistService.getWishlist(mockUserId);

      expect(mockProductApi.get).toHaveBeenCalledWith(`/wishlist/${mockUserId}`);
      expect(result).toEqual([]);
    });

    it('should handle errors when fetching wishlist', async () => {
      const error = new Error('Failed to fetch wishlist');
      mockProductApi.get.mockRejectedValue(error);

      await expect(wishlistService.getWishlist(mockUserId)).rejects.toThrow('Failed to fetch wishlist');
    });
  });

  describe('addItem', () => {
    it('should add item to wishlist successfully', async () => {
      mockProductApi.post.mockResolvedValue({ data: null });

      await wishlistService.addItem(mockUserId, mockProductId);

      expect(mockProductApi.post).toHaveBeenCalledWith(`/wishlist/${mockUserId}/${mockProductId}`);
    });

    it('should handle errors when adding item', async () => {
      const error = new Error('Failed to add item');
      mockProductApi.post.mockRejectedValue(error);

      await expect(wishlistService.addItem(mockUserId, mockProductId)).rejects.toThrow('Failed to add item');
    });
  });

  describe('removeItem', () => {
    it('should remove item from wishlist successfully', async () => {
      mockProductApi.delete.mockResolvedValue({ data: null });

      await wishlistService.removeItem(mockUserId, mockProductId);

      expect(mockProductApi.delete).toHaveBeenCalledWith(`/wishlist/${mockUserId}/${mockProductId}`);
    });

    it('should handle errors when removing item', async () => {
      const error = new Error('Failed to remove item');
      mockProductApi.delete.mockRejectedValue(error);

      await expect(wishlistService.removeItem(mockUserId, mockProductId)).rejects.toThrow('Failed to remove item');
    });
  });
});
