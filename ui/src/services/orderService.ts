import { orderApi } from './api';
import type { Order, CreateOrderRequest, OrderTracking, OrderStatusHistory, NotificationPreferences } from '../types';

export const orderService = {
  createOrder: async (orderData: CreateOrderRequest): Promise<Order> => {
    const response = await orderApi.post<Order>('/orders', orderData);
    return response.data;
  },

  getOrder: async (orderId: string): Promise<Order> => {
    const response = await orderApi.get<Order>(`/orders/${orderId}`);
    return response.data;
  },

  getUserOrders: async (userId: string): Promise<Order[]> => {
    const response = await orderApi.get<Order[]>(`/orders/user/${userId}`);
    return response.data;
  },

  getOrderTracking: async (orderId: string): Promise<OrderTracking> => {
    const response = await orderApi.get<OrderTracking>(`/orders/${orderId}/tracking`);
    return response.data;
  },

  getOrderStatusHistory: async (orderId: string): Promise<OrderStatusHistory[]> => {
    const response = await orderApi.get<OrderStatusHistory[]>(`/orders/${orderId}/status-history`);
    return response.data;
  },

  subscribeToOrderUpdates: (orderId: string, onUpdate: (data: any) => void): EventSource => {
    const eventSource = new EventSource(`/api/orders/${orderId}/tracking/stream`);
    
    eventSource.addEventListener('connected', (event) => {
      console.log('Connected to order tracking stream:', event);
    });
    
    eventSource.addEventListener('status-update', (event) => {
      try {
        const data = JSON.parse(event.data);
        onUpdate(data);
      } catch (error) {
        console.error('Error parsing SSE data:', error);
      }
    });
    
    eventSource.onerror = (error) => {
      console.error('SSE connection error:', error);
    };
    
    return eventSource;
  },

  updateNotificationPreferences: async (userId: string, preferences: NotificationPreferences): Promise<NotificationPreferences> => {
    const response = await orderApi.put<NotificationPreferences>(`/notifications/preferences?userId=${userId}`, preferences);
    return response.data;
  },

  getNotificationPreferences: async (userId: string): Promise<NotificationPreferences> => {
    const response = await orderApi.get<NotificationPreferences>(`/notifications/preferences?userId=${userId}`);
    return response.data;
  },
};

