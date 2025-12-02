import React from 'react';
import type { LoyaltyTier } from '../types';
import './LoyaltyTierBadge.css';

interface LoyaltyTierBadgeProps {
  tier: LoyaltyTier;
  pointsToNextTier?: number;
  currentPoints?: number;
}

export const LoyaltyTierBadge: React.FC<LoyaltyTierBadgeProps> = ({ 
  tier, 
  pointsToNextTier = 0
}) => {
  const getTierColor = (tier: LoyaltyTier): string => {
    switch (tier) {
      case 'PLATINUM':
        return '#e8e8e8';
      case 'GOLD':
        return '#ffd700';
      case 'SILVER':
        return '#c0c0c0';
      case 'BRONZE':
      default:
        return '#cd7f32';
    }
  };

  const getTierLabel = (tier: LoyaltyTier): string => {
    return tier.charAt(0) + tier.slice(1).toLowerCase();
  };

  const tierColor = getTierColor(tier);
  const tierLabel = getTierLabel(tier);

  return (
    <div className="loyalty-tier-badge">
      <div 
        className="loyalty-tier-badge-icon" 
        style={{ backgroundColor: tierColor }}
        aria-label={`${tierLabel} tier`}
      >
        {tier.charAt(0)}
      </div>
      <div className="loyalty-tier-badge-info">
        <div className="loyalty-tier-badge-label">{tierLabel} Member</div>
        {pointsToNextTier > 0 && (
          <div className="loyalty-tier-badge-progress">
            {pointsToNextTier.toLocaleString()} points to next tier
          </div>
        )}
      </div>
    </div>
  );
};
