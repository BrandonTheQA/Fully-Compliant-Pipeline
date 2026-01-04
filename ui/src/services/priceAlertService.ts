import { productApi } from './api';

export interface PriceAlert {
  alertId: string;
  productId: string;
  userEmail: string;
  userId?: string;
  targetPrice?: number;
  currentPrice: number;
  notificationFrequency: string;
  status: string;
  createdAt: string;
  lastTriggeredAt?: string;
  updatedAt: string;
}

export interface PriceHistory {
  priceHistoryId: string;
  productId: string;
  price: number;
  previousPrice?: number;
  changeType: string;
  changePercentage?: number;
  changedAt: string;
}

export interface CreatePriceAlertRequest {
  productId: string;
  email: string;
  targetPrice?: number;
  notificationFrequency?: string;
  userId?: string;
}

export interface UpdatePriceAlertRequest {
  targetPrice?: number;
  notificationFrequency?: string;
  status?: string;
}

export interface PriceAlertListResponse {
  alerts: PriceAlert[];
}

export const priceAlertService = {
  createAlert: async (request: CreatePriceAlertRequest): Promise<PriceAlert> => {
    const response = await productApi.post<PriceAlert>('/v2/price-alerts', request);
    return response.data;
  },

  getAlerts: async (email?: string, userId?: string): Promise<PriceAlert[]> => {
    const params = new URLSearchParams();
    if (email) params.append('email', email);
    if (userId) params.append('userId', userId);
    const queryString = params.toString();
    const url = `/v2/price-alerts${queryString ? `?${queryString}` : ''}`;
    const response = await productApi.get<PriceAlertListResponse>(url);
    return response.data.alerts;
  },

  getAlert: async (alertId: string): Promise<PriceAlert> => {
    const response = await productApi.get<PriceAlert>(`/v2/price-alerts/${alertId}`);
    return response.data;
  },

  updateAlert: async (alertId: string, request: UpdatePriceAlertRequest): Promise<PriceAlert> => {
    const response = await productApi.put<PriceAlert>(`/v2/price-alerts/${alertId}`, request);
    return response.data;
  },

  deleteAlert: async (alertId: string): Promise<void> => {
    await productApi.delete(`/v2/price-alerts/${alertId}`);
  },

  getPriceHistory: async (alertId: string): Promise<PriceHistory[]> => {
    const response = await productApi.get<PriceHistory[]>(`/v2/price-alerts/${alertId}/history`);
    return response.data;
  },
};

