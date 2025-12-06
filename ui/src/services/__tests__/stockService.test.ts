import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { stockService } from '../stockService';
import { productApi } from '../api';
import type { StockStatusResponse, NotificationResponse } from '../stockService';

jest.mock('../api');

describe('stockService', () => {
  const mockProductApi = productApi as jest.Mocked<typeof productApi>;

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('getStockStatus', () => {
    it('should get stock status successfully', async () => {
      const productId = 'product-1';
      const mockResponse: StockStatusResponse = {
        productId: 'product-1',
        status: 'IN_STOCK',
        quantity: 50,
        lowStockThreshold: 10,
        message: 'In Stock',
      };

      mockProductApi.get.mockResolvedValue({ data: mockResponse });

      const result = await stockService.getStockStatus(productId);

      expect(mockProductApi.get).toHaveBeenCalledWith('/v2/products/product-1/stock');
      expect(result).toEqual(mockResponse);
    });

    it('should handle errors when getting stock status', async () => {
      const error = new Error('Failed to fetch stock status');
      mockProductApi.get.mockRejectedValue(error);

      await expect(stockService.getStockStatus('product-1')).rejects.toThrow('Failed to fetch stock status');
    });
  });

  describe('getBulkStockStatus', () => {
    it('should get bulk stock status successfully', async () => {
      const productIds = ['product-1', 'product-2'];
      const mockResponse = {
        statuses: [
          {
            productId: 'product-1',
            status: 'IN_STOCK' as const,
            quantity: 50,
            lowStockThreshold: 10,
            message: 'In Stock',
          },
          {
            productId: 'product-2',
            status: 'LOW_STOCK' as const,
            quantity: 5,
            lowStockThreshold: 10,
            message: 'Low Stock - Only 5 left!',
          },
        ],
      };

      mockProductApi.post.mockResolvedValue({ data: mockResponse });

      const result = await stockService.getBulkStockStatus(productIds);

      expect(mockProductApi.post).toHaveBeenCalledWith('/v2/products/stock/bulk', {
        productIds,
      });
      expect(result).toEqual(mockResponse.statuses);
    });
  });

  describe('signUpForNotification', () => {
    it('should sign up for notification successfully', async () => {
      const productId = 'product-1';
      const email = 'test@example.com';

      mockProductApi.post.mockResolvedValue({ data: undefined });

      await stockService.signUpForNotification(productId, email);

      expect(mockProductApi.post).toHaveBeenCalledWith(`/v2/products/${productId}/notify-me`, {
        productId,
        email,
      });
    });

    it('should handle errors when signing up for notification', async () => {
      const error = new Error('Failed to sign up');
      mockProductApi.post.mockRejectedValue(error);

      await expect(stockService.signUpForNotification('product-1', 'test@example.com')).rejects.toThrow('Failed to sign up');
    });
  });

  describe('getUserNotifications', () => {
    it('should get user notifications successfully', async () => {
      const userId = 'user-1';
      const mockNotifications: NotificationResponse[] = [
        {
          notificationId: 'notification-1',
          productId: 'product-1',
          productName: 'Product 1',
          status: 'PENDING',
          signupDate: '2023-01-01T00:00:00',
          notifiedDate: null,
        },
      ];

      mockProductApi.get.mockResolvedValue({ data: mockNotifications });

      const result = await stockService.getUserNotifications(userId);

      expect(mockProductApi.get).toHaveBeenCalledWith('/v2/stock/notifications', {
        params: { userId },
      });
      expect(result).toEqual(mockNotifications);
    });
  });

  describe('unsubscribe', () => {
    it('should unsubscribe from notification successfully', async () => {
      const notificationId = 'notification-1';

      mockProductApi.delete.mockResolvedValue({ data: undefined });

      await stockService.unsubscribe(notificationId);

      expect(mockProductApi.delete).toHaveBeenCalledWith(`/v2/stock/notifications/${notificationId}`);
    });

    it('should handle errors when unsubscribing', async () => {
      const error = new Error('Failed to unsubscribe');
      mockProductApi.delete.mockRejectedValue(error);

      await expect(stockService.unsubscribe('notification-1')).rejects.toThrow('Failed to unsubscribe');
    });
  });
});

