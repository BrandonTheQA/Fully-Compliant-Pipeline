import React, { useEffect, useState } from 'react';
import { loyaltyService } from '../services/loyaltyService';
import type { ReferralStats } from '../types';
import './ReferralSection.css';

interface ReferralSectionProps {
  userId: string;
}

export const ReferralSection: React.FC<ReferralSectionProps> = ({ userId }) => {
  const [referralCode, setReferralCode] = useState<string>('');
  const [referralLink, setReferralLink] = useState<string>('');
  const [stats, setStats] = useState<ReferralStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    loadReferralData();
  }, [userId]);

  const loadReferralData = async () => {
    try {
      setLoading(true);
      const codeData = await loyaltyService.getReferralCode(userId);
      setReferralCode(codeData.referralCode);
      setReferralLink(codeData.referralLink);

      const referralStats = await loyaltyService.getReferralStats(userId);
      setStats(referralStats);
    } catch (err) {
      console.error('Error loading referral data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCopyCode = async () => {
    try {
      await navigator.clipboard.writeText(referralCode);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error('Failed to copy:', err);
    }
  };

  const handleCopyLink = async () => {
    try {
      await navigator.clipboard.writeText(referralLink);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error('Failed to copy:', err);
    }
  };

  if (loading) {
    return <div className="referral-section loading">Loading referral information...</div>;
  }

  return (
    <div className="referral-section">
      <h3 className="referral-section-title">Referral Program</h3>
      
      <div className="referral-section-code">
        <label>Your Referral Code:</label>
        <div className="referral-section-code-display">
          <span className="referral-section-code-value">{referralCode}</span>
          <button
            type="button"
            onClick={handleCopyCode}
            className="referral-section-copy-btn"
            aria-label="Copy referral code"
          >
            {copied ? '✓ Copied' : 'Copy'}
          </button>
        </div>
      </div>

      <div className="referral-section-link">
        <label>Your Referral Link:</label>
        <div className="referral-section-link-display">
          <span className="referral-section-link-value">{referralLink}</span>
          <button
            type="button"
            onClick={handleCopyLink}
            className="referral-section-copy-btn"
            aria-label="Copy referral link"
          >
            {copied ? '✓ Copied' : 'Copy'}
          </button>
        </div>
      </div>

      {stats && (
        <div className="referral-section-stats">
          <h4>Your Referral Statistics</h4>
          <div className="referral-section-stats-grid">
            <div className="referral-section-stat">
              <div className="referral-section-stat-value">{stats.totalReferrals}</div>
              <div className="referral-section-stat-label">Total Referrals</div>
            </div>
            <div className="referral-section-stat">
              <div className="referral-section-stat-value">{stats.successfulReferrals}</div>
              <div className="referral-section-stat-label">Successful</div>
            </div>
            <div className="referral-section-stat">
              <div className="referral-section-stat-value">{stats.pointsEarned}</div>
              <div className="referral-section-stat-label">Points Earned</div>
            </div>
            <div className="referral-section-stat">
              <div className="referral-section-stat-value">{stats.successRate.toFixed(1)}%</div>
              <div className="referral-section-stat-label">Success Rate</div>
            </div>
          </div>
        </div>
      )}

      <div className="referral-section-info">
        <p>Share your referral code with friends! When they sign up and make their first purchase, you'll both earn bonus points.</p>
      </div>
    </div>
  );
};
