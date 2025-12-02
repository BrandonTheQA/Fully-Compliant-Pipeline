import { userApi } from './api';
import type { 
  LoyaltyAccount, 
  LoyaltyDashboard, 
  LoyaltyTransaction, 
  ReferralStats, 
  TierBenefits,
  RedeemPointsRequest,
  RedeemPointsResponse
} from '../types';

export const loyaltyService = {
  getBalance: async (userId: string): Promise<LoyaltyAccount> => {
    const response = await userApi.get<LoyaltyAccount>('/loyalty/balance', {
      params: { userId }
    });
    return response.data;
  },

  getDashboard: async (userId: string): Promise<LoyaltyDashboard> => {
    const response = await userApi.get<LoyaltyDashboard>('/loyalty/dashboard', {
      params: { userId }
    });
    return response.data;
  },

  getHistory: async (userId: string, page: number = 0, size: number = 20): Promise<{
    transactions: LoyaltyTransaction[];
    totalElements: number;
    totalPages: number;
    currentPage: number;
    pageSize: number;
  }> => {
    const response = await userApi.get('/loyalty/history', {
      params: { userId, page, size }
    });
    return response.data;
  },

  redeemPoints: async (userId: string, request: RedeemPointsRequest): Promise<RedeemPointsResponse> => {
    const response = await userApi.post<RedeemPointsResponse>('/loyalty/redeem', request, {
      params: { userId }
    });
    return response.data;
  },

  getReferralCode: async (userId: string): Promise<{ referralCode: string; referralLink: string }> => {
    const response = await userApi.get('/loyalty/referral-code', {
      params: { userId }
    });
    return response.data;
  },

  getReferralStats: async (userId: string): Promise<ReferralStats> => {
    const response = await userApi.get<ReferralStats>('/loyalty/referral-stats', {
      params: { userId }
    });
    return response.data;
  },

  enroll: async (userId: string, referralCode?: string): Promise<LoyaltyAccount> => {
    const response = await userApi.post<LoyaltyAccount>('/loyalty/enroll', 
      referralCode ? { referralCode } : undefined,
      { params: { userId } }
    );
    return response.data;
  },

  optOut: async (userId: string): Promise<void> => {
    await userApi.post('/loyalty/opt-out', null, {
      params: { userId }
    });
  },

  getTierBenefits: async (userId: string): Promise<TierBenefits> => {
    const response = await userApi.get<TierBenefits>('/loyalty/tier-benefits', {
      params: { userId }
    });
    return response.data;
  },
};
