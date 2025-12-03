/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect } from '@jest/globals';
import { render, screen } from '@testing-library/react';
import { LoyaltyTierBadge } from '../LoyaltyTierBadge';
import type { LoyaltyTier } from '../../types';

describe('LoyaltyTierBadge', () => {
  it('should render BRONZE tier badge', () => {
    render(<LoyaltyTierBadge tier="BRONZE" />);
    
    expect(screen.getByText('Bronze Member')).toBeInTheDocument();
    expect(screen.getByText('B')).toBeInTheDocument();
  });

  it('should render SILVER tier badge', () => {
    render(<LoyaltyTierBadge tier="SILVER" />);
    
    expect(screen.getByText('Silver Member')).toBeInTheDocument();
    expect(screen.getByText('S')).toBeInTheDocument();
  });

  it('should render GOLD tier badge', () => {
    render(<LoyaltyTierBadge tier="GOLD" />);
    
    expect(screen.getByText('Gold Member')).toBeInTheDocument();
    expect(screen.getByText('G')).toBeInTheDocument();
  });

  it('should render PLATINUM tier badge', () => {
    render(<LoyaltyTierBadge tier="PLATINUM" />);
    
    expect(screen.getByText('Platinum Member')).toBeInTheDocument();
    expect(screen.getByText('P')).toBeInTheDocument();
  });

  it('should display points to next tier when provided', () => {
    render(<LoyaltyTierBadge tier="BRONZE" pointsToNextTier={500} />);
    
    expect(screen.getByText(/500 points to next tier/i)).toBeInTheDocument();
  });

  it('should not display points to next tier when zero', () => {
    render(<LoyaltyTierBadge tier="PLATINUM" pointsToNextTier={0} />);
    
    expect(screen.queryByText(/points to next tier/i)).not.toBeInTheDocument();
  });

  it('should not display points to next tier when not provided', () => {
    render(<LoyaltyTierBadge tier="GOLD" />);
    
    expect(screen.queryByText(/points to next tier/i)).not.toBeInTheDocument();
  });

  it('should apply correct color for BRONZE tier', () => {
    render(<LoyaltyTierBadge tier="BRONZE" />);
    
    const badgeIcon = screen.getByText('B').closest('.loyalty-tier-badge-icon');
    expect(badgeIcon).toHaveStyle({ backgroundColor: '#cd7f32' });
  });

  it('should apply correct color for SILVER tier', () => {
    render(<LoyaltyTierBadge tier="SILVER" />);
    
    const badgeIcon = screen.getByText('S').closest('.loyalty-tier-badge-icon');
    expect(badgeIcon).toHaveStyle({ backgroundColor: '#c0c0c0' });
  });

  it('should apply correct color for GOLD tier', () => {
    render(<LoyaltyTierBadge tier="GOLD" />);
    
    const badgeIcon = screen.getByText('G').closest('.loyalty-tier-badge-icon');
    expect(badgeIcon).toHaveStyle({ backgroundColor: '#ffd700' });
  });

  it('should apply correct color for PLATINUM tier', () => {
    render(<LoyaltyTierBadge tier="PLATINUM" />);
    
    const badgeIcon = screen.getByText('P').closest('.loyalty-tier-badge-icon');
    expect(badgeIcon).toHaveStyle({ backgroundColor: '#e8e8e8' });
  });

  it('should format tier label correctly', () => {
    render(<LoyaltyTierBadge tier="BRONZE" />);
    
    expect(screen.getByText('Bronze Member')).toBeInTheDocument();
  });

  it('should have accessible aria-label', () => {
    render(<LoyaltyTierBadge tier="GOLD" />);
    
    const badgeIcon = screen.getByText('G').closest('.loyalty-tier-badge-icon');
    expect(badgeIcon).toHaveAttribute('aria-label', 'Gold tier');
  });

  it('should handle default tier (BRONZE) for unknown tier', () => {
    render(<LoyaltyTierBadge tier={'UNKNOWN' as LoyaltyTier} />);
    
    // Should default to BRONZE color
    const badgeIcon = screen.getByText('U').closest('.loyalty-tier-badge-icon');
    expect(badgeIcon).toHaveStyle({ backgroundColor: '#cd7f32' });
  });

  it('should display formatted points to next tier with commas', () => {
    render(<LoyaltyTierBadge tier="SILVER" pointsToNextTier={1500} />);
    
    expect(screen.getByText(/1,500 points to next tier/i)).toBeInTheDocument();
  });
});
