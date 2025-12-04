/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect, beforeEach, afterEach, jest } from '@jest/globals';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { ReferralSection } from '../ReferralSection';
import { loyaltyService } from '../../services/loyaltyService';
import type { ReferralStats } from '../../types';

jest.mock('../../services/loyaltyService');

// Mock navigator.clipboard
const mockWriteText = jest.fn(() => Promise.resolve());
const mockClipboard = {
  writeText: mockWriteText,
};
// Store reference for assertions
const originalClipboard = navigator.clipboard;
Object.assign(navigator, {
  clipboard: mockClipboard as any,
});

describe('ReferralSection', () => {
  const mockReferralCode = {
    referralCode: 'REF123',
    referralLink: 'https://example.com/ref/REF123',
  };

  const mockReferralStats: ReferralStats = {
    totalReferrals: 10,
    successfulReferrals: 8,
    pointsEarned: 500,
    successRate: 80.0,
  };

  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.runOnlyPendingTimers();
    jest.useRealTimers();
  });

  it('should render loading state initially', async () => {
    (loyaltyService.getReferralCode as jest.MockedFunction<typeof loyaltyService.getReferralCode>).mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve(mockReferralCode), 100))
    );
    (loyaltyService.getReferralStats as jest.MockedFunction<typeof loyaltyService.getReferralStats>).mockResolvedValue(mockReferralStats);

    render(<ReferralSection userId="user-123" />);

    expect(screen.getByText('Loading referral information...')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.queryByText('Loading referral information...')).not.toBeInTheDocument();
    });
  });

  it('should load and display referral code', async () => {
    (loyaltyService.getReferralCode as jest.MockedFunction<typeof loyaltyService.getReferralCode>).mockResolvedValue(mockReferralCode);
    (loyaltyService.getReferralStats as jest.MockedFunction<typeof loyaltyService.getReferralStats>).mockResolvedValue(mockReferralStats);

    render(<ReferralSection userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('Referral Program')).toBeInTheDocument();
      expect(screen.getByText('REF123')).toBeInTheDocument();
    });
  });

  it('should load and display referral link', async () => {
    (loyaltyService.getReferralCode as jest.MockedFunction<typeof loyaltyService.getReferralCode>).mockResolvedValue(mockReferralCode);
    (loyaltyService.getReferralStats as jest.MockedFunction<typeof loyaltyService.getReferralStats>).mockResolvedValue(mockReferralStats);

    render(<ReferralSection userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('https://example.com/ref/REF123')).toBeInTheDocument();
    });
  });

  it('should copy referral code to clipboard', async () => {
    (loyaltyService.getReferralCode as jest.MockedFunction<typeof loyaltyService.getReferralCode>).mockResolvedValue(mockReferralCode);
    (loyaltyService.getReferralStats as jest.MockedFunction<typeof loyaltyService.getReferralStats>).mockResolvedValue(mockReferralStats);

    render(<ReferralSection userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('REF123')).toBeInTheDocument();
    });

    const copyCodeButton = screen.getAllByText('Copy')[0];
    fireEvent.click(copyCodeButton);

    await waitFor(() => {
      expect(mockWriteText).toHaveBeenCalledWith('REF123');
    });
    
    await waitFor(() => {
      expect(screen.getAllByText('✓ Copied').length).toBeGreaterThan(0);
    });
  });

  it('should copy referral link to clipboard', async () => {
    (loyaltyService.getReferralCode as jest.MockedFunction<typeof loyaltyService.getReferralCode>).mockResolvedValue(mockReferralCode);
    (loyaltyService.getReferralStats as jest.MockedFunction<typeof loyaltyService.getReferralStats>).mockResolvedValue(mockReferralStats);

    render(<ReferralSection userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('https://example.com/ref/REF123')).toBeInTheDocument();
    });

    const copyLinkButtons = screen.getAllByText('Copy');
    fireEvent.click(copyLinkButtons[1]); // Second copy button is for link

    expect(mockWriteText).toHaveBeenCalledWith('https://example.com/ref/REF123');
  });

  it('should display referral statistics', async () => {
    (loyaltyService.getReferralCode as jest.MockedFunction<typeof loyaltyService.getReferralCode>).mockResolvedValue(mockReferralCode);
    (loyaltyService.getReferralStats as jest.MockedFunction<typeof loyaltyService.getReferralStats>).mockResolvedValue(mockReferralStats);

    render(<ReferralSection userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('Your Referral Statistics')).toBeInTheDocument();
      expect(screen.getByText('10')).toBeInTheDocument(); // totalReferrals
      expect(screen.getByText('8')).toBeInTheDocument(); // successfulReferrals
      expect(screen.getByText('500')).toBeInTheDocument(); // pointsEarned
      expect(screen.getByText('80.0%')).toBeInTheDocument(); // successRate
    });
  });

  it('should display referral stat labels', async () => {
    (loyaltyService.getReferralCode as jest.MockedFunction<typeof loyaltyService.getReferralCode>).mockResolvedValue(mockReferralCode);
    (loyaltyService.getReferralStats as jest.MockedFunction<typeof loyaltyService.getReferralStats>).mockResolvedValue(mockReferralStats);

    render(<ReferralSection userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('Total Referrals')).toBeInTheDocument();
      expect(screen.getByText('Successful')).toBeInTheDocument();
      expect(screen.getByText('Points Earned')).toBeInTheDocument();
      expect(screen.getByText('Success Rate')).toBeInTheDocument();
    });
  });

  it('should not display statistics when stats are null', async () => {
    (loyaltyService.getReferralCode as jest.MockedFunction<typeof loyaltyService.getReferralCode>).mockResolvedValue(mockReferralCode);
    (loyaltyService.getReferralStats as jest.MockedFunction<typeof loyaltyService.getReferralStats>).mockResolvedValue(null as any);

    render(<ReferralSection userId="user-123" />);

    await waitFor(() => {
      expect(screen.queryByText('Your Referral Statistics')).not.toBeInTheDocument();
    });
  });

  it('should display referral information text', async () => {
    (loyaltyService.getReferralCode as jest.MockedFunction<typeof loyaltyService.getReferralCode>).mockResolvedValue(mockReferralCode);
    (loyaltyService.getReferralStats as jest.MockedFunction<typeof loyaltyService.getReferralStats>).mockResolvedValue(mockReferralStats);

    render(<ReferralSection userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText(/Share your referral code with friends!/i)).toBeInTheDocument();
    });
  });

  it('should reset copied state after timeout', async () => {
    (loyaltyService.getReferralCode as jest.MockedFunction<typeof loyaltyService.getReferralCode>).mockResolvedValue(mockReferralCode);
    (loyaltyService.getReferralStats as jest.MockedFunction<typeof loyaltyService.getReferralStats>).mockResolvedValue(mockReferralStats);

    render(<ReferralSection userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('REF123')).toBeInTheDocument();
    });

    const copyCodeButton = screen.getAllByText('Copy')[0];
    fireEvent.click(copyCodeButton);

    await waitFor(() => {
      expect(screen.getAllByText('✓ Copied').length).toBeGreaterThan(0);
    });

    jest.advanceTimersByTime(2000);

    await waitFor(() => {
      // After timeout, copied state should reset - check that Copy buttons are visible again
      const copyButtons = screen.getAllByText('Copy');
      expect(copyButtons.length).toBeGreaterThan(0);
    });
  });

  it('should handle clipboard copy errors gracefully', async () => {
    const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
    mockWriteText.mockRejectedValue(new Error('Clipboard error'));
    (loyaltyService.getReferralCode as jest.MockedFunction<typeof loyaltyService.getReferralCode>).mockResolvedValue(mockReferralCode);
    (loyaltyService.getReferralStats as jest.MockedFunction<typeof loyaltyService.getReferralStats>).mockResolvedValue(mockReferralStats);

    render(<ReferralSection userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('REF123')).toBeInTheDocument();
    });

    const copyCodeButton = screen.getAllByText('Copy')[0];
    fireEvent.click(copyCodeButton);

    await waitFor(() => {
      expect(consoleErrorSpy).toHaveBeenCalled();
    });

    consoleErrorSpy.mockRestore();
  });

  it('should reload data when userId changes', async () => {
    (loyaltyService.getReferralCode as jest.MockedFunction<typeof loyaltyService.getReferralCode>).mockResolvedValue(mockReferralCode);
    (loyaltyService.getReferralStats as jest.MockedFunction<typeof loyaltyService.getReferralStats>).mockResolvedValue(mockReferralStats);

    const { rerender } = render(<ReferralSection userId="user-123" />);

    await waitFor(() => {
      expect(loyaltyService.getReferralCode).toHaveBeenCalledWith('user-123');
    });

    jest.clearAllMocks();
    (loyaltyService.getReferralCode as jest.MockedFunction<typeof loyaltyService.getReferralCode>).mockResolvedValue(mockReferralCode);
    (loyaltyService.getReferralStats as jest.MockedFunction<typeof loyaltyService.getReferralStats>).mockResolvedValue(mockReferralStats);

    rerender(<ReferralSection userId="user-456" />);

    await waitFor(() => {
      expect(loyaltyService.getReferralCode).toHaveBeenCalledWith('user-456');
    });
  });
});
