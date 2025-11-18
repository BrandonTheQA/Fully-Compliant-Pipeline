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
}

export interface OrderItem {
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

