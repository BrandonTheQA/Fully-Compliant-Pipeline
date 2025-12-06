/// <reference types="@testing-library/jest-dom" />
import { describe, it, expect } from '@jest/globals';
import { render, screen } from '@testing-library/react';
import { LowStockWarning } from '../LowStockWarning';
import type { Product } from '../../types';

describe('LowStockWarning', () => {
  it('should display warning for LOW_STOCK product', () => {
    const product: Product = {
      id: 'product-1',
      name: 'Test Product',
      description: 'Description',
      price: 10.0,
      quantity: 5,
      category: 'Category',
      stockStatus: 'LOW_STOCK',
    };

    render(<LowStockWarning product={product} />);

    expect(screen.getByText(/Only 5 left in stock - order soon!/)).toBeInTheDocument();
    expect(screen.getByRole('alert')).toBeInTheDocument();
  });

  it('should not render for IN_STOCK product', () => {
    const product: Product = {
      id: 'product-1',
      name: 'Test Product',
      description: 'Description',
      price: 10.0,
      quantity: 50,
      category: 'Category',
      stockStatus: 'IN_STOCK',
    };

    const { container } = render(<LowStockWarning product={product} />);

    expect(container.firstChild).toBeNull();
  });

  it('should not render for OUT_OF_STOCK product', () => {
    const product: Product = {
      id: 'product-1',
      name: 'Test Product',
      description: 'Description',
      price: 10.0,
      quantity: 0,
      category: 'Category',
      stockStatus: 'OUT_OF_STOCK',
    };

    const { container } = render(<LowStockWarning product={product} />);

    expect(container.firstChild).toBeNull();
  });

  it('should have correct accessibility attributes', () => {
    const product: Product = {
      id: 'product-1',
      name: 'Test Product',
      description: 'Description',
      price: 10.0,
      quantity: 3,
      category: 'Category',
      stockStatus: 'LOW_STOCK',
    };

    render(<LowStockWarning product={product} />);

    const warning = screen.getByRole('alert');
    expect(warning.getAttribute('aria-live')).toBe('assertive');
  });
});

