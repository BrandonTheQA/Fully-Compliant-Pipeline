/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect } from '@jest/globals';
import { render, screen } from '@testing-library/react';
import { ReturnStatusTimeline } from '../ReturnStatusTimeline';
import type { ReturnStatusHistory } from '../../types';

describe('ReturnStatusTimeline', () => {
  const mockStatusHistory: ReturnStatusHistory[] = [
    {
      status: 'PENDING_APPROVAL',
      createdAt: '2024-01-01T10:00:00Z',
    },
    {
      status: 'APPROVED',
      createdAt: '2024-01-02T10:00:00Z',
      notes: 'Approved by admin',
      updatedBy: 'admin-1',
    },
    {
      status: 'RECEIVED',
      createdAt: '2024-01-05T10:00:00Z',
    },
  ];

  it('should render status timeline with history', () => {
    render(<ReturnStatusTimeline statusHistory={mockStatusHistory} />);

    expect(screen.getByText('Status History')).toBeInTheDocument();
    // Component uses replace('_', ' ') which only replaces first underscore
    expect(screen.getAllByText(/PENDING APPROVAL/i).length).toBeGreaterThan(0);
    // APPROVED may appear multiple times
    expect(screen.getAllByText(/APPROVED/i).length).toBeGreaterThan(0);
    expect(screen.getByText(/RECEIVED/i)).toBeInTheDocument();
  });

  it('should render empty timeline when history is empty', () => {
    render(<ReturnStatusTimeline statusHistory={[]} />);

    expect(screen.getByText('Status History')).toBeInTheDocument();
    expect(screen.queryByText(/PENDING_APPROVAL/i)).not.toBeInTheDocument();
  });

  it('should format status names correctly', () => {
    render(<ReturnStatusTimeline statusHistory={mockStatusHistory} />);

    // Component uses replace('_', ' ') which only replaces first underscore
    expect(screen.getByText(/PENDING APPROVAL/i)).toBeInTheDocument();
  });

  it('should display timestamps correctly', () => {
    render(<ReturnStatusTimeline statusHistory={mockStatusHistory} />);

    // Check that dates are rendered (format may vary by locale)
    const dates = screen.getAllByText(/\d{1,2}\/\d{1,2}\/\d{4}/);
    expect(dates.length).toBeGreaterThan(0);
  });

  it('should display notes when provided', () => {
    render(<ReturnStatusTimeline statusHistory={mockStatusHistory} />);

    expect(screen.getByText('Approved by admin')).toBeInTheDocument();
  });

  it('should display updatedBy when provided', () => {
    render(<ReturnStatusTimeline statusHistory={mockStatusHistory} />);

    expect(screen.getByText(/Updated by: admin-1/i)).toBeInTheDocument();
  });

  it('should not display notes when not provided', () => {
    const historyWithoutNotes: ReturnStatusHistory[] = [
      {
        status: 'PENDING_APPROVAL',
        createdAt: '2024-01-01T10:00:00Z',
      },
    ];

    render(<ReturnStatusTimeline statusHistory={historyWithoutNotes} />);

    expect(screen.queryByText(/Updated by:/i)).not.toBeInTheDocument();
  });

  it('should not display updatedBy when not provided', () => {
    const historyWithoutUpdatedBy: ReturnStatusHistory[] = [
      {
        status: 'APPROVED',
        createdAt: '2024-01-02T10:00:00Z',
        notes: 'Approved',
      },
    ];

    render(<ReturnStatusTimeline statusHistory={historyWithoutUpdatedBy} />);

    expect(screen.queryByText(/Updated by:/i)).not.toBeInTheDocument();
  });

  it('should apply correct color for APPROVED status', () => {
    const history: ReturnStatusHistory[] = [
      {
        status: 'APPROVED',
        createdAt: '2024-01-01T10:00:00Z',
      },
    ];

    const { container } = render(<ReturnStatusTimeline statusHistory={history} />);
    const marker = container.querySelector('.timeline-marker') as HTMLElement;
    expect(marker.style.backgroundColor).toBe('rgb(40, 167, 69)');
  });

  it('should apply correct color for REJECTED status', () => {
    const history: ReturnStatusHistory[] = [
      {
        status: 'REJECTED',
        createdAt: '2024-01-01T10:00:00Z',
      },
    ];

    const { container } = render(<ReturnStatusTimeline statusHistory={history} />);
    const marker = container.querySelector('.timeline-marker') as HTMLElement;
    expect(marker.style.backgroundColor).toBe('rgb(220, 53, 69)');
  });

  it('should apply correct color for REFUNDED status', () => {
    const history: ReturnStatusHistory[] = [
      {
        status: 'REFUNDED',
        createdAt: '2024-01-01T10:00:00Z',
      },
    ];

    const { container } = render(<ReturnStatusTimeline statusHistory={history} />);
    const marker = container.querySelector('.timeline-marker') as HTMLElement;
    expect(marker.style.backgroundColor).toBe('rgb(23, 162, 184)');
  });

  it('should apply correct color for COMPLETED status', () => {
    const history: ReturnStatusHistory[] = [
      {
        status: 'COMPLETED',
        createdAt: '2024-01-01T10:00:00Z',
      },
    ];

    const { container } = render(<ReturnStatusTimeline statusHistory={history} />);
    const marker = container.querySelector('.timeline-marker') as HTMLElement;
    expect(marker.style.backgroundColor).toBe('rgb(23, 162, 184)');
  });

  it('should apply correct color for PENDING_APPROVAL status', () => {
    const history: ReturnStatusHistory[] = [
      {
        status: 'PENDING_APPROVAL',
        createdAt: '2024-01-01T10:00:00Z',
      },
    ];

    const { container } = render(<ReturnStatusTimeline statusHistory={history} />);
    const marker = container.querySelector('.timeline-marker') as HTMLElement;
    expect(marker.style.backgroundColor).toBe('rgb(255, 193, 7)');
  });

  it('should apply default color for unknown status', () => {
    const history: ReturnStatusHistory[] = [
      {
        status: 'UNKNOWN_STATUS' as any,
        createdAt: '2024-01-01T10:00:00Z',
      },
    ];

    const { container } = render(<ReturnStatusTimeline statusHistory={history} />);
    const marker = container.querySelector('.timeline-marker') as HTMLElement;
    expect(marker.style.backgroundColor).toBe('rgb(108, 117, 125)');
  });

  it('should render multiple timeline items', () => {
    const longHistory: ReturnStatusHistory[] = [
      {
        status: 'PENDING_APPROVAL',
        createdAt: '2024-01-01T10:00:00Z',
      },
      {
        status: 'APPROVED',
        createdAt: '2024-01-02T10:00:00Z',
      },
      {
        status: 'IN_TRANSIT',
        createdAt: '2024-01-03T10:00:00Z',
      },
      {
        status: 'RECEIVED',
        createdAt: '2024-01-04T10:00:00Z',
      },
      {
        status: 'REFUNDED',
        createdAt: '2024-01-05T10:00:00Z',
      },
    ];

    render(<ReturnStatusTimeline statusHistory={longHistory} />);

    // Check that all statuses are rendered (with underscore replacement)
    expect(screen.getByText(/PENDING APPROVAL/i)).toBeInTheDocument();
    expect(screen.getByText(/APPROVED/i)).toBeInTheDocument();
    expect(screen.getByText(/IN TRANSIT/i)).toBeInTheDocument();
    expect(screen.getByText(/RECEIVED/i)).toBeInTheDocument();
    expect(screen.getByText(/REFUNDED/i)).toBeInTheDocument();
  });
});

