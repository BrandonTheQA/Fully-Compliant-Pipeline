import React from 'react';
import type { Order, OrderTracking } from '../types';
import './OrderDetailsCard.css';

interface OrderDetailsCardProps {
  order?: Order;
  tracking: OrderTracking;
}

export const OrderDetailsCard: React.FC<OrderDetailsCardProps> = ({ order, tracking }) => {
  return (
    <div className="order-details-card">
      <h3>Order Details</h3>
      
      {order && order.items && order.items.length > 0 && (
        <div className="order-items-section">
          <h4>Items</h4>
          <div className="items-list">
            {order.items.map((item, index) => (
              <div key={index} className="order-item">
                <div className="item-info">
                  <span className="item-name">{item.productName || `Product ${item.productId.slice(0, 8)}`}</span>
                  <span className="item-quantity">Qty: {item.quantity}</span>
                </div>
                <div className="item-price">
                  ${item.subtotal ? item.subtotal.toFixed(2) : (item.price ? (item.price * item.quantity).toFixed(2) : '0.00')}
                </div>
              </div>
            ))}
          </div>
          {order.totalAmount && (
            <div className="order-total">
              <strong>Total: ${order.totalAmount.toFixed(2)}</strong>
            </div>
          )}
        </div>
      )}

      {tracking.shippingAddress && (
        <div className="shipping-section">
          <h4>Shipping Address</h4>
          <p className="shipping-address">{tracking.shippingAddress}</p>
        </div>
      )}

      {tracking.shippingMethod && (
        <div className="shipping-method">
          <strong>Shipping Method:</strong> {tracking.shippingMethod}
        </div>
      )}

      {tracking.currentLocation && (
        <div className="current-location">
          <strong>Current Location:</strong> {tracking.currentLocation}
        </div>
      )}
    </div>
  );
};
