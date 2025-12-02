import React, { useState, useEffect } from 'react';
import type { RedeemPointsResponse } from '../types';
import './PointRedemptionForm.css';

interface PointRedemptionFormProps {
  userId: string;
  currentBalance: number;
  orderTotal?: number;
  orderId?: string;
  onRedemptionSuccess?: (response: RedeemPointsResponse) => void;
  onError?: (error: string) => void;
}

export const PointRedemptionForm: React.FC<PointRedemptionFormProps> = ({
  currentBalance,
  orderTotal,
  onRedemptionSuccess,
}) => {
  const [pointsToRedeem, setPointsToRedeem] = useState<number>(0);
  const [discountAmount, setDiscountAmount] = useState<number>(0);
  const [error, setError] = useState<string | null>(null);

  const MIN_REDEMPTION = 500;
  const REDEMPTION_RATE = 100; // 100 points = $1

  useEffect(() => {
    calculateDiscount();
  }, [pointsToRedeem]);

  const calculateDiscount = () => {
    if (pointsToRedeem >= MIN_REDEMPTION && pointsToRedeem <= currentBalance) {
      let calculatedDiscount = pointsToRedeem / REDEMPTION_RATE;
      
      // Apply max 50% of order value limit
      if (orderTotal) {
        const maxDiscount = orderTotal * 0.5;
        calculatedDiscount = Math.min(calculatedDiscount, maxDiscount);
      }
      
      setDiscountAmount(calculatedDiscount);
    } else {
      setDiscountAmount(0);
    }
  };

  const handleRedeem = () => {
    if (pointsToRedeem < MIN_REDEMPTION) {
      setError(`Minimum redemption is ${MIN_REDEMPTION} points`);
      return;
    }

    if (pointsToRedeem > currentBalance) {
      setError('Insufficient points');
      return;
    }

    // Calculate discount and notify parent
    // Actual redemption will happen during order creation
    if (onRedemptionSuccess) {
      const mockResponse: RedeemPointsResponse = {
        pointsRedeemed: pointsToRedeem,
        discountAmount: discountAmount,
        remainingBalance: currentBalance - pointsToRedeem,
        message: `Will redeem ${pointsToRedeem} points for $${discountAmount.toFixed(2)} discount`,
      };
      onRedemptionSuccess(mockResponse);
    }
  };

  const handleQuickRedeem = (points: number) => {
    if (points <= currentBalance && points >= MIN_REDEMPTION) {
      setPointsToRedeem(points);
    }
  };

  return (
    <div className="point-redemption-form">
      <h3 className="point-redemption-form-title">Redeem Points</h3>
      <div className="point-redemption-form-balance">
        Available: {currentBalance.toLocaleString()} points
      </div>

      <div className="point-redemption-form-input-group">
        <label htmlFor="points-input">Points to redeem:</label>
        <input
          id="points-input"
          type="number"
          min={MIN_REDEMPTION}
          max={currentBalance}
          value={pointsToRedeem || ''}
          onChange={(e) => setPointsToRedeem(parseInt(e.target.value) || 0)}
          placeholder={`Minimum: ${MIN_REDEMPTION}`}
        />
      </div>

      {discountAmount > 0 && (
        <div className="point-redemption-form-discount">
          Discount: ${discountAmount.toFixed(2)}
        </div>
      )}

      {error && (
        <div className="point-redemption-form-error">{error}</div>
      )}

      <div className="point-redemption-form-quick-options">
        <button
          type="button"
          onClick={() => handleQuickRedeem(500)}
          disabled={currentBalance < 500}
          className="point-redemption-form-quick-btn"
        >
          500 pts
        </button>
        <button
          type="button"
          onClick={() => handleQuickRedeem(1000)}
          disabled={currentBalance < 1000}
          className="point-redemption-form-quick-btn"
        >
          1,000 pts
        </button>
        <button
          type="button"
          onClick={() => handleQuickRedeem(Math.min(currentBalance, Math.floor(currentBalance / 100) * 100))}
          disabled={currentBalance < MIN_REDEMPTION}
          className="point-redemption-form-quick-btn"
        >
          Max
        </button>
      </div>

      <button
        type="button"
        onClick={handleRedeem}
        disabled={pointsToRedeem < MIN_REDEMPTION || pointsToRedeem > currentBalance}
        className="point-redemption-form-submit"
      >
        Apply Points Discount
      </button>
    </div>
  );
};
