import React from 'react';
import type { Product } from '../types';
import './StockStatusBadge.css';

interface StockStatusBadgeProps {
  product: Product;
}

export const StockStatusBadge: React.FC<StockStatusBadgeProps> = ({ product }) => {
  const stockStatus = product.stockStatus || 
    (product.quantity > 0 ? 'IN_STOCK' : 'OUT_OF_STOCK');

  const getStatusClass = () => {
    switch (stockStatus) {
      case 'IN_STOCK':
        return 'stock-status-in-stock';
      case 'LOW_STOCK':
        return 'stock-status-low-stock';
      case 'OUT_OF_STOCK':
        return 'stock-status-out-of-stock';
      default:
        return 'stock-status-unknown';
    }
  };

  const getStatusText = () => {
    switch (stockStatus) {
      case 'IN_STOCK':
        return 'In Stock';
      case 'LOW_STOCK':
        return `Low Stock - Only ${product.quantity} left!`;
      case 'OUT_OF_STOCK':
        return 'Out of Stock';
      default:
        return 'Unknown';
    }
  };

  return (
    <span className={`stock-status-badge ${getStatusClass()}`} role="status" aria-live="polite">
      {getStatusText()}
    </span>
  );
};

