import React from 'react';
import { GiftCardPurchase } from '../components/GiftCardPurchase';
import './GiftCardPurchasePage.css';

export const GiftCardPurchasePage: React.FC = () => {
  return (
    <div className="gift-card-purchase-page">
      <GiftCardPurchase />
    </div>
  );
};
