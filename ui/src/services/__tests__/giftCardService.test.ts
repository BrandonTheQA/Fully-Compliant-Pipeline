import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { giftCardService } from '../giftCardService';
import { userApi } from '../api';

jest.mock('../api');

const mockUserApi = userApi as jest.Mocked<typeof userApi>;

describe('giftCardService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });
  
  describe('purchaseGiftCard', () => {
    it('should purchase a gift card', async () => {
      const mockRequest = {
        amount: 100,
        quantity: 1,
        purchaserEmail: 'purchaser@example.com',
      };
      
      const mockResponse = {
        giftCards: [{
          giftCardId: 'gc123',
          code: 'ABCD-EFGH-IJKL-MNOP',
          amount: 100,
          balance: 100,
          status: 'ACTIVE',
          purchaserEmail: 'purchaser@example.com',
          purchaseDate: '2024-01-01T00:00:00',
          expirationDate: '2025-01-01T00:00:00',
        }],
        totalAmount: 100,
      };
      
      mockUserApi.post.mockResolvedValue({ data: mockResponse });
      
      const result = await giftCardService.purchaseGiftCard(mockRequest);
      
      expect(mockUserApi.post).toHaveBeenCalledWith('/gift-cards/purchase', mockRequest);
      expect(result).toEqual(mockResponse);
    });
  });
  
  describe('redeemGiftCard', () => {
    it('should redeem a gift card', async () => {
      const mockRequest = {
        code: 'ABCD-EFGH-IJKL-MNOP',
        redemptionAmount: 50,
      };
      
      const mockResponse = {
        success: true,
        remainingBalance: 50,
        appliedAmount: 50,
        giftCard: {
          giftCardId: 'gc123',
          code: 'ABCD-EFGH-IJKL-MNOP',
          balance: 50,
          status: 'ACTIVE',
        },
      };
      
      mockUserApi.post.mockResolvedValue({ data: mockResponse });
      
      const result = await giftCardService.redeemGiftCard(mockRequest);
      
      expect(mockUserApi.post).toHaveBeenCalledWith('/gift-cards/redeem', mockRequest);
      expect(result).toEqual(mockResponse);
    });
  });
  
  describe('applyGiftCard', () => {
    it('should apply a gift card to order', async () => {
      const mockRequest = {
        code: 'ABCD-EFGH-IJKL-MNOP',
        orderTotal: 150,
      };
      
      const mockResponse = {
        appliedAmount: 100,
        remainingBalance: 0,
        orderTotal: 50,
        giftCard: {
          giftCardId: 'gc123',
          code: 'ABCD-EFGH-IJKL-MNOP',
          balance: 0,
        },
      };
      
      mockUserApi.post.mockResolvedValue({ data: mockResponse });
      
      const result = await giftCardService.applyGiftCard(mockRequest);
      
      expect(mockUserApi.post).toHaveBeenCalledWith('/gift-cards/apply', mockRequest);
      expect(result).toEqual(mockResponse);
    });
  });
  
  describe('checkBalance', () => {
    it('should check gift card balance', async () => {
      const mockCode = 'ABCD-EFGH-IJKL-MNOP';
      const mockResponse = {
        code: mockCode,
        balance: 100,
        amount: 100,
        status: 'ACTIVE',
        expirationDate: '2025-01-01T00:00:00',
      };
      
      mockUserApi.get.mockResolvedValue({ data: mockResponse });
      
      const result = await giftCardService.checkBalance(mockCode);
      
      expect(mockUserApi.get).toHaveBeenCalledWith(`/gift-cards/balance/${mockCode}`);
      expect(result).toEqual(mockResponse);
    });
  });
  
  describe('getUserGiftCards', () => {
    it('should get user gift cards', async () => {
      const mockUserId = 'user123';
      const mockResponse = [{
        giftCardId: 'gc123',
        code: 'ABCD-EFGH-IJKL-MNOP',
        amount: 100,
        balance: 100,
        status: 'ACTIVE',
      }];
      
      mockUserApi.get.mockResolvedValue({ data: mockResponse });
      
      const result = await giftCardService.getUserGiftCards(mockUserId);
      
      expect(mockUserApi.get).toHaveBeenCalledWith(`/gift-cards/user/${mockUserId}`);
      expect(result).toEqual(mockResponse);
    });
  });
  
  describe('getGiftCardDetails', () => {
    it('should get gift card details', async () => {
      const mockGiftCardId = 'gc123';
      const mockResponse = {
        giftCardId: mockGiftCardId,
        code: 'ABCD-EFGH-IJKL-MNOP',
        amount: 100,
        balance: 100,
        status: 'ACTIVE',
      };
      
      mockUserApi.get.mockResolvedValue({ data: mockResponse });
      
      const result = await giftCardService.getGiftCardDetails(mockGiftCardId);
      
      expect(mockUserApi.get).toHaveBeenCalledWith(`/gift-cards/${mockGiftCardId}`);
      expect(result).toEqual(mockResponse);
    });
  });
  
  describe('getTransactionHistory', () => {
    it('should get transaction history', async () => {
      const mockGiftCardId = 'gc123';
      const mockResponse = [{
        transactionId: 'tx123',
        giftCardId: mockGiftCardId,
        transactionType: 'REDEMPTION',
        amount: 50,
        createdAt: '2024-01-01T00:00:00',
      }];
      
      mockUserApi.get.mockResolvedValue({ data: mockResponse });
      
      const result = await giftCardService.getTransactionHistory(mockGiftCardId);
      
      expect(mockUserApi.get).toHaveBeenCalledWith(`/gift-cards/${mockGiftCardId}/transactions`);
      expect(result).toEqual(mockResponse);
    });
  });
  
  describe('resendEmail', () => {
    it('should resend gift card email', async () => {
      const mockGiftCardId = 'gc123';
      
      mockUserApi.post.mockResolvedValue({ data: null });
      
      await giftCardService.resendEmail(mockGiftCardId);
      
      expect(mockUserApi.post).toHaveBeenCalledWith(`/gift-cards/resend/${mockGiftCardId}`);
    });
  });
});
