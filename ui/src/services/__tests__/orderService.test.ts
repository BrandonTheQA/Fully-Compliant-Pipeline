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

  describe('getOrderTracking', () => {
    it('should get order tracking successfully', async () => {
      const orderId = 'order-1';
      const mockTracking = {
        orderId: 'order-1',
        status: 'SHIPPED',
        trackingNumber: 'TRACK123',
        carrierName: 'FedEx',
        estimatedDeliveryDate: '2024-01-15T10:00:00Z',
        shippingAddress: '123 Main St',
        shippingMethod: 'Standard',
        currentLocation: 'Distribution Center',
        statusHistory: [],
      };

      mockOrderApi.get.mockResolvedValue({ data: mockTracking });

      const result = await orderService.getOrderTracking(orderId);

      expect(mockOrderApi.get).toHaveBeenCalledWith('/orders/order-1/tracking');
      expect(result).toEqual(mockTracking);
    });
  });

  describe('getOrderStatusHistory', () => {
    it('should get order status history successfully', async () => {
      const orderId = 'order-1';
      const mockHistory = [
        {
          id: 'status-1',
          status: 'PENDING',
          createdAt: '2024-01-10T10:00:00Z',
        },
        {
          id: 'status-2',
          status: 'SHIPPED',
          createdAt: '2024-01-12T10:00:00Z',
        },
      ];

      mockOrderApi.get.mockResolvedValue({ data: mockHistory });

      const result = await orderService.getOrderStatusHistory(orderId);

      expect(mockOrderApi.get).toHaveBeenCalledWith('/orders/order-1/status-history');
      expect(result).toEqual(mockHistory);
    });
  });

  describe('subscribeToOrderUpdates', () => {
    it('should create EventSource for order updates', () => {
      const orderId = 'order-1';
      const onUpdate = jest.fn();
      const mockEventSource = {
        addEventListener: jest.fn(),
        removeEventListener: jest.fn(),
        close: jest.fn(),
        onerror: null,
        readyState: 0,
      };

      global.EventSource = jest.fn().mockImplementation(() => mockEventSource) as any;

      const eventSource = orderService.subscribeToOrderUpdates(orderId, onUpdate);

      expect(global.EventSource).toHaveBeenCalledWith('/api/orders/order-1/tracking/stream');
      expect(mockEventSource.addEventListener).toHaveBeenCalledWith('connected', expect.any(Function));
      expect(mockEventSource.addEventListener).toHaveBeenCalledWith('status-update', expect.any(Function));
      expect(eventSource).toBe(mockEventSource);
    });

    it('should call onUpdate when status-update event is received', () => {
      const orderId = 'order-1';
      const onUpdate = jest.fn();
      const mockEventSource = {
        addEventListener: jest.fn((event: string, handler: (e: any) => void) => {
          if (event === 'status-update') {
            // Simulate event
            setTimeout(() => {
              handler({ data: JSON.stringify({ status: 'SHIPPED' }) });
            }, 0);
          }
        }),
        removeEventListener: jest.fn(),
        close: jest.fn(),
        onerror: null,
        readyState: 0,
      };

      global.EventSource = jest.fn().mockImplementation(() => mockEventSource) as any;

      orderService.subscribeToOrderUpdates(orderId, onUpdate);

      return new Promise((resolve) => {
        setTimeout(() => {
          expect(onUpdate).toHaveBeenCalledWith({ status: 'SHIPPED' });
          resolve(undefined);
        }, 10);
      });
    });

    it('should handle JSON parse errors gracefully', () => {
      const orderId = 'order-1';
      const onUpdate = jest.fn();
      const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
      const mockEventSource = {
        addEventListener: jest.fn((event: string, handler: (e: any) => void) => {
          if (event === 'status-update') {
            // Simulate invalid JSON
            setTimeout(() => {
              handler({ data: 'invalid json' });
            }, 0);
          }
        }),
        removeEventListener: jest.fn(),
        close: jest.fn(),
        onerror: null,
        readyState: 0,
      };

      global.EventSource = jest.fn().mockImplementation(() => mockEventSource) as any;

      orderService.subscribeToOrderUpdates(orderId, onUpdate);

      return new Promise((resolve) => {
        setTimeout(() => {
          expect(consoleErrorSpy).toHaveBeenCalled();
          consoleErrorSpy.mockRestore();
          resolve(undefined);
        }, 10);
      });
    });
  });

  describe('updateNotificationPreferences', () => {
    it('should update notification preferences successfully', async () => {
      const userId = 'user-123';
      const preferences = {
        userId: 'user-123',
        emailEnabled: true,
        smsEnabled: false,
        phoneNumber: '',
        notificationFrequency: 'ALL' as const,
      };

      mockOrderApi.put.mockResolvedValue({ data: preferences });

      const result = await orderService.updateNotificationPreferences(userId, preferences);

      expect(mockOrderApi.put).toHaveBeenCalledWith(
        '/notifications/preferences?userId=user-123',
        preferences
      );
      expect(result).toEqual(preferences);
    });
  });

  describe('getNotificationPreferences', () => {
    it('should get notification preferences successfully', async () => {
      const userId = 'user-123';
      const preferences = {
        userId: 'user-123',
        emailEnabled: true,
        smsEnabled: false,
        phoneNumber: '',
        notificationFrequency: 'ALL' as const,
      };

      mockOrderApi.get.mockResolvedValue({ data: preferences });

      const result = await orderService.getNotificationPreferences(userId);

      expect(mockOrderApi.get).toHaveBeenCalledWith('/notifications/preferences?userId=user-123');
      expect(result).toEqual(preferences);
    });
  });
});

