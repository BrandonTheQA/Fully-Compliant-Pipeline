import React, { useEffect, useState } from 'react';
import { loyaltyService } from '../services/loyaltyService';
import type { LoyaltyAccount } from '../types';
import './LoyaltyBalance.css';

interface LoyaltyBalanceProps {
  userId: string;
  onBalanceChange?: (balance: number) => void;
}

export const LoyaltyBalance: React.FC<LoyaltyBalanceProps> = ({ userId, onBalanceChange }) => {
  const [account, setAccount] = useState<LoyaltyAccount | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadBalance();
  }, [userId]);

  const loadBalance = async () => {
    try {
      setLoading(true);
      setError(null);
      const balance = await loyaltyService.getBalance(userId);
      setAccount(balance);
      if (onBalanceChange) {
        onBalanceChange(balance.currentPoints);
      }
    } catch (err) {
      setError('Failed to load loyalty balance');
      console.error('Error loading loyalty balance:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loyalty-balance loading">Loading...</div>;
  }

  if (error || !account) {
    return null; // Don't show error, just don't display balance
  }

  return (
    <div className="loyalty-balance">
      <span className="loyalty-balance-label">Points:</span>
      <span className="loyalty-balance-value">{account.currentPoints.toLocaleString()}</span>
    </div>
  );
};
