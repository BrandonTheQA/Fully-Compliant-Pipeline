import { productApi } from './api';

export interface StockStatusResponse {
  productId: string;
  status: 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';
  quantity: number;
  lowStockThreshold: number | null;
  message: string;
}

export interface BulkStockStatusRequest {
  productIds: string[];
}

export interface BulkStockStatusResponse {
  statuses: StockStatusResponse[];
}

export interface NotificationSignupRequest {
  productId: string;
  email: string;
}

export interface NotificationResponse {
  notificationId: string;
  productId: string;
  productName: string;
  status: 'PENDING' | 'NOTIFIED' | 'UNSUBSCRIBED';
  signupDate: string | null;
  notifiedDate: string | null;
}

export const stockService = {
  /**
   * Get stock status for a product
   */
  getStockStatus: async (productId: string): Promise<StockStatusResponse> => {
    const response = await productApi.get<StockStatusResponse>(`/v2/products/${productId}/stock`);
    return response.data;
  },

  /**
   * Get bulk stock status for multiple products
   */
  getBulkStockStatus: async (productIds: string[]): Promise<StockStatusResponse[]> => {
    const response = await productApi.post<BulkStockStatusResponse>('/v2/products/stock/bulk', {
      productIds,
    });
    return response.data.statuses;
  },

  /**
   * Sign up for back-in-stock notifications
   */
  signUpForNotification: async (productId: string, email: string): Promise<void> => {
    await productApi.post(`/v2/products/${productId}/notify-me`, {
      productId,
      email,
    });
  },

  /**
   * Get user's notification subscriptions
   */
  getUserNotifications: async (userId: string): Promise<NotificationResponse[]> => {
    const response = await productApi.get<NotificationResponse[]>('/v2/stock/notifications', {
      params: { userId },
    });
    return response.data;
  },

  /**
   * Unsubscribe from notification
   */
  unsubscribe: async (notificationId: string): Promise<void> => {
    await productApi.delete(`/v2/stock/notifications/${notificationId}`);
  },
};

