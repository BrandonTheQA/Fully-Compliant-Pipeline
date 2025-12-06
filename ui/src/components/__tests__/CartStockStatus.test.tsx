/// <reference types="@testing-library/jest-dom" />
import { describe, it, expect, jest } from '@jest/globals';
import { render, screen, fireEvent } from '@testing-library/react';
import { CartStockStatus } from '../CartStockStatus';
import type { CartItem } from '../../types';

describe('CartStockStatus', () => {
  const mockOnRemove = jest.fn();

  const mockCartItem: CartItem = {
    id: 'product-1',
    name: 'Test Product',
    description: 'Description',
    price: 10.0,
    quantity: 50,
    category: 'Category',
    orderQuantity: 2,
  };

  it('should display stock status badge', () => {
    const item: CartItem = {
      ...mockCartItem,
      stockStatus: 'IN_STOCK',
    };

    render(<CartStockStatus item={item} />);

    expect(screen.getByText('In Stock')).toBeInTheDocument();
  });

  it('should display low stock warning for LOW_STOCK item', () => {
    const item: CartItem = {
      ...mockCartItem,
      quantity: 5,
      stockStatus: 'LOW_STOCK',
    };

    render(<CartStockStatus item={item} />);

    expect(screen.getByText(/Only 5 left in stock - order soon!/)).toBeInTheDocument();
  });

  it('should display out of stock message and actions for OUT_OF_STOCK item', () => {
    const item: CartItem = {
      ...mockCartItem,
      quantity: 0,
      stockStatus: 'OUT_OF_STOCK',
    };

    render(<CartStockStatus item={item} onRemove={mockOnRemove} />);

    expect(screen.getByText(/This item is currently out of stock and cannot be purchased/)).toBeInTheDocument();
    expect(screen.getByText('Remove from Cart')).toBeInTheDocument();
    expect(screen.getByText('Notify Me When Available')).toBeInTheDocument();
  });

  it('should call onRemove when remove button is clicked', () => {
    const item: CartItem = {
      ...mockCartItem,
      quantity: 0,
      stockStatus: 'OUT_OF_STOCK',
    };

    render(<CartStockStatus item={item} onRemove={mockOnRemove} />);

    const removeButton = screen.getByText('Remove from Cart');
    fireEvent.click(removeButton);

    expect(mockOnRemove).toHaveBeenCalledTimes(1);
  });

  it('should not show out of stock actions for IN_STOCK item', () => {
    const item: CartItem = {
      ...mockCartItem,
      stockStatus: 'IN_STOCK',
    };

    render(<CartStockStatus item={item} />);

    expect(screen.queryByText(/This item is currently out of stock/)).not.toBeInTheDocument();
  });
});

