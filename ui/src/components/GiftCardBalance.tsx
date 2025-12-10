import React, { useState } from 'react';
import { giftCardService } from '../services/giftCardService';
import type { BalanceInquiryResponse, GiftCardTransaction } from '../services/giftCardService';
import './GiftCardBalance.css';

export const GiftCardBalance: React.FC = () => {
  const [code, setCode] = useState('');
  const [balance, setBalance] = useState<BalanceInquiryResponse | null>(null);
  const [transactions, setTransactions] = useState<GiftCardTransaction[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleCheckBalance = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!code.trim()) {
      setError('Please enter a gift card code');
      return;
    }

    setLoading(true);
    setError(null);
    setBalance(null);
    setTransactions([]);

    try {
      const balanceData = await giftCardService.checkBalance(code.trim());
      setBalance(balanceData);
      
      // Load transaction history if gift card found
      if (balanceData.code) {
        try {
          // We need giftCardId to get transactions, but balance inquiry doesn't return it
          // For now, we'll skip transaction history in balance inquiry
          // This can be enhanced later
        } catch (err) {
          // Silently fail for transaction history
        }
      }
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to check balance. Please verify the code.');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    try {
      return new Date(dateString).toLocaleDateString();
    } catch {
      return dateString;
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  };

  return (
    <div className="gift-card-balance">
      <h2>Check Gift Card Balance</h2>
      
      <form onSubmit={handleCheckBalance} className="balance-form">
        <div className="form-group">
          <label htmlFor="gift-card-code">Gift Card Code</label>
          <input
            id="gift-card-code"
            type="text"
            value={code}
            onChange={(e) => setCode(e.target.value.toUpperCase())}
            placeholder="Enter gift card code (e.g., ABCD-EFGH-IJKL-MNOP)"
            className="code-input"
            maxLength={19}
          />
        </div>
        
        <button type="submit" disabled={loading} className="check-balance-btn">
          {loading ? 'Checking...' : 'Check Balance'}
        </button>
      </form>

      {error && (
        <div className="error-message" role="alert">
          {error}
        </div>
      )}

      {balance && (
        <div className="balance-results">
          <div className="balance-card">
            <h3>Gift Card Information</h3>
            <div className="balance-details">
              <div className="balance-row">
                <span className="label">Code:</span>
                <span className="value code-value">{balance.code}</span>
              </div>
              <div className="balance-row">
                <span className="label">Balance:</span>
                <span className="value balance-value">{formatCurrency(balance.balance)}</span>
              </div>
              <div className="balance-row">
                <span className="label">Original Amount:</span>
                <span className="value">{formatCurrency(balance.amount)}</span>
              </div>
              <div className="balance-row">
                <span className="label">Status:</span>
                <span className={`value status status-${balance.status.toLowerCase()}`}>
                  {balance.status}
                </span>
              </div>
              <div className="balance-row">
                <span className="label">Expiration Date:</span>
                <span className="value">{formatDate(balance.expirationDate)}</span>
              </div>
            </div>
          </div>

          {transactions.length > 0 && (
            <div className="transactions-section">
              <h3>Transaction History</h3>
              <div className="transactions-list">
                {transactions.map((transaction) => (
                  <div key={transaction.transactionId} className="transaction-item">
                    <div className="transaction-type">{transaction.transactionType}</div>
                    <div className="transaction-amount">{formatCurrency(transaction.amount)}</div>
                    <div className="transaction-date">{formatDate(transaction.createdAt)}</div>
                    {transaction.description && (
                      <div className="transaction-description">{transaction.description}</div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
