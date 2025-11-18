import React from 'react';
import { ShippingBanner } from './ShippingBanner';
import './ShippingCostCalculator.css';

interface ShippingCostCalculatorProps {
  cartTotal: number;
  region: string;
  shippingCost: number;
  freeShippingThreshold: number;
  remainingAmount: number;
  qualifiesForFreeShipping: boolean;
}

export const ShippingCostCalculator: React.FC<ShippingCostCalculatorProps> = ({
  cartTotal,
  region: _region,
  shippingCost,
  freeShippingThreshold,
  remainingAmount: _remainingAmount,
  qualifiesForFreeShipping,
}) => {
  const totalWithShipping = cartTotal + shippingCost;

  return (
    <div className="shipping-cost-calculator">
      <div className="shipping-cost-section">
        <div className="shipping-cost-header">
          <h3>Shipping & Costs</h3>
        </div>
        
        <div className={`shipping-cost-display ${qualifiesForFreeShipping ? 'shipping-free' : ''}`}>
          <div className="shipping-cost-label">Estimated Shipping:</div>
          <div className="shipping-cost-value">
            {qualifiesForFreeShipping ? (
              <>
                <span className="shipping-free-text">FREE</span>
                <span className="shipping-free-icon">🎉</span>
              </>
            ) : (
              `$${shippingCost.toFixed(2)}`
            )}
          </div>
        </div>

        {!qualifiesForFreeShipping && (
          <div className="shipping-progress-wrapper">
            <ShippingBanner
              cartTotal={cartTotal}
              region={_region}
              threshold={freeShippingThreshold}
            />
          </div>
        )}
      </div>

      <div className="cost-breakdown">
        <div className="cost-row">
          <span className="cost-label">Subtotal:</span>
          <span className="cost-value">${cartTotal.toFixed(2)}</span>
        </div>
        <div className="cost-row">
          <span className="cost-label">Shipping:</span>
          <span className={`cost-value ${qualifiesForFreeShipping ? 'shipping-free-text' : ''}`}>
            {qualifiesForFreeShipping ? 'FREE' : `$${shippingCost.toFixed(2)}`}
          </span>
        </div>
        <div className="cost-row cost-total">
          <span className="cost-label">Total:</span>
          <span className="cost-value total-amount">${totalWithShipping.toFixed(2)}</span>
        </div>
      </div>
    </div>
  );
};





