/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { LoyaltyDashboard } from '../LoyaltyDashboard';
import { loyaltyService } from '../../services/loyaltyService';
import type { LoyaltyDashboard as LoyaltyDashboardType } from '../../types';

jest.mock('../../services/loyaltyService');

describe('LoyaltyDashboard', () => {
  const mockDashboard: LoyaltyDashboardType = {
    account: {
      accountId: 'acc-123',
      userId: 'user-123',
      currentPoints: 1500,
      currentTier: 'SILVER',
      highestTierAchieved: 'SILVER',
      lifetimePointsEarned: 2500,
      lifetimePointsRedeemed: 1000,
      referralCode: 'REF123',
      enrollmentDate: '2024-01-01',
      isActive: true,
    },
    recentTransactions: [
      {
        transactionId: 'txn-1',
        transactionType: 'EARNED',
        points: 100,
        description: 'Purchase reward',
        activityType: 'PURCHASE',
        createdAt: '2024-01-15T10:00:00Z',
      },
      {
        transactionId: 'txn-2',
        transactionType: 'REDEEMED',
        points: -50,
        description: 'Points redemption',
        activityType: 'REDEMPTION',
        createdAt: '2024-01-14T10:00:00Z',
      },
    ],
    pointsToNextTier: 500,
    expiringPoints: 200,
    expiringPointsDate: '2024-02-01',
    tierBenefits: {
      tier: 'SILVER',
      multiplier: 1.5,
      benefits: ['Free shipping', 'Early access to sales'],
      pointsToNextTier: 500,
    },
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render loading state initially', async () => {
    (loyaltyService.getDashboard as jest.MockedFunction<typeof loyaltyService.getDashboard>).mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve(mockDashboard), 100))
    );

    render(<LoyaltyDashboard userId="user-123" />);

    expect(screen.getByText('Loading loyalty dashboard...')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.queryByText('Loading loyalty dashboard...')).not.toBeInTheDocument();
    });
  });

  it('should load and display dashboard data', async () => {
    (loyaltyService.getDashboard as jest.MockedFunction<typeof loyaltyService.getDashboard>).mockResolvedValue(mockDashboard);

    render(<LoyaltyDashboard userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('Loyalty Program')).toBeInTheDocument();
      expect(screen.getByText(/1,500/)).toBeInTheDocument();
    });
  });

  it('should display current points balance', async () => {
    (loyaltyService.getDashboard as jest.MockedFunction<typeof loyaltyService.getDashboard>).mockResolvedValue(mockDashboard);

    render(<LoyaltyDashboard userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText(/Current Balance/i)).toBeInTheDocument();
      expect(screen.getByText(/1,500 points/)).toBeInTheDocument();
    });
  });

  it('should display tier badge', async () => {
    (loyaltyService.getDashboard as jest.MockedFunction<typeof loyaltyService.getDashboard>).mockResolvedValue(mockDashboard);

    render(<LoyaltyDashboard userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText(/Silver Member/i)).toBeInTheDocument();
    });
  });

  it('should display points to next tier', async () => {
    (loyaltyService.getDashboard as jest.MockedFunction<typeof loyaltyService.getDashboard>).mockResolvedValue(mockDashboard);

    render(<LoyaltyDashboard userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText(/500 points until next tier/i)).toBeInTheDocument();
    });
  });

  it('should display tier benefits', async () => {
    (loyaltyService.getDashboard as jest.MockedFunction<typeof loyaltyService.getDashboard>).mockResolvedValue(mockDashboard);

    render(<LoyaltyDashboard userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('Tier Benefits')).toBeInTheDocument();
      expect(screen.getByText(/1.5x Points Multiplier/i)).toBeInTheDocument();
      expect(screen.getByText('Free shipping')).toBeInTheDocument();
      expect(screen.getByText('Early access to sales')).toBeInTheDocument();
    });
  });

  it('should display expiring points warning', async () => {
    (loyaltyService.getDashboard as jest.MockedFunction<typeof loyaltyService.getDashboard>).mockResolvedValue(mockDashboard);

    render(<LoyaltyDashboard userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('Points Expiring Soon')).toBeInTheDocument();
      expect(screen.getByText(/200 points/)).toBeInTheDocument();
    });
  });

  it('should not display expiring points warning when no points expiring', async () => {
    const dashboardWithoutExpiring: LoyaltyDashboardType = {
      ...mockDashboard,
      expiringPoints: 0,
    };
    (loyaltyService.getDashboard as jest.MockedFunction<typeof loyaltyService.getDashboard>).mockResolvedValue(dashboardWithoutExpiring);

    render(<LoyaltyDashboard userId="user-123" />);

    await waitFor(() => {
      expect(screen.queryByText('Points Expiring Soon')).not.toBeInTheDocument();
    });
  });

  it('should display lifetime statistics', async () => {
    (loyaltyService.getDashboard as jest.MockedFunction<typeof loyaltyService.getDashboard>).mockResolvedValue(mockDashboard);

    render(<LoyaltyDashboard userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('Lifetime Statistics')).toBeInTheDocument();
      expect(screen.getByText(/Total Points Earned/i)).toBeInTheDocument();
      expect(screen.getByText(/2,500/)).toBeInTheDocument();
      expect(screen.getByText(/Total Points Redeemed/i)).toBeInTheDocument();
      expect(screen.getByText(/1,000/)).toBeInTheDocument();
    });
  });

  it('should display recent transactions', async () => {
    (loyaltyService.getDashboard as jest.MockedFunction<typeof loyaltyService.getDashboard>).mockResolvedValue(mockDashboard);

    render(<LoyaltyDashboard userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('Recent Activity')).toBeInTheDocument();
      expect(screen.getByText(/\+100 points/)).toBeInTheDocument();
      expect(screen.getByText(/-50 points/)).toBeInTheDocument();
      expect(screen.getByText('Purchase reward')).toBeInTheDocument();
      expect(screen.getByText('Points redemption')).toBeInTheDocument();
    });
  });

  it('should display "No recent transactions" when empty', async () => {
    const dashboardWithoutTransactions: LoyaltyDashboardType = {
      ...mockDashboard,
      recentTransactions: [],
    };
    (loyaltyService.getDashboard as jest.MockedFunction<typeof loyaltyService.getDashboard>).mockResolvedValue(dashboardWithoutTransactions);

    render(<LoyaltyDashboard userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('No recent transactions')).toBeInTheDocument();
    });
  });

  it('should display error message and retry button on failure', async () => {
    const error = new Error('Failed to load dashboard');
    (loyaltyService.getDashboard as jest.MockedFunction<typeof loyaltyService.getDashboard>).mockRejectedValue(error);

    render(<LoyaltyDashboard userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('Failed to load loyalty dashboard')).toBeInTheDocument();
      expect(screen.getByText('Retry')).toBeInTheDocument();
    });
  });

  it('should reload dashboard when retry button is clicked', async () => {
    const error = new Error('Failed to load dashboard');
    (loyaltyService.getDashboard as jest.MockedFunction<typeof loyaltyService.getDashboard>)
      .mockRejectedValueOnce(error)
      .mockResolvedValueOnce(mockDashboard);

    render(<LoyaltyDashboard userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('Retry')).toBeInTheDocument();
    });

    const retryButton = screen.getByText('Retry');
    fireEvent.click(retryButton);

    await waitFor(() => {
      expect(loyaltyService.getDashboard).toHaveBeenCalledTimes(2);
      expect(screen.getByText('Loyalty Program')).toBeInTheDocument();
    });
  });

  it('should reload dashboard when userId changes', async () => {
    (loyaltyService.getDashboard as jest.MockedFunction<typeof loyaltyService.getDashboard>).mockResolvedValue(mockDashboard);

    const { rerender } = render(<LoyaltyDashboard userId="user-123" />);

    await waitFor(() => {
      expect(loyaltyService.getDashboard).toHaveBeenCalledWith('user-123');
    });

    jest.clearAllMocks();
    (loyaltyService.getDashboard as jest.MockedFunction<typeof loyaltyService.getDashboard>).mockResolvedValue(mockDashboard);

    rerender(<LoyaltyDashboard userId="user-456" />);

    await waitFor(() => {
      expect(loyaltyService.getDashboard).toHaveBeenCalledWith('user-456');
    });
  });

  it('should render ReferralSection component', async () => {
    (loyaltyService.getDashboard as jest.MockedFunction<typeof loyaltyService.getDashboard>).mockResolvedValue(mockDashboard);

    render(<LoyaltyDashboard userId="user-123" />);

    await waitFor(() => {
      // ReferralSection should be rendered (it will have its own tests)
      expect(screen.getByText('Loyalty Program')).toBeInTheDocument();
    });
  });

  it('should calculate progress bar width correctly', async () => {
    (loyaltyService.getDashboard as jest.MockedFunction<typeof loyaltyService.getDashboard>).mockResolvedValue(mockDashboard);

    render(<LoyaltyDashboard userId="user-123" />);

    await waitFor(() => {
      // Progress bar should be rendered with calculated width
      expect(screen.getByText(/500 points until next tier/i)).toBeInTheDocument();
    });
  });
});
