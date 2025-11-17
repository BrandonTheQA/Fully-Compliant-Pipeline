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
});

