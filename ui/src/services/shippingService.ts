import { productApi } from './api';

export interface ShippingThresholdResponse {
  region: string;
  freeShippingThreshold: number;
  currentCartTotal: number;
  remainingAmount: number;
  qualifiesForFreeShipping: boolean;
}

const CACHE_KEY_PREFIX = 'shipping_threshold_';
const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes

interface CachedThreshold {
  data: ShippingThresholdResponse;
  timestamp: number;
}

export const shippingService = {
  /**
   * Get shipping threshold information for a region
   * Caches the result in sessionStorage to minimize API calls
   * 
   * @param cartTotal Current cart total amount
   * @param region Optional region code (e.g., "US", "CA"). If not provided, auto-detected by backend
   * @returns Shipping threshold information
   */
  getShippingThreshold: async (
    cartTotal: number = 0,
    region?: string
  ): Promise<ShippingThresholdResponse> => {
    // Create cache key based on region
    const cacheKey = `${CACHE_KEY_PREFIX}${region || 'default'}`;
    
    // Try to get from cache first
    const cached = sessionStorage.getItem(cacheKey);
    if (cached) {
      try {
        const cachedData: CachedThreshold = JSON.parse(cached);
        const now = Date.now();
        
        // Check if cache is still valid (within 5 minutes)
        if (now - cachedData.timestamp < CACHE_DURATION) {
          // Return cached data but update cart total
          return {
            ...cachedData.data,
            currentCartTotal: cartTotal,
            remainingAmount: Math.max(0, cachedData.data.freeShippingThreshold - cartTotal),
            qualifiesForFreeShipping: cartTotal >= cachedData.data.freeShippingThreshold,
          };
        }
      } catch (e) {
        // If cache is corrupted, continue to fetch from API
      }
    }
    
      // Fetch from API
      try {
        const params: Record<string, string> = {};
        if (cartTotal >= 0) {
          params.cartTotal = cartTotal.toString();
        }
        if (region) {
          params.region = region;
        }
        
        const queryString = new URLSearchParams(params).toString();
        const url = `/shipping/threshold${queryString ? `?${queryString}` : ''}`;
      
      const response = await productApi.get<ShippingThresholdResponse>(url);
      const data = response.data;
      
      // Cache the threshold (but not the cart total, as that changes frequently)
      const thresholdToCache: ShippingThresholdResponse = {
        ...data,
        currentCartTotal: 0, // Don't cache cart total
        remainingAmount: data.freeShippingThreshold, // Cache with full threshold
        qualifiesForFreeShipping: false,
      };
      
      sessionStorage.setItem(
        cacheKey,
        JSON.stringify({
          data: thresholdToCache,
          timestamp: Date.now(),
        })
      );
      
      return data;
    } catch (error) {
      // Fallback to default threshold if API call fails
      const fallbackThreshold: ShippingThresholdResponse = {
        region: region || 'US',
        freeShippingThreshold: 50.00,
        currentCartTotal: cartTotal,
        remainingAmount: Math.max(0, 50.00 - cartTotal),
        qualifiesForFreeShipping: cartTotal >= 50.00,
      };
      
      return fallbackThreshold;
    }
  },
  
  /**
   * Clear cached shipping threshold data
   */
  clearCache: (region?: string) => {
    if (region) {
      sessionStorage.removeItem(`${CACHE_KEY_PREFIX}${region}`);
    } else {
      // Clear all shipping cache entries
      Object.keys(sessionStorage).forEach((key) => {
        if (key.startsWith(CACHE_KEY_PREFIX)) {
          sessionStorage.removeItem(key);
        }
      });
    }
  },
};

