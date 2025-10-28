import { orderApi } from './api';
import type { Order, CreateOrderRequest } from '../types';

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
};

