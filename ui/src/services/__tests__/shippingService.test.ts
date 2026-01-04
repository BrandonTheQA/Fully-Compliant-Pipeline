/// <reference types="@testing-library/jest-dom" />
import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { shippingService } from '../shippingService';
import { productApi } from '../api';

jest.mock('../api');

const mockProductApi = productApi as jest.Mocked<typeof productApi>;

describe('shippingService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    sessionStorage.clear();
  });

  it('should fetch shipping threshold from API', async () => {
    const mockResponse = {
      region: 'US',
      freeShippingThreshold: 50.00,
      currentCartTotal: 35.00,
      remainingAmount: 15.00,
      qualifiesForFreeShipping: false,
    };

    mockProductApi.get.mockResolvedValue({ data: mockResponse } as any);

    const result = await shippingService.getShippingThreshold(35.00, 'US');

    expect(result).toEqual(mockResponse);
    expect(mockProductApi.get).toHaveBeenCalledWith('/shipping/threshold?cartTotal=35&region=US');
  });

  it('should cache threshold data in sessionStorage', async () => {
    const mockResponse = {
      region: 'US',
      freeShippingThreshold: 50.00,
      currentCartTotal: 0,
      remainingAmount: 50.00,
      qualifiesForFreeShipping: false,
    };

    mockProductApi.get.mockResolvedValue({ data: mockResponse } as any);

    await shippingService.getShippingThreshold(0, 'US');

    const cached = sessionStorage.getItem('shipping_threshold_US');
    expect(cached).toBeTruthy();

    const cachedData = JSON.parse(cached!);
    expect(cachedData.data.region).toBe('US');
    expect(cachedData.data.freeShippingThreshold).toBe(50.00);
  });

  it('should return cached data when available and valid', async () => {
    const cachedData = {
      data: {
        region: 'US',
        freeShippingThreshold: 50.00,
        currentCartTotal: 0,
        remainingAmount: 50.00,
        qualifiesForFreeShipping: false,
      },
      timestamp: Date.now() - 1000, // 1 second ago
    };

    sessionStorage.setItem('shipping_threshold_US', JSON.stringify(cachedData));

    const result = await shippingService.getShippingThreshold(35.00, 'US');

    expect(result.currentCartTotal).toBe(35.00);
    expect(result.remainingAmount).toBe(15.00);
    expect(result.qualifiesForFreeShipping).toBe(false);
    // Should not call API when cache is valid
    expect(mockProductApi.get).not.toHaveBeenCalled();
  });

  it('should fetch from API when cache is expired', async () => {
    const cachedData = {
      data: {
        region: 'US',
        freeShippingThreshold: 50.00,
        currentCartTotal: 0,
        remainingAmount: 50.00,
        qualifiesForFreeShipping: false,
      },
      timestamp: Date.now() - 6 * 60 * 1000, // 6 minutes ago (expired)
    };

    sessionStorage.setItem('shipping_threshold_US', JSON.stringify(cachedData));

    const mockResponse = {
      region: 'US',
      freeShippingThreshold: 50.00,
      currentCartTotal: 35.00,
      remainingAmount: 15.00,
      qualifiesForFreeShipping: false,
    };

    mockProductApi.get.mockResolvedValue({ data: mockResponse } as any);

    const result = await shippingService.getShippingThreshold(35.00, 'US');

    expect(result).toEqual(mockResponse);
    expect(mockProductApi.get).toHaveBeenCalled();
  });

  it('should return fallback values when API call fails', async () => {
    mockProductApi.get.mockRejectedValue(new Error('API Error'));

    const result = await shippingService.getShippingThreshold(35.00, 'US');

    expect(result.region).toBe('US');
    expect(result.freeShippingThreshold).toBe(50.00);
    expect(result.currentCartTotal).toBe(35.00);
    expect(result.remainingAmount).toBe(15.00);
    expect(result.qualifiesForFreeShipping).toBe(false);
  });

  it('should auto-detect region when not provided', async () => {
    const mockResponse = {
      region: 'US',
      freeShippingThreshold: 50.00,
      currentCartTotal: 0,
      remainingAmount: 50.00,
      qualifiesForFreeShipping: false,
    };

    mockProductApi.get.mockResolvedValue({ data: mockResponse } as any);

    await shippingService.getShippingThreshold(0);

    // When cartTotal is 0, it may or may not be included in the query string
    expect(mockProductApi.get).toHaveBeenCalled();
    const callArgs = (mockProductApi.get as jest.Mock).mock.calls[0][0];
    expect(callArgs).toContain('/shipping/threshold');
  });

  it('should clear cache for specific region', () => {
    sessionStorage.setItem('shipping_threshold_US', 'test');
    sessionStorage.setItem('shipping_threshold_CA', 'test');

    shippingService.clearCache('US');

    expect(sessionStorage.getItem('shipping_threshold_US')).toBeNull();
    expect(sessionStorage.getItem('shipping_threshold_CA')).toBeTruthy();
  });

  it('should clear all cache when no region specified', () => {
    sessionStorage.setItem('shipping_threshold_US', 'test');
    sessionStorage.setItem('shipping_threshold_CA', 'test');
    sessionStorage.setItem('other_key', 'test');

    shippingService.clearCache();

    expect(sessionStorage.getItem('shipping_threshold_US')).toBeNull();
    expect(sessionStorage.getItem('shipping_threshold_CA')).toBeNull();
    expect(sessionStorage.getItem('other_key')).toBeTruthy();
  });

  it('should handle corrupted cache gracefully', async () => {
    sessionStorage.setItem('shipping_threshold_US', 'invalid json');

    const mockResponse = {
      region: 'US',
      freeShippingThreshold: 50.00,
      currentCartTotal: 0,
      remainingAmount: 50.00,
      qualifiesForFreeShipping: false,
    };

    mockProductApi.get.mockResolvedValue({ data: mockResponse } as any);

    const result = await shippingService.getShippingThreshold(0, 'US');

    expect(result).toEqual(mockResponse);
    expect(mockProductApi.get).toHaveBeenCalled();
  });

  describe('Product-level shipping cost calls', () => {
    it('should calculate shipping cost for product price', async () => {
      const mockResponse = {
        region: 'US',
        cartTotal: 35.00,
        shippingCost: 9.99,
        freeShippingThreshold: 50.00,
        remainingAmount: 15.00,
        qualifiesForFreeShipping: false,
        defaultShippingCost: 9.99,
      };

      mockProductApi.get.mockResolvedValue({ data: mockResponse } as any);

      const result = await shippingService.getShippingCost(35.00, 'US');

      expect(result).toEqual(mockResponse);
      expect(mockProductApi.get).toHaveBeenCalledWith('/shipping/cost?cartTotal=35&region=US');
    });

    it('should return free shipping for product above threshold', async () => {
      const mockResponse = {
        region: 'US',
        cartTotal: 55.00,
        shippingCost: 0,
        freeShippingThreshold: 50.00,
        remainingAmount: 0,
        qualifiesForFreeShipping: true,
        defaultShippingCost: 9.99,
      };

      mockProductApi.get.mockResolvedValue({ data: mockResponse } as any);

      const result = await shippingService.getShippingCost(55.00, 'US');

      expect(result.qualifiesForFreeShipping).toBe(true);
      expect(result.shippingCost).toBe(0);
    });

    it('should handle product at exact threshold', async () => {
      const mockResponse = {
        region: 'US',
        cartTotal: 50.00,
        shippingCost: 0,
        freeShippingThreshold: 50.00,
        remainingAmount: 0,
        qualifiesForFreeShipping: true,
        defaultShippingCost: 9.99,
      };

      mockProductApi.get.mockResolvedValue({ data: mockResponse } as any);

      const result = await shippingService.getShippingCost(50.00, 'US');

      expect(result.qualifiesForFreeShipping).toBe(true);
      expect(result.shippingCost).toBe(0);
    });

    it('should handle different regions for product shipping', async () => {
      const mockResponseCA = {
        region: 'CA',
        cartTotal: 35.00,
        shippingCost: 12.99,
        freeShippingThreshold: 75.00,
        remainingAmount: 40.00,
        qualifiesForFreeShipping: false,
        defaultShippingCost: 12.99,
      };

      mockProductApi.get.mockResolvedValue({ data: mockResponseCA } as any);

      const result = await shippingService.getShippingCost(35.00, 'CA');

      expect(result.region).toBe('CA');
      expect(result.freeShippingThreshold).toBe(75.00);
      expect(mockProductApi.get).toHaveBeenCalledWith('/shipping/cost?cartTotal=35&region=CA');
    });

    it('should use cached cost data when available and valid', async () => {
      const cachedData = {
        data: {
          region: 'US',
          cartTotal: 0,
          shippingCost: 9.99,
          freeShippingThreshold: 50.00,
          remainingAmount: 50.00,
          qualifiesForFreeShipping: false,
          defaultShippingCost: 9.99,
        },
        timestamp: Date.now() - 1000, // 1 second ago
      };

      sessionStorage.setItem('shipping_cost_US', JSON.stringify(cachedData));

      const result = await shippingService.getShippingCost(35.00, 'US');

      expect(result.cartTotal).toBe(35.00);
      expect(result.shippingCost).toBe(9.99); // Uses defaultShippingCost from cache
      expect(result.remainingAmount).toBe(15.00);
      expect(result.qualifiesForFreeShipping).toBe(false);
      expect(mockProductApi.get).not.toHaveBeenCalled();
    });

    it('should recalculate shipping cost from cache when cart qualifies for free shipping', async () => {
      const cachedData = {
        data: {
          region: 'US',
          cartTotal: 0,
          shippingCost: 9.99,
          freeShippingThreshold: 50.00,
          remainingAmount: 50.00,
          qualifiesForFreeShipping: false,
          defaultShippingCost: 9.99,
        },
        timestamp: Date.now() - 1000,
      };

      sessionStorage.setItem('shipping_cost_US', JSON.stringify(cachedData));

      const result = await shippingService.getShippingCost(55.00, 'US');

      expect(result.cartTotal).toBe(55.00);
      expect(result.shippingCost).toBe(0); // Should be 0 when qualifying
      expect(result.qualifiesForFreeShipping).toBe(true);
      expect(mockProductApi.get).not.toHaveBeenCalled();
    });

    it('should fetch from API when cached cost has invalid threshold', async () => {
      const cachedData = {
        data: {
          region: 'US',
          cartTotal: 0,
          shippingCost: 9.99,
          freeShippingThreshold: 0, // Invalid threshold
          remainingAmount: 0,
          qualifiesForFreeShipping: false,
          defaultShippingCost: 9.99,
        },
        timestamp: Date.now() - 1000,
      };

      sessionStorage.setItem('shipping_cost_US', JSON.stringify(cachedData));

      const mockResponse = {
        region: 'US',
        cartTotal: 35.00,
        shippingCost: 9.99,
        freeShippingThreshold: 50.00,
        remainingAmount: 15.00,
        qualifiesForFreeShipping: false,
        defaultShippingCost: 9.99,
      };

      mockProductApi.get.mockResolvedValue({ data: mockResponse } as any);

      const result = await shippingService.getShippingCost(35.00, 'US');

      expect(result).toEqual(mockResponse);
      expect(mockProductApi.get).toHaveBeenCalled();
      // Cache should be removed or updated with new data
      const cached = sessionStorage.getItem('shipping_cost_US');
      if (cached) {
        const cachedData = JSON.parse(cached);
        expect(cachedData.data.freeShippingThreshold).toBeGreaterThan(0);
      }
    });

    it('should fetch from API when cached cost is expired', async () => {
      const cachedData = {
        data: {
          region: 'US',
          cartTotal: 0,
          shippingCost: 9.99,
          freeShippingThreshold: 50.00,
          remainingAmount: 50.00,
          qualifiesForFreeShipping: false,
          defaultShippingCost: 9.99,
        },
        timestamp: Date.now() - 6 * 60 * 1000, // 6 minutes ago (expired)
      };

      sessionStorage.setItem('shipping_cost_US', JSON.stringify(cachedData));

      const mockResponse = {
        region: 'US',
        cartTotal: 35.00,
        shippingCost: 9.99,
        freeShippingThreshold: 50.00,
        remainingAmount: 15.00,
        qualifiesForFreeShipping: false,
        defaultShippingCost: 9.99,
      };

      mockProductApi.get.mockResolvedValue({ data: mockResponse } as any);

      const result = await shippingService.getShippingCost(35.00, 'US');

      expect(result).toEqual(mockResponse);
      expect(mockProductApi.get).toHaveBeenCalled();
    });

    it('should handle corrupted cost cache gracefully', async () => {
      sessionStorage.setItem('shipping_cost_US', 'invalid json');

      const mockResponse = {
        region: 'US',
        cartTotal: 35.00,
        shippingCost: 9.99,
        freeShippingThreshold: 50.00,
        remainingAmount: 15.00,
        qualifiesForFreeShipping: false,
        defaultShippingCost: 9.99,
      };

      mockProductApi.get.mockResolvedValue({ data: mockResponse } as any);

      const result = await shippingService.getShippingCost(35.00, 'US');

      expect(result).toEqual(mockResponse);
      expect(mockProductApi.get).toHaveBeenCalled();
    });

    it('should return fallback cost when API call fails', async () => {
      mockProductApi.get.mockRejectedValue(new Error('API Error'));

      const result = await shippingService.getShippingCost(35.00, 'US');

      expect(result.region).toBe('US');
      expect(result.freeShippingThreshold).toBe(50.00);
      expect(result.cartTotal).toBe(35.00);
      expect(result.shippingCost).toBe(5.99); // Fallback cost
      expect(result.remainingAmount).toBe(15.00);
      expect(result.qualifiesForFreeShipping).toBe(false);
    });

    it('should return free shipping in fallback when cart qualifies', async () => {
      mockProductApi.get.mockRejectedValue(new Error('API Error'));

      const result = await shippingService.getShippingCost(55.00, 'US');

      expect(result.shippingCost).toBe(0);
      expect(result.qualifiesForFreeShipping).toBe(true);
    });
  });

  describe('getShippingRecommendations', () => {
    it('should fetch shipping recommendations from API', async () => {
      const mockResponse = {
        optimizationPaths: [],
        qualifiesForFreeShipping: false,
        remainingAmount: 15.00,
        region: 'US',
        cartTotal: 35.00,
        freeShippingThreshold: 50.00,
      };

      mockProductApi.get.mockResolvedValue({ data: mockResponse } as any);

      const result = await shippingService.getShippingRecommendations(35.00, [], 'US');

      expect(result).toEqual(mockResponse);
      expect(mockProductApi.get).toHaveBeenCalledWith('/shipping/recommendations?cartTotal=35&region=US');
    });

    it('should include cart items in request when provided', async () => {
      const mockResponse = {
        optimizationPaths: [],
        qualifiesForFreeShipping: false,
        remainingAmount: 15.00,
        region: 'US',
        cartTotal: 35.00,
        freeShippingThreshold: 50.00,
      };

      mockProductApi.get.mockResolvedValue({ data: mockResponse } as any);

      const result = await shippingService.getShippingRecommendations(35.00, ['product-1', 'product-2'], 'US');

      expect(result).toEqual(mockResponse);
      expect(mockProductApi.get).toHaveBeenCalledWith('/shipping/recommendations?cartTotal=35&cartItems=product-1%2Cproduct-2&region=US');
    });

    it('should include userId in request when provided', async () => {
      const mockResponse = {
        optimizationPaths: [],
        qualifiesForFreeShipping: false,
        remainingAmount: 15.00,
        region: 'US',
        cartTotal: 35.00,
        freeShippingThreshold: 50.00,
      };

      mockProductApi.get.mockResolvedValue({ data: mockResponse } as any);

      const result = await shippingService.getShippingRecommendations(35.00, [], 'US', 'user-123');

      expect(result).toEqual(mockResponse);
      expect(mockProductApi.get).toHaveBeenCalledWith('/shipping/recommendations?cartTotal=35&region=US&userId=user-123');
    });

    it('should cache recommendations', async () => {
      const mockResponse = {
        optimizationPaths: [],
        qualifiesForFreeShipping: false,
        remainingAmount: 15.00,
        region: 'US',
        cartTotal: 35.00,
        freeShippingThreshold: 50.00,
      };

      mockProductApi.get.mockResolvedValue({ data: mockResponse } as any);

      await shippingService.getShippingRecommendations(35.00, [], 'US');

      const cached = sessionStorage.getItem('shipping_recommendations_35_empty_US');
      expect(cached).toBeTruthy();

      const cachedData = JSON.parse(cached!);
      expect(cachedData.data).toEqual(mockResponse);
    });

    it('should return cached recommendations when available and valid', async () => {
      const cachedData = {
        data: {
          optimizationPaths: [],
          qualifiesForFreeShipping: false,
          remainingAmount: 15.00,
          region: 'US',
          cartTotal: 35.00,
          freeShippingThreshold: 50.00,
        },
        timestamp: Date.now() - 10000, // 10 seconds ago (within 30 second TTL)
      };

      sessionStorage.setItem('shipping_recommendations_35_empty_US', JSON.stringify(cachedData));

      const result = await shippingService.getShippingRecommendations(35.00, [], 'US');

      expect(result).toEqual(cachedData.data);
      expect(mockProductApi.get).not.toHaveBeenCalled();
    });

    it('should fetch from API when cached recommendations are expired', async () => {
      const cachedData = {
        data: {
          optimizationPaths: [],
          qualifiesForFreeShipping: false,
          remainingAmount: 15.00,
          region: 'US',
          cartTotal: 35.00,
          freeShippingThreshold: 50.00,
        },
        timestamp: Date.now() - 40 * 1000, // 40 seconds ago (expired)
      };

      sessionStorage.setItem('shipping_recommendations_35_empty_US', JSON.stringify(cachedData));

      const mockResponse = {
        optimizationPaths: [],
        qualifiesForFreeShipping: false,
        remainingAmount: 15.00,
        region: 'US',
        cartTotal: 35.00,
        freeShippingThreshold: 50.00,
      };

      mockProductApi.get.mockResolvedValue({ data: mockResponse } as any);

      const result = await shippingService.getShippingRecommendations(35.00, [], 'US');

      expect(result).toEqual(mockResponse);
      expect(mockProductApi.get).toHaveBeenCalled();
    });

    it('should handle corrupted recommendations cache gracefully', async () => {
      sessionStorage.setItem('shipping_recommendations_35_empty_US', 'invalid json');

      const mockResponse = {
        optimizationPaths: [],
        qualifiesForFreeShipping: false,
        remainingAmount: 15.00,
        region: 'US',
        cartTotal: 35.00,
        freeShippingThreshold: 50.00,
      };

      mockProductApi.get.mockResolvedValue({ data: mockResponse } as any);

      const result = await shippingService.getShippingRecommendations(35.00, [], 'US');

      expect(result).toEqual(mockResponse);
      expect(mockProductApi.get).toHaveBeenCalled();
    });

    it('should return null when API call fails', async () => {
      const consoleWarnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});
      mockProductApi.get.mockRejectedValue(new Error('API Error'));

      const result = await shippingService.getShippingRecommendations(35.00, [], 'US');

      expect(result).toBeNull();
      expect(consoleWarnSpy).toHaveBeenCalled();
      consoleWarnSpy.mockRestore();
    });

    it('should sort cart items for cache key', async () => {
      const mockResponse = {
        optimizationPaths: [],
        qualifiesForFreeShipping: false,
        remainingAmount: 15.00,
        region: 'US',
        cartTotal: 35.00,
        freeShippingThreshold: 50.00,
      };

      mockProductApi.get.mockResolvedValue({ data: mockResponse } as any);

      await shippingService.getShippingRecommendations(35.00, ['product-2', 'product-1'], 'US');

      // Cache key should use sorted items
      const cached = sessionStorage.getItem('shipping_recommendations_35_product-1,product-2_US');
      expect(cached).toBeTruthy();
    });
  });

  describe('clearCache', () => {
    it('should clear recommendation cache for specific region', () => {
      sessionStorage.setItem('shipping_recommendations_35_empty_US', 'test');
      sessionStorage.setItem('shipping_recommendations_50_empty_CA', 'test');
      sessionStorage.setItem('shipping_recommendations_35_product-1_US', 'test');

      shippingService.clearCache('US');

      expect(sessionStorage.getItem('shipping_recommendations_35_empty_US')).toBeNull();
      expect(sessionStorage.getItem('shipping_recommendations_35_product-1_US')).toBeNull();
      expect(sessionStorage.getItem('shipping_recommendations_50_empty_CA')).toBeTruthy();
    });

    it('should clear all recommendation cache when no region specified', () => {
      sessionStorage.setItem('shipping_recommendations_35_empty_US', 'test');
      sessionStorage.setItem('shipping_recommendations_50_empty_CA', 'test');
      sessionStorage.setItem('other_key', 'test');

      shippingService.clearCache();

      expect(sessionStorage.getItem('shipping_recommendations_35_empty_US')).toBeNull();
      expect(sessionStorage.getItem('shipping_recommendations_50_empty_CA')).toBeNull();
      expect(sessionStorage.getItem('other_key')).toBeTruthy();
    });
  });
});

