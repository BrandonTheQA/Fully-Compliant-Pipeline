import React from 'react';
import './ShippingBanner.css';

interface ShippingBannerProps {
  cartTotal: number;
  region: string;
  threshold: number;
}

export const ShippingBanner: React.FC<ShippingBannerProps> = ({
  cartTotal,
  region: _region,
  threshold,
}) => {
  const qualifiesForFreeShipping = cartTotal >= threshold;
  const remainingAmount = Math.max(0, threshold - cartTotal);
  const progressPercentage = Math.min(100, (cartTotal / threshold) * 100);

  return (
    <div className={`shipping-banner ${qualifiesForFreeShipping ? 'shipping-banner-success' : 'shipping-banner-info'}`}>
      <div className="shipping-banner-content">
        {qualifiesForFreeShipping ? (
          <div className="shipping-banner-message shipping-banner-success-message">
            <span className="shipping-banner-icon">🎉</span>
            <span>You've qualified for FREE shipping!</span>
          </div>
        ) : (
          <div className="shipping-banner-message">
            <span>Add ${remainingAmount.toFixed(2)} more to qualify for FREE shipping!</span>
          </div>
        )}
      </div>
      {!qualifiesForFreeShipping && (
        <div className="shipping-banner-progress">
          <div 
            className="shipping-banner-progress-bar"
            style={{ width: `${progressPercentage}%` }}
            role="progressbar"
            aria-valuenow={progressPercentage}
            aria-valuemin={0}
            aria-valuemax={100}
            aria-label={`${progressPercentage.toFixed(0)}% towards free shipping`}
          />
        </div>
      )}
    </div>
  );
};

