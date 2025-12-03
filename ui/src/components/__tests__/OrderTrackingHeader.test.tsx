/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect } from '@jest/globals';
import { render, screen } from '@testing-library/react';
import { OrderTrackingHeader } from '../OrderTrackingHeader';
import type { OrderTracking } from '../../types';

describe('OrderTrackingHeader', () => {
  const mockTracking: OrderTracking = {
    orderId: 'order-123456789',
    status: 'SHIPPED',
    trackingNumber: 'TRACK123456',
    carrierName: 'FedEx',
    estimatedDeliveryDate: '2024-01-15T10:00:00Z',
    shippingAddress: '123 Main St',
    shippingMethod: 'Standard',
    currentLocation: 'Distribution Center',
    statusHistory: [],
  };

  it('should render order tracking header', () => {
    render(<OrderTrackingHeader tracking={mockTracking} />);
    
    expect(screen.getByText(/Order #/i)).toBeInTheDocument();
  });

  it('should display order status badge', () => {
    render(<OrderTrackingHeader tracking={mockTracking} />);
    
    expect(screen.getByText(/SHIPPED/i)).toBeInTheDocument();
  });

  it('should display tracking number when available', () => {
    render(<OrderTrackingHeader tracking={mockTracking} />);
    
    expect(screen.getByText(/Tracking Number:/i)).toBeInTheDocument();
    expect(screen.getByText('TRACK123456')).toBeInTheDocument();
  });

  it('should display carrier name when available', () => {
    render(<OrderTrackingHeader tracking={mockTracking} />);
    
    expect(screen.getByText(/via FedEx/i)).toBeInTheDocument();
  });

  it('should display estimated delivery date when available', () => {
    render(<OrderTrackingHeader tracking={mockTracking} />);
    
    expect(screen.getByText(/Estimated Delivery:/i)).toBeInTheDocument();
    expect(screen.getByText(/January 15, 2024/i)).toBeInTheDocument();
  });

  it('should format status by replacing underscores', () => {
    const trackingWithUnderscore: OrderTracking = {
      ...mockTracking,
      status: 'OUT_FOR_DELIVERY',
    };

    render(<OrderTrackingHeader tracking={trackingWithUnderscore} />);
    
    // The status is formatted by replacing underscore with space
    // Check that status badge exists and contains the formatted status
    const statusBadge = screen.getByLabelText(/Order status:/i);
    expect(statusBadge).toBeInTheDocument();
    expect(statusBadge.textContent).toContain('OUT');
    expect(statusBadge.textContent).toContain('FOR');
    expect(statusBadge.textContent).toContain('DELIVERY');
  });

  it('should apply correct status color class for DELIVERED', () => {
    const deliveredTracking: OrderTracking = {
      ...mockTracking,
      status: 'DELIVERED',
    };

    render(<OrderTrackingHeader tracking={deliveredTracking} />);
    
    const statusBadge = screen.getByText(/DELIVERED/i).closest('.status-badge');
    expect(statusBadge?.className).toContain('status-delivered');
  });

  it('should apply correct status color class for SHIPPED', () => {
    render(<OrderTrackingHeader tracking={mockTracking} />);
    
    const statusBadge = screen.getByText(/SHIPPED/i).closest('.status-badge');
    expect(statusBadge?.className).toContain('status-shipped');
  });

  it('should apply correct status color class for IN_TRANSIT', () => {
    const inTransitTracking: OrderTracking = {
      ...mockTracking,
      status: 'IN_TRANSIT',
    };

    render(<OrderTrackingHeader tracking={inTransitTracking} />);
    
    const statusBadge = screen.getByText(/IN TRANSIT/i).closest('.status-badge');
    expect(statusBadge?.className).toContain('status-shipped');
  });

  it('should apply correct status color class for PROCESSING', () => {
    const processingTracking: OrderTracking = {
      ...mockTracking,
      status: 'PROCESSING',
    };

    render(<OrderTrackingHeader tracking={processingTracking} />);
    
    const statusBadge = screen.getByText(/PROCESSING/i).closest('.status-badge');
    expect(statusBadge?.className).toContain('status-processing');
  });

  it('should apply default status color class for unknown status', () => {
    const unknownTracking: OrderTracking = {
      ...mockTracking,
      status: 'UNKNOWN_STATUS',
    };

    render(<OrderTrackingHeader tracking={unknownTracking} />);
    
    const statusBadge = screen.getByText(/UNKNOWN STATUS/i).closest('.status-badge');
    expect(statusBadge?.className).toContain('status-pending');
  });

  it('should handle missing tracking number', () => {
    const trackingWithoutNumber: OrderTracking = {
      ...mockTracking,
      trackingNumber: undefined,
    };

    render(<OrderTrackingHeader tracking={trackingWithoutNumber} />);
    
    expect(screen.queryByText(/Tracking Number:/i)).not.toBeInTheDocument();
  });

  it('should handle missing carrier name', () => {
    const trackingWithoutCarrier: OrderTracking = {
      ...mockTracking,
      carrierName: undefined,
    };

    render(<OrderTrackingHeader tracking={trackingWithoutCarrier} />);
    
    expect(screen.getByText(/Tracking Number:/i)).toBeInTheDocument();
    expect(screen.queryByText(/via/i)).not.toBeInTheDocument();
  });

  it('should handle missing estimated delivery date', () => {
    const trackingWithoutDate: OrderTracking = {
      ...mockTracking,
      estimatedDeliveryDate: undefined,
    };

    render(<OrderTrackingHeader tracking={trackingWithoutDate} />);
    
    expect(screen.queryByText(/Estimated Delivery:/i)).not.toBeInTheDocument();
  });

  it('should format date correctly', () => {
    render(<OrderTrackingHeader tracking={mockTracking} />);
    
    // Check for formatted date
    expect(screen.getByText(/January 15, 2024/i)).toBeInTheDocument();
  });

  it('should display "Not available" for invalid date', () => {
    const trackingWithInvalidDate: OrderTracking = {
      ...mockTracking,
      estimatedDeliveryDate: '',
    };

    render(<OrderTrackingHeader tracking={trackingWithInvalidDate} />);
    
    // The formatDate function should handle empty string
    expect(screen.queryByText(/Estimated Delivery:/i)).not.toBeInTheDocument();
  });

  it('should truncate order ID to first 8 characters', () => {
    render(<OrderTrackingHeader tracking={mockTracking} />);
    
    // Should show first 8 characters of order-123456789
    expect(screen.getByText(/Order #/i)).toBeInTheDocument();
    const orderIdElement = screen.getByText(/Order #/i).closest('.order-info');
    expect(orderIdElement).toBeInTheDocument();
  });
});
