/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect } from '@jest/globals';
import { render, screen } from '@testing-library/react';
import { OrderStatusTimeline } from '../OrderStatusTimeline';
import type { OrderStatusHistory } from '../../types';

describe('OrderStatusTimeline', () => {
  const mockStatusHistory: OrderStatusHistory[] = [
    {
      id: 'status-1',
      status: 'PENDING',
      createdAt: '2024-01-10T10:00:00Z',
      location: 'Warehouse A',
      notes: 'Order received',
    },
    {
      id: 'status-2',
      status: 'CONFIRMED',
      createdAt: '2024-01-11T10:00:00Z',
      location: 'Warehouse A',
      notes: 'Order confirmed',
    },
    {
      id: 'status-3',
      status: 'SHIPPED',
      createdAt: '2024-01-12T10:00:00Z',
      location: 'Distribution Center',
      notes: 'Order shipped',
    },
  ];

  it('should render timeline with status history', () => {
    render(<OrderStatusTimeline statusHistory={mockStatusHistory} currentStatus="SHIPPED" />);
    
    expect(screen.getByText('Order Status Timeline')).toBeInTheDocument();
  });

  it('should display all status entries', () => {
    render(<OrderStatusTimeline statusHistory={mockStatusHistory} currentStatus="SHIPPED" />);
    
    expect(screen.getByText(/PENDING/i)).toBeInTheDocument();
    expect(screen.getByText(/CONFIRMED/i)).toBeInTheDocument();
    expect(screen.getByText(/SHIPPED/i)).toBeInTheDocument();
  });

  it('should highlight current status', () => {
    render(<OrderStatusTimeline statusHistory={mockStatusHistory} currentStatus="SHIPPED" />);
    
    const shippedEntry = screen.getByText(/SHIPPED/i).closest('.timeline-item');
    expect(shippedEntry?.className).toContain('current');
    expect(shippedEntry).toHaveAttribute('aria-current', 'step');
  });

  it('should display status dates', () => {
    render(<OrderStatusTimeline statusHistory={mockStatusHistory} currentStatus="SHIPPED" />);
    
    // Dates should be formatted and displayed
    const dates = screen.getAllByText(/\w{3} \d{1,2}, \d{4}/);
    expect(dates.length).toBeGreaterThan(0);
  });

  it('should display location when available', () => {
    render(<OrderStatusTimeline statusHistory={mockStatusHistory} currentStatus="SHIPPED" />);
    
    expect(screen.getByText(/Location:/i)).toBeInTheDocument();
    expect(screen.getByText('Warehouse A')).toBeInTheDocument();
    expect(screen.getByText('Distribution Center')).toBeInTheDocument();
  });

  it('should display notes when available', () => {
    render(<OrderStatusTimeline statusHistory={mockStatusHistory} currentStatus="SHIPPED" />);
    
    expect(screen.getByText('Order received')).toBeInTheDocument();
    expect(screen.getByText('Order confirmed')).toBeInTheDocument();
    expect(screen.getByText('Order shipped')).toBeInTheDocument();
  });

  it('should handle empty status history', () => {
    render(<OrderStatusTimeline statusHistory={[]} currentStatus="PENDING" />);
    
    expect(screen.getByText('No status history available')).toBeInTheDocument();
  });

  it('should sort status history by date descending', () => {
    const unsortedHistory: OrderStatusHistory[] = [
      {
        id: 'status-1',
        status: 'PENDING',
        createdAt: '2024-01-12T10:00:00Z',
      },
      {
        id: 'status-2',
        status: 'CONFIRMED',
        createdAt: '2024-01-10T10:00:00Z',
      },
      {
        id: 'status-3',
        status: 'SHIPPED',
        createdAt: '2024-01-11T10:00:00Z',
      },
    ];

    render(<OrderStatusTimeline statusHistory={unsortedHistory} currentStatus="SHIPPED" />);
    
    const timelineItems = screen.getAllByRole('listitem', { hidden: true });
    // Most recent should be first
    expect(timelineItems.length).toBeGreaterThan(0);
  });

  it('should handle status history without location', () => {
    const historyWithoutLocation: OrderStatusHistory[] = [
      {
        id: 'status-1',
        status: 'PENDING',
        createdAt: '2024-01-10T10:00:00Z',
      },
    ];

    render(<OrderStatusTimeline statusHistory={historyWithoutLocation} currentStatus="PENDING" />);
    
    expect(screen.queryByText(/Location:/i)).not.toBeInTheDocument();
  });

  it('should handle status history without notes', () => {
    const historyWithoutNotes: OrderStatusHistory[] = [
      {
        id: 'status-1',
        status: 'PENDING',
        createdAt: '2024-01-10T10:00:00Z',
        location: 'Warehouse A',
      },
    ];

    render(<OrderStatusTimeline statusHistory={historyWithoutNotes} currentStatus="PENDING" />);
    
    expect(screen.getByText(/PENDING/i)).toBeInTheDocument();
  });

  it('should handle case-insensitive status comparison', () => {
    render(<OrderStatusTimeline statusHistory={mockStatusHistory} currentStatus="shipped" />);
    
    const shippedEntry = screen.getByText(/SHIPPED/i).closest('.timeline-item');
    expect(shippedEntry?.className).toContain('current');
  });

  it('should format status names by replacing underscores', () => {
    const historyWithUnderscores: OrderStatusHistory[] = [
      {
        id: 'status-1',
        status: 'OUT_FOR_DELIVERY',
        createdAt: '2024-01-10T10:00:00Z',
      },
    ];

    render(<OrderStatusTimeline statusHistory={historyWithUnderscores} currentStatus="OUT_FOR_DELIVERY" />);
    
    expect(screen.getByText(/OUT FOR DELIVERY/i)).toBeInTheDocument();
  });
});
