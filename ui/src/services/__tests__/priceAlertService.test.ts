import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { priceAlertService } from '../priceAlertService';
import { productApi } from '../api';
import type { PriceAlert, PriceHistory, CreatePriceAlertRequest, UpdatePriceAlertRequest } from '../priceAlertService';

jest.mock('../api');

describe('priceAlertService', () => {
  const mockProductApi = productApi as jest.Mocked<typeof productApi>;

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('createAlert', () => {
    it('should create price alert successfully', async () => {
      const mockRequest: CreatePriceAlertRequest = {
        productId: 'product-1',
        email: 'test@example.com',
        targetPrice: 80.0,
        notificationFrequency: 'IMMEDIATE',
      };

      const mockResponse: PriceAlert = {
        alertId: 'alert-1',
        productId: 'product-1',
        userEmail: 'test@example.com',
        currentPrice: 100.0,
        targetPrice: 80.0,
        status: 'ACTIVE',
        notificationFrequency: 'IMMEDIATE',
        createdAt: '2023-01-01T00:00:00',
        updatedAt: '2023-01-01T00:00:00',
      };

      mockProductApi.post.mockResolvedValue({ data: mockResponse });

      const result = await priceAlertService.createAlert(mockRequest);

      expect(mockProductApi.post).toHaveBeenCalledWith('/v2/price-alerts', mockRequest);
      expect(result).toEqual(mockResponse);
    });

    it('should handle errors when creating alert', async () => {
      const mockRequest: CreatePriceAlertRequest = {
        productId: 'product-1',
        email: 'test@example.com',
      };

      const error = new Error('Failed to create price alert');
      mockProductApi.post.mockRejectedValue(error);

      await expect(priceAlertService.createAlert(mockRequest)).rejects.toThrow('Failed to create price alert');
    });
  });

  describe('getAlerts', () => {
    it('should get alerts by email successfully', async () => {
      const mockAlerts: PriceAlert[] = [
        {
          alertId: 'alert-1',
          productId: 'product-1',
          userEmail: 'test@example.com',
          currentPrice: 100.0,
          status: 'ACTIVE',
          notificationFrequency: 'IMMEDIATE',
          createdAt: '2023-01-01T00:00:00',
          updatedAt: '2023-01-01T00:00:00',
        },
      ];

      mockProductApi.get.mockResolvedValue({ data: { alerts: mockAlerts } });

      const result = await priceAlertService.getAlerts('test@example.com');

      expect(mockProductApi.get).toHaveBeenCalledWith('/v2/price-alerts?email=test%40example.com');
      expect(result).toEqual(mockAlerts);
    });

    it('should get alerts by userId successfully', async () => {
      const mockAlerts: PriceAlert[] = [];

      mockProductApi.get.mockResolvedValue({ data: { alerts: mockAlerts } });

      const result = await priceAlertService.getAlerts(undefined, 'user-1');

      expect(mockProductApi.get).toHaveBeenCalledWith('/v2/price-alerts?userId=user-1');
      expect(result).toEqual(mockAlerts);
    });
  });

  describe('getAlert', () => {
    it('should get alert by ID successfully', async () => {
      const mockAlert: PriceAlert = {
        alertId: 'alert-1',
        productId: 'product-1',
        userEmail: 'test@example.com',
        currentPrice: 100.0,
        status: 'ACTIVE',
        notificationFrequency: 'IMMEDIATE',
        createdAt: '2023-01-01T00:00:00',
        updatedAt: '2023-01-01T00:00:00',
      };

      mockProductApi.get.mockResolvedValue({ data: mockAlert });

      const result = await priceAlertService.getAlert('alert-1');

      expect(mockProductApi.get).toHaveBeenCalledWith('/v2/price-alerts/alert-1');
      expect(result).toEqual(mockAlert);
    });
  });

  describe('updateAlert', () => {
    it('should update alert successfully', async () => {
      const mockRequest: UpdatePriceAlertRequest = {
        targetPrice: 75.0,
        notificationFrequency: 'DAILY_DIGEST',
      };

      const mockResponse: PriceAlert = {
        alertId: 'alert-1',
        productId: 'product-1',
        userEmail: 'test@example.com',
        currentPrice: 100.0,
        targetPrice: 75.0,
        status: 'ACTIVE',
        notificationFrequency: 'DAILY_DIGEST',
        createdAt: '2023-01-01T00:00:00',
        updatedAt: '2023-01-01T00:00:00',
      };

      mockProductApi.put.mockResolvedValue({ data: mockResponse });

      const result = await priceAlertService.updateAlert('alert-1', mockRequest);

      expect(mockProductApi.put).toHaveBeenCalledWith('/v2/price-alerts/alert-1', mockRequest);
      expect(result).toEqual(mockResponse);
    });
  });

  describe('deleteAlert', () => {
    it('should delete alert successfully', async () => {
      mockProductApi.delete.mockResolvedValue({ data: undefined });

      await priceAlertService.deleteAlert('alert-1');

      expect(mockProductApi.delete).toHaveBeenCalledWith('/v2/price-alerts/alert-1');
    });
  });

  describe('getPriceHistory', () => {
    it('should get price history successfully', async () => {
      const mockHistory: PriceHistory[] = [
        {
          priceHistoryId: 'history-1',
          productId: 'product-1',
          price: 90.0,
          previousPrice: 100.0,
          changeType: 'DECREASE',
          changePercentage: 10.0,
          changedAt: '2023-01-01T00:00:00',
        },
      ];

      mockProductApi.get.mockResolvedValue({ data: mockHistory });

      const result = await priceAlertService.getPriceHistory('alert-1');

      expect(mockProductApi.get).toHaveBeenCalledWith('/v2/price-alerts/alert-1/history');
      expect(result).toEqual(mockHistory);
    });
  });
});

