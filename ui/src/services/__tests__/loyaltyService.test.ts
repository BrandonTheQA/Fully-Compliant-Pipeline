import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { loyaltyService } from '../loyaltyService';
import { userApi } from '../api';
import type { RedeemPointsRequest } from '../../types';

jest.mock('../api');

const mockUserApi = userApi as jest.Mocked<typeof userApi>;

describe('loyaltyService', () => {
  const mockUserId = 'user123';
  
  beforeEach(() => {
    jest.clearAllMocks();
  });
  
  describe('getBalance', () => {
    it('should fetch loyalty balance', async () => {
      const mockBalance = {
        accountId: 'acc123',
        userId: mockUserId,
        currentPoints: 1000,
        currentTier: 'BRONZE' as const,
        highestTierAchieved: 'BRONZE' as const,
        lifetimePointsEarned: 1000,
        lifetimePointsRedeemed: 0,
        referralCode: 'REF123',
        enrollmentDate: '2024-01-01',
        isActive: true,
      };
      
      mockUserApi.get.mockResolvedValue({ data: mockBalance });
      
      const result = await loyaltyService.getBalance(mockUserId);
      
      expect(mockUserApi.get).toHaveBeenCalledWith('/loyalty/balance', {
        params: { userId: mockUserId }
      });
      expect(result).toEqual(mockBalance);
    });
  });
  
  describe('getDashboard', () => {
    it('should fetch loyalty dashboard', async () => {
      const mockDashboard = {
        account: {
          currentPoints: 1000,
          currentTier: 'BRONZE' as const,
        highestTierAchieved: 'BRONZE' as const,
        },
        recentTransactions: [],
        pointsToNextTier: 0,
      };
      
      mockUserApi.get.mockResolvedValue({ data: mockDashboard });
      
      const result = await loyaltyService.getDashboard(mockUserId);
      
      expect(mockUserApi.get).toHaveBeenCalledWith('/loyalty/dashboard', {
        params: { userId: mockUserId }
      });
      expect(result).toEqual(mockDashboard);
    });
  });

  describe('getHistory', () => {
    it('should fetch loyalty history with default pagination', async () => {
      const mockHistory = {
        transactions: [],
        totalElements: 0,
        totalPages: 0,
        currentPage: 0,
        pageSize: 20,
      };
      
      mockUserApi.get.mockResolvedValue({ data: mockHistory });
      
      const result = await loyaltyService.getHistory(mockUserId);
      
      expect(mockUserApi.get).toHaveBeenCalledWith('/loyalty/history', {
        params: { userId: mockUserId, page: 0, size: 20 }
      });
      expect(result).toEqual(mockHistory);
    });

    it('should fetch loyalty history with custom pagination', async () => {
      const mockHistory = {
        transactions: [],
        totalElements: 100,
        totalPages: 5,
        currentPage: 2,
        pageSize: 20,
      };
      
      mockUserApi.get.mockResolvedValue({ data: mockHistory });
      
      const result = await loyaltyService.getHistory(mockUserId, 2, 10);
      
      expect(mockUserApi.get).toHaveBeenCalledWith('/loyalty/history', {
        params: { userId: mockUserId, page: 2, size: 10 }
      });
      expect(result).toEqual(mockHistory);
    });
  });
  
  describe('redeemPoints', () => {
    it('should redeem points', async () => {
      const mockRequest: RedeemPointsRequest = {
        points: 500,
        orderId: 'order123',
        orderTotal: 100.0,
      };
      
      const mockResponse = {
        pointsRedeemed: 500,
        discountAmount: 5.0,
        remainingBalance: 500,
        message: 'Success',
      };
      
      mockUserApi.post.mockResolvedValue({ data: mockResponse });
      
      const result = await loyaltyService.redeemPoints(mockUserId, mockRequest);
      
      expect(mockUserApi.post).toHaveBeenCalledWith(
        '/loyalty/redeem',
        mockRequest,
        { params: { userId: mockUserId } }
      );
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getReferralCode', () => {
    it('should fetch referral code', async () => {
      const mockReferralCode = {
        referralCode: 'REF123',
        referralLink: 'https://example.com/ref/REF123',
      };
      
      mockUserApi.get.mockResolvedValue({ data: mockReferralCode });
      
      const result = await loyaltyService.getReferralCode(mockUserId);
      
      expect(mockUserApi.get).toHaveBeenCalledWith('/loyalty/referral-code', {
        params: { userId: mockUserId }
      });
      expect(result).toEqual(mockReferralCode);
    });
  });

  describe('getReferralStats', () => {
    it('should fetch referral stats', async () => {
      const mockStats = {
        totalReferrals: 10,
        successfulReferrals: 8,
        pointsEarned: 500,
        successRate: 80.0,
      };
      
      mockUserApi.get.mockResolvedValue({ data: mockStats });
      
      const result = await loyaltyService.getReferralStats(mockUserId);
      
      expect(mockUserApi.get).toHaveBeenCalledWith('/loyalty/referral-stats', {
        params: { userId: mockUserId }
      });
      expect(result).toEqual(mockStats);
    });
  });

  describe('enroll', () => {
    it('should enroll user without referral code', async () => {
      const mockAccount = {
        accountId: 'acc123',
        userId: mockUserId,
        currentPoints: 0,
        currentTier: 'BRONZE' as const,
        highestTierAchieved: 'BRONZE' as const,
        lifetimePointsEarned: 0,
        lifetimePointsRedeemed: 0,
        referralCode: 'REF123',
        enrollmentDate: '2024-01-01',
        isActive: true,
      };
      
      mockUserApi.post.mockResolvedValue({ data: mockAccount });
      
      const result = await loyaltyService.enroll(mockUserId);
      
      expect(mockUserApi.post).toHaveBeenCalledWith(
        '/loyalty/enroll',
        undefined,
        { params: { userId: mockUserId } }
      );
      expect(result).toEqual(mockAccount);
    });

    it('should enroll user with referral code', async () => {
      const mockAccount = {
        accountId: 'acc123',
        userId: mockUserId,
        currentPoints: 100,
        currentTier: 'BRONZE' as const,
        highestTierAchieved: 'BRONZE' as const,
        lifetimePointsEarned: 100,
        lifetimePointsRedeemed: 0,
        referralCode: 'REF123',
        enrollmentDate: '2024-01-01',
        isActive: true,
      };
      
      mockUserApi.post.mockResolvedValue({ data: mockAccount });
      
      const result = await loyaltyService.enroll(mockUserId, 'FRIEND123');
      
      expect(mockUserApi.post).toHaveBeenCalledWith(
        '/loyalty/enroll',
        { referralCode: 'FRIEND123' },
        { params: { userId: mockUserId } }
      );
      expect(result).toEqual(mockAccount);
    });
  });

  describe('optOut', () => {
    it('should opt out user from loyalty program', async () => {
      mockUserApi.post.mockResolvedValue({ data: null as any });
      
      await loyaltyService.optOut(mockUserId);
      
      expect(mockUserApi.post).toHaveBeenCalledWith(
        '/loyalty/opt-out',
        null,
        { params: { userId: mockUserId } }
      );
    });
  });

  describe('getTierBenefits', () => {
    it('should fetch tier benefits', async () => {
      const mockBenefits = {
        multiplier: 1.0,
        benefits: ['Free shipping', 'Early access'],
      };
      
      mockUserApi.get.mockResolvedValue({ data: mockBenefits });
      
      const result = await loyaltyService.getTierBenefits(mockUserId);
      
      expect(mockUserApi.get).toHaveBeenCalledWith('/loyalty/tier-benefits', {
        params: { userId: mockUserId }
      });
      expect(result).toEqual(mockBenefits);
    });
  });
});
