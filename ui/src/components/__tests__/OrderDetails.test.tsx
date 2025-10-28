/// <reference types="@testing-library/jest-dom" />
import { describe, it, expect } from '@jest/globals';
import { render } from '@testing-library/react';
import { screen } from '@testing-library/dom';
import { OrderDetails } from '../OrderDetails';
import type { Order } from '../../types';

describe('OrderDetails', () => {
  const mockOrder: Order = {
    id: 'order-123',
    userId: 'user-456',
    items: [
      {
        productId: 'prod-1',
        productName: 'Laptop',
        quantity: 1,
        price: 999.99,
        subtotal: 999.99,
      },
      {
        productId: 'prod-2',
        productName: 'Mouse',
        quantity: 2,
        price: 29.99,
        subtotal: 59.98,
      },
    ],
    totalAmount: 1059.97,
    status: 'PENDING',
    createdAt: '2024-01-01T00:00:00',
  };

  it('should render order details', () => {
    render(<OrderDetails order={mockOrder} />);

    expect(screen.getByText('Order Details')).toBeInTheDocument();
    expect(screen.getByText('order-123')).toBeInTheDocument();
    expect(screen.getByText('user-456')).toBeInTheDocument();
    expect(screen.getByText('PENDING')).toBeInTheDocument();
    expect(screen.getByText('$1059.97')).toBeInTheDocument();
  });

  it('should render order items table', () => {
    render(<OrderDetails order={mockOrder} />);

    expect(screen.getByText('Laptop')).toBeInTheDocument();
    expect(screen.getByText('Mouse')).toBeInTheDocument();
    
    // Check quantities - use getAllByText since there might be multiple "1" values
    const quantities = screen.getAllByText('1');
    expect(quantities.length).toBeGreaterThan(0);
    expect(screen.getByText('2')).toBeInTheDocument();
    
    // Check prices - use getAllByText for prices since they appear in multiple cells
    const laptopPrices = screen.getAllByText('$999.99');
    expect(laptopPrices.length).toBeGreaterThan(0);
    const mousePrices = screen.getAllByText('$29.99');
    expect(mousePrices.length).toBeGreaterThan(0);
  });

  it('should render formatted date', () => {
    render(<OrderDetails order={mockOrder} />);

    const dateString = new Date('2024-01-01T00:00:00').toLocaleString();
    expect(screen.getByText(dateString)).toBeInTheDocument();
  });
});

