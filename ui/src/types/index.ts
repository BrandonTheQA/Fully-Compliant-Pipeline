export interface User {
  userId: string;
  name: string;
  email: string;
  password?: string;
  createdAt?: string;
}

export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  quantity: number;
  category: string;
  createdAt?: string;
  updatedAt?: string;
  stockStatus?: 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';
}

export interface OrderItem {
  id?: number;
  productId: string;
  productName?: string;
  quantity: number;
  price?: number;
  subtotal?: number;
}

export interface Order {
  id: string;
  userId: string;
  items: OrderItem[];
  totalAmount: number;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateUserRequest {
  name: string;
  email: string;
  password: string;
}

export interface CreateProductRequest {
  name: string;
  description: string;
  price: number;
  quantity: number;
  category: string;
}

export interface CreateOrderRequest {
  userId: string;
  items: {
    productId: string;
    quantity: number;
  }[];
}

export interface CartItem extends Product {
  orderQuantity: number;
}

export interface RecommendedProduct {
  id: string;
  name: string;
  description?: string;
  price: number;
  category?: string;
  savingsMessage?: string;
  imageUrl?: string;
}

export interface OptimizationPath {
  products: RecommendedProduct[];
  totalCost: number;
  savingsAmount: number;
  message: string;
  pathType: 'single' | 'bundle' | 'category';
}

export interface RecommendationResponse {
  optimizationPaths: OptimizationPath[];
  qualifiesForFreeShipping: boolean;
  remainingAmount: number;
  region: string;
  cartTotal: number;
  freeShippingThreshold: number;
}

export interface CartModificationSuggestion {
  type: 'replace' | 'remove' | 'increase_quantity';
  explanation: string;
  currentCost: number;
  optimizedCost: number;
  savings: number;
}

export interface OrderStatusHistory {
  id: string;
  status: string;
  location?: string;
  notes?: string;
  createdAt: string;
}

export interface OrderTracking {
  orderId: string;
  status: string;
  trackingNumber?: string;
  carrierName?: string;
  estimatedDeliveryDate?: string;
  shippingAddress?: string;
  shippingMethod?: string;
  currentLocation?: string;
  statusHistory: OrderStatusHistory[];
}

export interface WishlistItem {
  id: string;
  userId: string;
  productId: string;
  createdAt: string;
}

export interface NotificationPreferences {
  id?: string;
  userId: string;
  emailEnabled: boolean;
  smsEnabled: boolean;
  phoneNumber?: string;
  notificationFrequency: 'ALL' | 'CRITICAL_ONLY' | 'NONE';
  createdAt?: string;
  updatedAt?: string;
}

export type LoyaltyTier = 'BRONZE' | 'SILVER' | 'GOLD' | 'PLATINUM';

export interface LoyaltyAccount {
  accountId: string;
  userId: string;
  currentPoints: number;
  currentTier: LoyaltyTier;
  highestTierAchieved: LoyaltyTier;
  lifetimePointsEarned: number;
  lifetimePointsRedeemed: number;
  referralCode: string;
  enrollmentDate: string;
  isActive: boolean;
}

export interface LoyaltyTransaction {
  transactionId: string;
  transactionType: 'EARNED' | 'REDEEMED' | 'EXPIRED' | 'ADMIN_ADJUSTMENT';
  points: number;
  activityType: string;
  description?: string;
  createdAt: string;
  expirationDate?: string;
}

export interface LoyaltyDashboard {
  account: LoyaltyAccount;
  recentTransactions: LoyaltyTransaction[];
  pointsToNextTier: number;
  expiringPoints?: number;
  expiringPointsDate?: string;
  tierBenefits: TierBenefits;
}

export interface TierBenefits {
  tier: LoyaltyTier;
  multiplier: number;
  benefits: string[];
  pointsToNextTier: number;
}

export interface ReferralStats {
  totalReferrals: number;
  successfulReferrals: number;
  pointsEarned: number;
  successRate: number;
}

export interface RedeemPointsRequest {
  points: number;
  orderId?: string;
  orderTotal?: number;
}

export interface RedeemPointsResponse {
  pointsRedeemed: number;
  discountAmount: number;
  remainingBalance: number;
  message: string;
}

// Return Types
export type ReturnStatus = 
  | 'PENDING_APPROVAL'
  | 'APPROVED'
  | 'REJECTED'
  | 'IN_TRANSIT'
  | 'RECEIVED'
  | 'PROCESSING_REFUND'
  | 'REFUNDED'
  | 'COMPLETED';

export type ReturnType = 
  | 'REFUND_TO_PAYMENT'
  | 'STORE_CREDIT'
  | 'EXCHANGE';

export type ReturnReason = 
  | 'DEFECTIVE'
  | 'WRONG_ITEM'
  | 'NOT_AS_DESCRIBED'
  | 'CHANGED_MIND'
  | 'SIZE_COLOR_ISSUE'
  | 'OTHER';

export interface ReturnItem {
  returnItemId?: number;
  orderItemId: number;
  productId: string;
  productName: string;
  quantity: number;
  returnReason: ReturnReason;
  condition?: string;
  comments?: string;
  originalPrice: number;
  refundAmount?: number;
}

export interface ReturnStatusHistory {
  historyId?: number;
  status: ReturnStatus;
  notes?: string;
  updatedBy?: string;
  createdAt: string;
}

export interface Return {
  returnId: string;
  orderId: string;
  userId: string;
  rmaNumber: string;
  status: ReturnStatus;
  returnType: ReturnType;
  refundAmount?: number;
  refundMethod?: string;
  refundDate?: string;
  returnTrackingNumber?: string;
  returnCarrier?: string;
  returnLabelUrl?: string;
  items: ReturnItem[];
  statusHistory: ReturnStatusHistory[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateReturnRequest {
  orderId: string;
  userId: string;
  items: {
    orderItemId: number;
    quantity: number;
    returnReason: ReturnReason;
    condition?: string;
    comments?: string;
  }[];
  returnType: ReturnType;
  comments?: string;
}

export interface ReturnTracking {
  returnId: string;
  rmaNumber: string;
  status: ReturnStatus;
  returnType: ReturnType;
  returnTrackingNumber?: string;
  returnCarrier?: string;
  returnLabelUrl?: string;
  refundAmount?: number;
  refundMethod?: string;
  refundDate?: string;
  estimatedRefundDate?: string;
  statusHistory: ReturnStatusHistory[];
  items: ReturnItem[];
}

export interface ReturnPolicy {
  returnWindowDays: number;
  restockingFeePercentage?: number;
  freeReturnThreshold?: number;
  autoApproveThreshold?: number;
}

export interface ExchangeRequest {
  exchangeProductId: string;
  quantity?: number;
  notes?: string;
}

export interface ReturnAnalytics {
  totalReturns: number;
  totalReturnValue: number;
  averageReturnProcessingTime: number;
  returnRate: number;
  returnReasonsDistribution: Record<string, number>;
  returnRateByProduct: ProductReturnRate[];
  returnsByStatus: Record<string, number>;
  returnsByMonth: MonthlyReturnStats[];
}

export interface ProductReturnRate {
  productId: string;
  productName: string;
  returnRate: number;
  totalReturns: number;
}

export interface MonthlyReturnStats {
  month: string;
  year: number;
  totalReturns: number;
  totalValue: number;
}

