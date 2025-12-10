import React from 'react';
import { GiftCardBalance } from '../components/GiftCardBalance';
import './GiftCardBalancePage.css';

export const GiftCardBalancePage: React.FC = () => {
  return (
    <div className="gift-card-balance-page">
      <GiftCardBalance />
    </div>
  );
};
