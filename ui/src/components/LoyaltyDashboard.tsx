import React, { useEffect, useState } from 'react';
import { loyaltyService } from '../services/loyaltyService';
import { LoyaltyTierBadge } from './LoyaltyTierBadge';
import { ReferralSection } from './ReferralSection';
import type { LoyaltyDashboard as LoyaltyDashboardType } from '../types';
import './LoyaltyDashboard.css';

interface LoyaltyDashboardProps {
  userId: string;
}

export const LoyaltyDashboard: React.FC<LoyaltyDashboardProps> = ({ userId }) => {
  const [dashboard, setDashboard] = useState<LoyaltyDashboardType | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadDashboard();
  }, [userId]);

  const loadDashboard = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await loyaltyService.getDashboard(userId);
      setDashboard(data);
    } catch (err) {
      setError('Failed to load loyalty dashboard');
      console.error('Error loading loyalty dashboard:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="loyalty-dashboard loading">
        <div>Loading loyalty dashboard...</div>
      </div>
    );
  }

  if (error || !dashboard) {
    return (
      <div className="loyalty-dashboard error">
        <div>{error || 'Failed to load loyalty dashboard'}</div>
        <button onClick={loadDashboard} className="loyalty-dashboard-retry-btn">
          Retry
        </button>
      </div>
    );
  }

  const { account, recentTransactions, pointsToNextTier, expiringPoints, expiringPointsDate, tierBenefits } = dashboard;

  return (
    <div className="loyalty-dashboard">
      <div className="loyalty-dashboard-header">
        <h2 className="loyalty-dashboard-title">Loyalty Program</h2>
      </div>

      <div className="loyalty-dashboard-main">
        <div className="loyalty-dashboard-left">
          {/* Balance and Tier Section */}
          <div className="loyalty-dashboard-section">
            <div className="loyalty-dashboard-balance-display">
              <div className="loyalty-dashboard-balance-large">
                <span className="loyalty-dashboard-balance-label">Current Balance</span>
                <span className="loyalty-dashboard-balance-value">
                  {account.currentPoints.toLocaleString()} points
                </span>
              </div>
            </div>

            <LoyaltyTierBadge
              tier={account.currentTier}
              pointsToNextTier={pointsToNextTier}
              currentPoints={account.lifetimePointsEarned}
            />

            {pointsToNextTier > 0 && (
              <div className="loyalty-dashboard-progress">
                <div className="loyalty-dashboard-progress-label">
                  {pointsToNextTier.toLocaleString()} points until next tier
                </div>
                <div className="loyalty-dashboard-progress-bar-container">
                  <div
                    className="loyalty-dashboard-progress-bar"
                    style={{
                      width: `${Math.min(100, ((account.lifetimePointsEarned % 1000) / 1000) * 100)}%`
                    }}
                  />
                </div>
              </div>
            )}
          </div>

          {/* Tier Benefits */}
          <div className="loyalty-dashboard-section">
            <h3 className="loyalty-dashboard-section-title">Tier Benefits</h3>
            <div className="loyalty-dashboard-benefits">
              <div className="loyalty-dashboard-benefit-multiplier">
                {tierBenefits.multiplier}x Points Multiplier
              </div>
              <ul className="loyalty-dashboard-benefits-list">
                {tierBenefits.benefits.map((benefit, index) => (
                  <li key={index}>{benefit}</li>
                ))}
              </ul>
            </div>
          </div>

          {/* Expiration Warning */}
          {expiringPoints && expiringPoints > 0 && (
            <div className="loyalty-dashboard-section loyalty-dashboard-expiration-warning">
              <h3 className="loyalty-dashboard-section-title">Points Expiring Soon</h3>
              <div className="loyalty-dashboard-expiration-message">
                <strong>{expiringPoints.toLocaleString()} points</strong> will expire on{' '}
                {expiringPointsDate || 'soon'}. Use them before they expire!
              </div>
            </div>
          )}

          {/* Lifetime Stats */}
          <div className="loyalty-dashboard-section">
            <h3 className="loyalty-dashboard-section-title">Lifetime Statistics</h3>
            <div className="loyalty-dashboard-stats">
              <div className="loyalty-dashboard-stat">
                <div className="loyalty-dashboard-stat-label">Total Points Earned</div>
                <div className="loyalty-dashboard-stat-value">
                  {account.lifetimePointsEarned.toLocaleString()}
                </div>
              </div>
              <div className="loyalty-dashboard-stat">
                <div className="loyalty-dashboard-stat-label">Total Points Redeemed</div>
                <div className="loyalty-dashboard-stat-value">
                  {account.lifetimePointsRedeemed.toLocaleString()}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="loyalty-dashboard-right">
          {/* Recent Transactions */}
          <div className="loyalty-dashboard-section">
            <h3 className="loyalty-dashboard-section-title">Recent Activity</h3>
            {recentTransactions.length === 0 ? (
              <div className="loyalty-dashboard-no-transactions">
                No recent transactions
              </div>
            ) : (
              <div className="loyalty-dashboard-transactions">
                {recentTransactions.map((transaction) => (
                  <div key={transaction.transactionId} className="loyalty-dashboard-transaction">
                    <div className="loyalty-dashboard-transaction-main">
                      <div className="loyalty-dashboard-transaction-type">
                        {transaction.transactionType === 'EARNED' ? '+' : '-'}
                        {Math.abs(transaction.points).toLocaleString()} points
                      </div>
                      <div className="loyalty-dashboard-transaction-description">
                        {transaction.description || transaction.activityType}
                      </div>
                    </div>
                    <div className="loyalty-dashboard-transaction-date">
                      {new Date(transaction.createdAt).toLocaleDateString()}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Referral Section */}
          <ReferralSection userId={userId} />
        </div>
      </div>
    </div>
  );
};
