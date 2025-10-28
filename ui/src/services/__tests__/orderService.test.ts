import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { orderService } from '../orderService';
import { orderApi } from '../api';
import type { Order, CreateOrderRequest } from '../../types';

jest.mock('../api');

describe('orderService', () => {
  const mockOrderApi = orderApi as jest.Mocked<typeof orderApi>;

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('createOrder', () => {
    it('should create an order successfully', async () => {
      const orderData: CreateOrderRequest = {
        userId: '123',
        items: [
          { productId: '1', quantity: 2 },
          { productId: '2', quantity: 1 },
        ],
      };

      const mockOrder: Order = {
        id: 'order-1',
        userId: '123',
        items: [
          {
            productId: '1',
            productName: 'Laptop',
            quantity: 2,
            price: 999.99,
            subtotal: 1999.98,
          },
          {
            productId: '2',
            productName: 'Mouse',
            quantity: 1,
            price: 29.99,
            subtotal: 29.99,
          },
        ],
        totalAmount: 2029.97,
        status: 'PENDING',
      };

      mockOrderApi.post.mockResolvedValue({ data: mockOrder });

      const result = await orderService.createOrder(orderData);

      expect(mockOrderApi.post).toHaveBeenCalledWith('/orders', orderData);
      expect(result).toEqual(mockOrder);
    });

    it('should handle errors when creating order', async () => {
      const orderData: CreateOrderRequest = {
        userId: '123',
        items: [{ productId: '1', quantity: 1 }],
      };

      const error = new Error('Failed to create order');
      mockOrderApi.post.mockRejectedValue(error);

      await expect(orderService.createOrder(orderData)).rejects.toThrow('Failed to create order');
    });
  });

  describe('getOrder', () => {
    it('should get an order successfully', async () => {
      const orderId = 'order-1';
      const mockOrder: Order = {
        id: 'order-1',
        userId: '123',
        items: [],
        totalAmount: 0,
        status: 'PENDING',
      };

      mockOrderApi.get.mockResolvedValue({ data: mockOrder });

      const result = await orderService.getOrder(orderId);

      expect(mockOrderApi.get).toHaveBeenCalledWith('/orders/order-1');
      expect(result).toEqual(mockOrder);
    });
  });

  describe('getUserOrders', () => {
    it('should get user orders successfully', async () => {
      const userId = '123';
      const mockOrders: Order[] = [
        {
          id: 'order-1',
          userId: '123',
          items: [],
          totalAmount: 100,
          status: 'PENDING',
        },
      ];

      mockOrderApi.get.mockResolvedValue({ data: mockOrders });

      const result = await orderService.getUserOrders(userId);

      expect(mockOrderApi.get).toHaveBeenCalledWith('/orders/user/123');
      expect(result).toEqual(mockOrders);
    });
  });
});

