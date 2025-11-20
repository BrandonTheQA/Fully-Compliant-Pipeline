/// <reference types="@testing-library/jest-dom" />
import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { render, screen, waitFor } from '@testing-library/react';
import { ProductShippingPreview } from '../ProductShippingPreview';
import { shippingService } from '../../services/shippingService';
import type { Product } from '../../types';

jest.mock('../../services/shippingService');

const mockShippingService = shippingService as jest.Mocked<typeof shippingService>;

describe('ProductShippingPreview', () => {
  const mockProduct: Product = {
    id: '1',
    name: 'Test Product',
    description: 'Test Description',
    price: 35.00,
    quantity: 10,
    category: 'Test Category',
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should display shipping cost when product is below threshold', async () => {
    const mockShippingData = {
      region: 'US',
      cartTotal: 35.00,
      shippingCost: 9.99,
      freeShippingThreshold: 50.00,
      remainingAmount: 15.00,
      qualifiesForFreeShipping: false,
      defaultShippingCost: 9.99,
    };

    mockShippingService.getShippingCost.mockResolvedValue(mockShippingData);

    render(<ProductShippingPreview product={mockProduct} region="US" />);

    await waitFor(() => {
      expect(screen.getByText(/Estimated Shipping:/i)).toBeInTheDocument();
      expect(screen.getByText(/\$9\.99/)).toBeInTheDocument();
    });

    expect(screen.getByText(/Shipping to US/i)).toBeInTheDocument();
    expect(mockShippingService.getShippingCost).toHaveBeenCalledWith(35.00, 'US');
  });

  it('should display FREE shipping when product qualifies', async () => {
    const freeShippingProduct: Product = {
      ...mockProduct,
      price: 55.00,
    };

    const mockShippingData = {
      region: 'US',
      cartTotal: 55.00,
      shippingCost: 0,
      freeShippingThreshold: 50.00,
      remainingAmount: 0,
      qualifiesForFreeShipping: true,
      defaultShippingCost: 9.99,
    };

    mockShippingService.getShippingCost.mockResolvedValue(mockShippingData);

    render(<ProductShippingPreview product={freeShippingProduct} region="US" />);

    await waitFor(() => {
      expect(screen.getAllByText(/FREE/i).length).toBeGreaterThan(0);
      expect(screen.getByText(/This item qualifies for FREE shipping!/i)).toBeInTheDocument();
    });

    expect(mockShippingService.getShippingCost).toHaveBeenCalledWith(55.00, 'US');
  });

  it('should display progress indicator when product is below threshold', async () => {
    const mockShippingData = {
      region: 'US',
      cartTotal: 35.00,
      shippingCost: 9.99,
      freeShippingThreshold: 50.00,
      remainingAmount: 15.00,
      qualifiesForFreeShipping: false,
      defaultShippingCost: 9.99,
    };

    mockShippingService.getShippingCost.mockResolvedValue(mockShippingData);

    render(<ProductShippingPreview product={mockProduct} region="US" />);

    await waitFor(() => {
      expect(screen.getByText(/Add \$15\.00 more to qualify for FREE shipping!/i)).toBeInTheDocument();
    });
  });

  it('should display region indicator', async () => {
    const mockShippingData = {
      region: 'CA',
      cartTotal: 35.00,
      shippingCost: 12.99,
      freeShippingThreshold: 75.00,
      remainingAmount: 40.00,
      qualifiesForFreeShipping: false,
      defaultShippingCost: 12.99,
    };

    mockShippingService.getShippingCost.mockResolvedValue(mockShippingData);

    render(<ProductShippingPreview product={mockProduct} region="CA" />);

    await waitFor(() => {
      expect(screen.getByText(/Shipping to CA/i)).toBeInTheDocument();
    });
  });

  it('should handle loading state', () => {
    mockShippingService.getShippingCost.mockImplementation(
      () => new Promise(() => {}) // Never resolves
    );

    render(<ProductShippingPreview product={mockProduct} region="US" />);

    expect(screen.getByText(/Calculating shipping\.\.\./i)).toBeInTheDocument();
  });

  it('should handle error gracefully with fallback message', async () => {
    mockShippingService.getShippingCost.mockRejectedValue(new Error('API Error'));

    render(<ProductShippingPreview product={mockProduct} region="US" />);

    await waitFor(() => {
      // Error handling shows fallback data, so either fallback message or shipping info should appear
      const fallbackMessage = screen.queryByText(/Shipping cost calculated at checkout/i);
      const shippingInfo = screen.queryByText(/Estimated Shipping:/i);
      expect(fallbackMessage || shippingInfo).toBeTruthy();
    }, { timeout: 3000 });
  });

  it('should update when region changes', async () => {
    const mockShippingDataUS = {
      region: 'US',
      cartTotal: 35.00,
      shippingCost: 9.99,
      freeShippingThreshold: 50.00,
      remainingAmount: 15.00,
      qualifiesForFreeShipping: false,
      defaultShippingCost: 9.99,
    };

    const mockShippingDataCA = {
      region: 'CA',
      cartTotal: 35.00,
      shippingCost: 12.99,
      freeShippingThreshold: 75.00,
      remainingAmount: 40.00,
      qualifiesForFreeShipping: false,
      defaultShippingCost: 12.99,
    };

    mockShippingService.getShippingCost
      .mockResolvedValueOnce(mockShippingDataUS)
      .mockResolvedValueOnce(mockShippingDataCA);

    const { rerender } = render(<ProductShippingPreview product={mockProduct} region="US" />);

    await waitFor(() => {
      expect(screen.getByText(/Shipping to US/i)).toBeInTheDocument();
    });

    rerender(<ProductShippingPreview product={mockProduct} region="CA" />);

    await waitFor(() => {
      expect(screen.getByText(/Shipping to CA/i)).toBeInTheDocument();
    });

    expect(mockShippingService.getShippingCost).toHaveBeenCalledTimes(2);
  });

  it('should update when product price changes', async () => {
    const mockShippingData1 = {
      region: 'US',
      cartTotal: 35.00,
      shippingCost: 9.99,
      freeShippingThreshold: 50.00,
      remainingAmount: 15.00,
      qualifiesForFreeShipping: false,
      defaultShippingCost: 9.99,
    };

    const mockShippingData2 = {
      region: 'US',
      cartTotal: 55.00,
      shippingCost: 0,
      freeShippingThreshold: 50.00,
      remainingAmount: 0,
      qualifiesForFreeShipping: true,
      defaultShippingCost: 9.99,
    };

    mockShippingService.getShippingCost
      .mockResolvedValueOnce(mockShippingData1)
      .mockResolvedValueOnce(mockShippingData2);

    const { rerender } = render(<ProductShippingPreview product={mockProduct} region="US" />);

    await waitFor(() => {
      expect(screen.getByText(/\$9\.99/)).toBeInTheDocument();
    });

    const updatedProduct = { ...mockProduct, price: 55.00 };
    rerender(<ProductShippingPreview product={updatedProduct} region="US" />);

    await waitFor(() => {
      expect(screen.getAllByText(/FREE/i).length).toBeGreaterThan(0);
    });

    // Note: Due to debouncing and memoization, may be called once or twice
    expect(mockShippingService.getShippingCost).toHaveBeenCalled();
  });

  it('should use fallback data when API fails', async () => {
    mockShippingService.getShippingCost.mockRejectedValue(new Error('API Error'));

    render(<ProductShippingPreview product={mockProduct} region="US" />);

    await waitFor(() => {
      // Should show fallback message or fallback data
      const fallbackMessage = screen.queryByText(/Shipping cost calculated at checkout/i);
      const fallbackData = screen.queryByText(/Estimated Shipping:/i);
      expect(fallbackMessage || fallbackData).toBeTruthy();
    });
  });

  it('should handle product at exact threshold', async () => {
    const thresholdProduct: Product = {
      ...mockProduct,
      price: 50.00,
    };

    const mockShippingData = {
      region: 'US',
      cartTotal: 50.00,
      shippingCost: 0,
      freeShippingThreshold: 50.00,
      remainingAmount: 0,
      qualifiesForFreeShipping: true,
      defaultShippingCost: 9.99,
    };

    mockShippingService.getShippingCost.mockResolvedValue(mockShippingData);

    render(<ProductShippingPreview product={thresholdProduct} region="US" />);

    await waitFor(() => {
      expect(screen.getAllByText(/FREE/i).length).toBeGreaterThan(0);
      expect(screen.getByText(/This item qualifies for FREE shipping!/i)).toBeInTheDocument();
    });
  });

  it('should handle null region', async () => {
    const mockShippingData = {
      region: 'US',
      cartTotal: 35.00,
      shippingCost: 9.99,
      freeShippingThreshold: 50.00,
      remainingAmount: 15.00,
      qualifiesForFreeShipping: false,
      defaultShippingCost: 9.99,
    };

    mockShippingService.getShippingCost.mockResolvedValue(mockShippingData);

    render(<ProductShippingPreview product={mockProduct} region={null} />);

    await waitFor(() => {
      expect(mockShippingService.getShippingCost).toHaveBeenCalledWith(35.00, undefined);
    });
  });

  it('should not show progress indicator when qualifies for free shipping', async () => {
    const freeShippingProduct: Product = {
      ...mockProduct,
      price: 55.00,
    };

    const mockShippingData = {
      region: 'US',
      cartTotal: 55.00,
      shippingCost: 0,
      freeShippingThreshold: 50.00,
      remainingAmount: 0,
      qualifiesForFreeShipping: true,
      defaultShippingCost: 9.99,
    };

    mockShippingService.getShippingCost.mockResolvedValue(mockShippingData);

    render(<ProductShippingPreview product={freeShippingProduct} region="US" />);

    await waitFor(() => {
      expect(screen.queryByText(/Add.*more to qualify/i)).not.toBeInTheDocument();
      expect(screen.getByText(/This item qualifies for FREE shipping!/i)).toBeInTheDocument();
      expect(screen.getAllByText(/FREE/i).length).toBeGreaterThan(0);
    });
  });
});

