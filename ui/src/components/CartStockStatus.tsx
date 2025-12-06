import React from 'react';
import type { CartItem } from '../types';
import { StockStatusBadge } from './StockStatusBadge';
import { LowStockWarning } from './LowStockWarning';
import { NotifyMeButton } from './NotifyMeButton';
import './CartStockStatus.css';

interface CartStockStatusProps {
  item: CartItem;
  onRemove?: () => void;
}

export const CartStockStatus: React.FC<CartStockStatusProps> = ({ item, onRemove }) => {
  const stockStatus = item.stockStatus || 
    (item.quantity > 0 ? 'IN_STOCK' : 'OUT_OF_STOCK');

  const isOutOfStock = stockStatus === 'OUT_OF_STOCK';
  const isLowStock = stockStatus === 'LOW_STOCK';

  return (
    <div className="cart-stock-status">
      <div className="cart-stock-status-header">
        <StockStatusBadge product={item} />
      </div>
      {isLowStock && <LowStockWarning product={item} />}
      {isOutOfStock && (
        <div className="cart-stock-out-of-stock">
          <p className="out-of-stock-message">
            This item is currently out of stock and cannot be purchased.
          </p>
          <div className="out-of-stock-actions">
            {onRemove && (
              <button
                onClick={onRemove}
                className="btn btn-secondary"
                type="button"
              >
                Remove from Cart
              </button>
            )}
            <NotifyMeButton productId={item.id} productName={item.name} />
          </div>
        </div>
      )}
    </div>
  );
};

