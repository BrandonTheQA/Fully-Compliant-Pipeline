import { orderApi } from './api';
import type { 
  Return, 
  CreateReturnRequest, 
  ReturnTracking, 
  ReturnPolicy, 
  ExchangeRequest,
  ReturnAnalytics,
  Order
} from '../types';

export const returnService = {
  createReturn: async (returnData: CreateReturnRequest): Promise<Return> => {
    const response = await orderApi.post<Return>('/returns', returnData);
    return response.data;
  },

  getReturnByRMA: async (rmaNumber: string): Promise<Return> => {
    const response = await orderApi.get<Return>(`/returns/rma/${rmaNumber}`);
    return response.data;
  },

  getUserReturns: async (userId: string): Promise<Return[]> => {
    const response = await orderApi.get<Return[]>(`/returns/user/${userId}`);
    return response.data;
  },

  getReturnTracking: async (returnId: string): Promise<ReturnTracking> => {
    const response = await orderApi.get<ReturnTracking>(`/returns/${returnId}/tracking`);
    return response.data;
  },

  getReturnPolicy: async (): Promise<ReturnPolicy> => {
    const response = await orderApi.get<ReturnPolicy>('/returns/policy');
    return response.data;
  },

  createExchange: async (returnId: string, exchangeData: ExchangeRequest): Promise<Order> => {
    const response = await orderApi.post<Order>(`/returns/${returnId}/exchange`, exchangeData);
    return response.data;
  },

  // Admin endpoints
  getAdminReturns: async (params?: {
    status?: string;
    userId?: string;
    orderId?: string;
    rmaNumber?: string;
  }): Promise<Return[]> => {
    const queryParams = new URLSearchParams();
    if (params?.status) queryParams.append('status', params.status);
    if (params?.userId) queryParams.append('userId', params.userId);
    if (params?.orderId) queryParams.append('orderId', params.orderId);
    if (params?.rmaNumber) queryParams.append('rmaNumber', params.rmaNumber);
    
    const queryString = queryParams.toString();
    const url = `/admin/returns${queryString ? `?${queryString}` : ''}`;
    const response = await orderApi.get<Return[]>(url);
    return response.data;
  },

  getAdminReturn: async (returnId: string): Promise<Return> => {
    const response = await orderApi.get<Return>(`/admin/returns/${returnId}`);
    return response.data;
  },

  approveReturn: async (returnId: string, notes?: string, approvedBy?: string): Promise<void> => {
    const params = new URLSearchParams();
    if (approvedBy) params.append('approvedBy', approvedBy);
    
    const queryString = params.toString();
    const url = `/admin/returns/${returnId}/approve${queryString ? `?${queryString}` : ''}`;
    await orderApi.post(url, notes ? { notes } : {});
  },

  rejectReturn: async (returnId: string, reason: string, rejectedBy?: string): Promise<void> => {
    const params = new URLSearchParams();
    params.append('reason', reason);
    if (rejectedBy) params.append('rejectedBy', rejectedBy);
    
    const url = `/admin/returns/${returnId}/reject?${params.toString()}`;
    await orderApi.post(url);
  },

  updateReturnStatus: async (
    returnId: string, 
    status: string, 
    notes?: string, 
    updatedBy?: string
  ): Promise<void> => {
    const params = new URLSearchParams();
    if (updatedBy) params.append('updatedBy', updatedBy);
    
    const queryString = params.toString();
    const url = `/admin/returns/${returnId}/status${queryString ? `?${queryString}` : ''}`;
    await orderApi.put(url, { status, notes });
  },

  markReturnReceived: async (
    returnId: string, 
    notes?: string, 
    receivedBy?: string
  ): Promise<void> => {
    const params = new URLSearchParams();
    if (notes) params.append('notes', notes);
    if (receivedBy) params.append('receivedBy', receivedBy);
    
    const url = `/admin/returns/${returnId}/received${params.toString() ? `?${params.toString()}` : ''}`;
    await orderApi.post(url);
  },

  processRefund: async (returnId: string, processedBy?: string): Promise<void> => {
    const params = new URLSearchParams();
    if (processedBy) params.append('processedBy', processedBy);
    
    const url = `/admin/returns/${returnId}/refund${params.toString() ? `?${params.toString()}` : ''}`;
    await orderApi.post(url);
  },

  getAnalytics: async (): Promise<ReturnAnalytics> => {
    const response = await orderApi.get<ReturnAnalytics>('/admin/returns/analytics');
    return response.data;
  },
};

