import { loyaltyService } from '../loyaltyService';
import { userApi } from '../api';

jest.mock('../api');

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
        currentTier: 'BRONZE',
        lifetimePointsEarned: 1000,
        lifetimePointsRedeemed: 0,
        referralCode: 'REF123',
        enrollmentDate: '2024-01-01',
        isActive: true,
      };
      
      (userApi.get as jest.Mock).mockResolvedValue({ data: mockBalance });
      
      const result = await loyaltyService.getBalance(mockUserId);
      
      expect(userApi.get).toHaveBeenCalledWith('/loyalty/balance', {
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
          currentTier: 'BRONZE',
        },
        recentTransactions: [],
        pointsToNextTier: 0,
      };
      
      (userApi.get as jest.Mock).mockResolvedValue({ data: mockDashboard });
      
      const result = await loyaltyService.getDashboard(mockUserId);
      
      expect(userApi.get).toHaveBeenCalledWith('/loyalty/dashboard', {
        params: { userId: mockUserId }
      });
      expect(result).toEqual(mockDashboard);
    });
  });
  
  describe('redeemPoints', () => {
    it('should redeem points', async () => {
      const mockRequest = {
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
      
      (userApi.post as jest.Mock).mockResolvedValue({ data: mockResponse });
      
      const result = await loyaltyService.redeemPoints(mockUserId, mockRequest);
      
      expect(userApi.post).toHaveBeenCalledWith(
        '/loyalty/redeem',
        mockRequest,
        { params: { userId: mockUserId } }
      );
      expect(result).toEqual(mockResponse);
    });
  });
});
