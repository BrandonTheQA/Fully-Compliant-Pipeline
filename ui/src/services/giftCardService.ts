import { userApi } from './api';

export interface GiftCard {
  giftCardId: string;
  code: string;
  amount: number;
  balance: number;
  status: string;
  purchaserId?: string;
  purchaserEmail: string;
  recipientEmail?: string;
  recipientName?: string;
  personalMessage?: string;
  design?: string;
  purchaseDate: string;
  expirationDate: string;
  scheduledDeliveryDate?: string;
}

export interface GiftCardTransaction {
  transactionId: string;
  giftCardId: string;
  transactionType: string;
  amount: number;
  orderId?: string;
  description?: string;
  createdAt: string;
}

export interface PurchaseGiftCardRequest {
  amount: number;
  quantity?: number;
  purchaserId?: string;
  purchaserEmail: string;
  recipientEmail?: string;
  recipientName?: string;
  personalMessage?: string;
  design?: string;
  scheduledDeliveryDate?: string;
}

export interface PurchaseGiftCardResponse {
  giftCards: GiftCard[];
  totalAmount: number;
}

export interface RedeemGiftCardRequest {
  code: string;
  redemptionAmount: number;
}

export interface RedeemGiftCardResponse {
  success: boolean;
  remainingBalance: number;
  appliedAmount: number;
  giftCard: GiftCard;
}

export interface ApplyGiftCardRequest {
  code: string;
  orderTotal: number;
}

export interface ApplyGiftCardResponse {
  appliedAmount: number;
  remainingBalance: number;
  orderTotal: number;
  giftCard: GiftCard;
}

export interface BalanceInquiryResponse {
  code: string;
  balance: number;
  amount: number;
  status: string;
  expirationDate: string;
}

export const giftCardService = {
  purchaseGiftCard: async (request: PurchaseGiftCardRequest): Promise<PurchaseGiftCardResponse> => {
    const response = await userApi.post<PurchaseGiftCardResponse>('/gift-cards/purchase', request);
    return response.data;
  },

  redeemGiftCard: async (request: RedeemGiftCardRequest): Promise<RedeemGiftCardResponse> => {
    const response = await userApi.post<RedeemGiftCardResponse>('/gift-cards/redeem', request);
    return response.data;
  },

  applyGiftCard: async (request: ApplyGiftCardRequest): Promise<ApplyGiftCardResponse> => {
    const response = await userApi.post<ApplyGiftCardResponse>('/gift-cards/apply', request);
    return response.data;
  },

  checkBalance: async (code: string): Promise<BalanceInquiryResponse> => {
    const response = await userApi.get<BalanceInquiryResponse>(`/gift-cards/balance/${code}`);
    return response.data;
  },

  getUserGiftCards: async (userId: string): Promise<GiftCard[]> => {
    const response = await userApi.get<GiftCard[]>(`/gift-cards/user/${userId}`);
    return response.data;
  },

  getGiftCardDetails: async (giftCardId: string): Promise<GiftCard> => {
    const response = await userApi.get<GiftCard>(`/gift-cards/${giftCardId}`);
    return response.data;
  },

  getTransactionHistory: async (giftCardId: string): Promise<GiftCardTransaction[]> => {
    const response = await userApi.get<GiftCardTransaction[]>(`/gift-cards/${giftCardId}/transactions`);
    return response.data;
  },

  resendEmail: async (giftCardId: string): Promise<void> => {
    await userApi.post(`/gift-cards/resend/${giftCardId}`);
  },
};
