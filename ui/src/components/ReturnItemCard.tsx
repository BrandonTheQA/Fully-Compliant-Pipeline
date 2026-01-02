import React from 'react';
import type { ReturnItem } from '../types';
import './ReturnItemCard.css';

interface ReturnItemCardProps {
  item: ReturnItem;
}

export const ReturnItemCard: React.FC<ReturnItemCardProps> = ({ item }) => {
  return (
    <div className="return-item-card">
      <h4>{item.productName}</h4>
      <div className="return-item-details">
        <p><strong>Product ID:</strong> {item.productId}</p>
        <p><strong>Quantity:</strong> {item.quantity}</p>
        <p><strong>Return Reason:</strong> {item.returnReason.replace('_', ' ')}</p>
        {item.condition && <p><strong>Condition:</strong> {item.condition}</p>}
        {item.comments && <p><strong>Comments:</strong> {item.comments}</p>}
        <p><strong>Original Price:</strong> ${item.originalPrice.toFixed(2)}</p>
        {item.refundAmount && (
          <p><strong>Refund Amount:</strong> ${item.refundAmount.toFixed(2)}</p>
        )}
      </div>
    </div>
  );
};

