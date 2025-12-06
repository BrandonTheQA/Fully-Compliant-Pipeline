import React from 'react';
import type { Product } from '../types';
import './LowStockWarning.css';

interface LowStockWarningProps {
  product: Product;
}

export const LowStockWarning: React.FC<LowStockWarningProps> = ({ product }) => {
  const stockStatus = product.stockStatus || 
    (product.quantity > 0 ? 'IN_STOCK' : 'OUT_OF_STOCK');

  if (stockStatus !== 'LOW_STOCK') {
    return null;
  }

  return (
    <div className="low-stock-warning" role="alert" aria-live="assertive">
      <span className="low-stock-warning-icon">⚠️</span>
      <span className="low-stock-warning-message">
        Only {product.quantity} left in stock - order soon!
      </span>
    </div>
  );
};

