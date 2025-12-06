/// <reference types="@testing-library/jest-dom" />
import { describe, it, expect } from '@jest/globals';
import { render, screen } from '@testing-library/react';
import { StockStatusBadge } from '../StockStatusBadge';
import type { Product } from '../../types';

describe('StockStatusBadge', () => {
  it('should display IN_STOCK status correctly', () => {
    const product: Product = {
      id: 'product-1',
      name: 'Test Product',
      description: 'Description',
      price: 10.0,
      quantity: 50,
      category: 'Category',
      stockStatus: 'IN_STOCK',
    };

    render(<StockStatusBadge product={product} />);

    expect(screen.getByText('In Stock')).toBeInTheDocument();
    const badge = screen.getByRole('status');
    expect(badge.className).toContain('stock-status-in-stock');
  });

  it('should display LOW_STOCK status correctly', () => {
    const product: Product = {
      id: 'product-1',
      name: 'Test Product',
      description: 'Description',
      price: 10.0,
      quantity: 5,
      category: 'Category',
      stockStatus: 'LOW_STOCK',
    };

    render(<StockStatusBadge product={product} />);

    expect(screen.getByText(/Low Stock - Only 5 left!/)).toBeInTheDocument();
    const badge = screen.getByRole('status');
    expect(badge.className).toContain('stock-status-low-stock');
  });

  it('should display OUT_OF_STOCK status correctly', () => {
    const product: Product = {
      id: 'product-1',
      name: 'Test Product',
      description: 'Description',
      price: 10.0,
      quantity: 0,
      category: 'Category',
      stockStatus: 'OUT_OF_STOCK',
    };

    render(<StockStatusBadge product={product} />);

    expect(screen.getByText('Out of Stock')).toBeInTheDocument();
    const badge = screen.getByRole('status');
    expect(badge.className).toContain('stock-status-out-of-stock');
  });

  it('should infer IN_STOCK from quantity when stockStatus is not provided', () => {
    const product: Product = {
      id: 'product-1',
      name: 'Test Product',
      description: 'Description',
      price: 10.0,
      quantity: 50,
      category: 'Category',
    };

    render(<StockStatusBadge product={product} />);

    expect(screen.getByText('In Stock')).toBeInTheDocument();
  });

  it('should infer OUT_OF_STOCK from zero quantity when stockStatus is not provided', () => {
    const product: Product = {
      id: 'product-1',
      name: 'Test Product',
      description: 'Description',
      price: 10.0,
      quantity: 0,
      category: 'Category',
    };

    render(<StockStatusBadge product={product} />);

    expect(screen.getByText('Out of Stock')).toBeInTheDocument();
  });

  it('should have correct accessibility attributes', () => {
    const product: Product = {
      id: 'product-1',
      name: 'Test Product',
      description: 'Description',
      price: 10.0,
      quantity: 50,
      category: 'Category',
      stockStatus: 'IN_STOCK',
    };

    render(<StockStatusBadge product={product} />);

    const badge = screen.getByRole('status');
    expect(badge.getAttribute('aria-live')).toBe('polite');
  });
});

