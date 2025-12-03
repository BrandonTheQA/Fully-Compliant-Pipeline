/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect } from '@jest/globals';
import { render, screen } from '@testing-library/react';
import { OrderDetailsCard } from '../OrderDetailsCard';
import type { Order, OrderTracking } from '../../types';

describe('OrderDetailsCard', () => {
  const mockTracking: OrderTracking = {
    orderId: 'order-123',
    status: 'SHIPPED',
    trackingNumber: 'TRACK123',
    carrierName: 'FedEx',
    estimatedDeliveryDate: '2024-01-15T10:00:00Z',
    shippingAddress: '123 Main St, City, State 12345',
    shippingMethod: 'Standard Shipping',
    currentLocation: 'Distribution Center',
    statusHistory: [],
  };

  const mockOrder: Order = {
    id: 'order-123',
    userId: 'user-123',
    items: [
      {
        productId: 'product-1',
        productName: 'Test Product',
        quantity: 2,
        price: 29.99,
        subtotal: 59.98,
      },
      {
        productId: 'product-2',
        productName: 'Another Product',
        quantity: 1,
        price: 19.99,
        subtotal: 19.99,
      },
    ],
    totalAmount: 79.97,
    status: 'SHIPPED',
  };

  it('should render order details card', () => {
    render(<OrderDetailsCard order={mockOrder} tracking={mockTracking} />);
    
    expect(screen.getByText('Order Details')).toBeInTheDocument();
  });

  it('should display order items', () => {
    render(<OrderDetailsCard order={mockOrder} tracking={mockTracking} />);
    
    expect(screen.getByText('Items')).toBeInTheDocument();
    expect(screen.getByText('Test Product')).toBeInTheDocument();
    expect(screen.getByText('Another Product')).toBeInTheDocument();
    expect(screen.getByText(/Qty: 2/i)).toBeInTheDocument();
    expect(screen.getByText(/Qty: 1/i)).toBeInTheDocument();
  });

  it('should display item prices', () => {
    render(<OrderDetailsCard order={mockOrder} tracking={mockTracking} />);
    
    expect(screen.getByText('$59.98')).toBeInTheDocument();
    expect(screen.getByText('$19.99')).toBeInTheDocument();
  });

  it('should display order total', () => {
    render(<OrderDetailsCard order={mockOrder} tracking={mockTracking} />);
    
    expect(screen.getByText(/Total: \$79.97/i)).toBeInTheDocument();
  });

  it('should display shipping address', () => {
    render(<OrderDetailsCard order={mockOrder} tracking={mockTracking} />);
    
    expect(screen.getByText('Shipping Address')).toBeInTheDocument();
    expect(screen.getByText('123 Main St, City, State 12345')).toBeInTheDocument();
  });

  it('should display shipping method', () => {
    render(<OrderDetailsCard order={mockOrder} tracking={mockTracking} />);
    
    expect(screen.getByText(/Shipping Method:/i)).toBeInTheDocument();
    expect(screen.getByText('Standard Shipping')).toBeInTheDocument();
  });

  it('should display current location', () => {
    render(<OrderDetailsCard order={mockOrder} tracking={mockTracking} />);
    
    expect(screen.getByText(/Current Location:/i)).toBeInTheDocument();
    expect(screen.getByText('Distribution Center')).toBeInTheDocument();
  });

  it('should handle order without items', () => {
    const orderWithoutItems: Order = {
      id: 'order-123',
      userId: 'user-123',
      items: [],
      totalAmount: 0,
      status: 'PENDING',
    };

    render(<OrderDetailsCard order={orderWithoutItems} tracking={mockTracking} />);
    
    expect(screen.getByText('Order Details')).toBeInTheDocument();
    expect(screen.queryByText('Items')).not.toBeInTheDocument();
  });

  it('should handle order with items but no productName', () => {
    const orderWithPartialItems: Order = {
      id: 'order-123',
      userId: 'user-123',
      items: [
        {
          productId: 'product-1',
          quantity: 1,
          price: 10.00,
          subtotal: 10.00,
        },
      ],
      totalAmount: 10.00,
      status: 'PENDING',
    };

    render(<OrderDetailsCard order={orderWithPartialItems} tracking={mockTracking} />);
    
    expect(screen.getByText(/Product product-1/i)).toBeInTheDocument();
  });

  it('should handle tracking without shipping address', () => {
    const trackingWithoutAddress: OrderTracking = {
      ...mockTracking,
      shippingAddress: undefined,
    };

    render(<OrderDetailsCard order={mockOrder} tracking={trackingWithoutAddress} />);
    
    expect(screen.queryByText('Shipping Address')).not.toBeInTheDocument();
  });

  it('should handle tracking without shipping method', () => {
    const trackingWithoutMethod: OrderTracking = {
      ...mockTracking,
      shippingMethod: undefined,
    };

    render(<OrderDetailsCard order={mockOrder} tracking={trackingWithoutMethod} />);
    
    expect(screen.queryByText(/Shipping Method:/i)).not.toBeInTheDocument();
  });

  it('should handle tracking without current location', () => {
    const trackingWithoutLocation: OrderTracking = {
      ...mockTracking,
      currentLocation: undefined,
    };

    render(<OrderDetailsCard order={mockOrder} tracking={trackingWithoutLocation} />);
    
    expect(screen.queryByText(/Current Location:/i)).not.toBeInTheDocument();
  });

  it('should handle order without totalAmount', () => {
    const orderWithoutTotal: Order = {
      id: 'order-123',
      userId: 'user-123',
      items: [
        {
          productId: 'product-1',
          productName: 'Test Product',
          quantity: 1,
          price: 10.00,
        },
      ],
      status: 'PENDING',
    };

    render(<OrderDetailsCard order={orderWithoutTotal} tracking={mockTracking} />);
    
    expect(screen.queryByText(/Total:/i)).not.toBeInTheDocument();
  });

  it('should calculate item price from price and quantity when subtotal is missing', () => {
    const orderWithCalculatedPrice: Order = {
      id: 'order-123',
      userId: 'user-123',
      items: [
        {
          productId: 'product-1',
          productName: 'Test Product',
          quantity: 3,
          price: 10.00,
        },
      ],
      totalAmount: 30.00,
      status: 'PENDING',
    };

    render(<OrderDetailsCard order={orderWithCalculatedPrice} tracking={mockTracking} />);
    
    expect(screen.getByText('$30.00')).toBeInTheDocument();
  });
});
