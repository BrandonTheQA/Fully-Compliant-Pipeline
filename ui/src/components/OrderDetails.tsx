import React from 'react';
import type { Order } from '../types';
import './OrderDetails.css';

interface OrderDetailsProps {
  order: Order;
}

export const OrderDetails: React.FC<OrderDetailsProps> = ({ order }) => {
  return (
    <div className="order-details-container">
      <h2>Order Details</h2>
      <div className="order-info">
        <div className="info-row">
          <span className="label">Order ID:</span>
          <span className="value">{order.id}</span>
        </div>
        <div className="info-row">
          <span className="label">User ID:</span>
          <span className="value">{order.userId}</span>
        </div>
        <div className="info-row">
          <span className="label">Status:</span>
          <span className="value status-badge">{order.status}</span>
        </div>
        <div className="info-row">
          <span className="label">Total Amount:</span>
          <span className="value total">${order.totalAmount.toFixed(2)}</span>
        </div>
        {order.createdAt && (
          <div className="info-row">
            <span className="label">Created At:</span>
            <span className="value">{new Date(order.createdAt).toLocaleString()}</span>
          </div>
        )}
      </div>

      <div className="order-items">
        <h3>Order Items</h3>
        <table className="items-table">
          <thead>
            <tr>
              <th>Product Name</th>
              <th>Quantity</th>
              <th>Price</th>
              <th>Subtotal</th>
            </tr>
          </thead>
          <tbody>
            {order.items.map((item, index) => (
              <tr key={index}>
                <td>{item.productName || item.productId}</td>
                <td>{item.quantity}</td>
                <td>${item.price?.toFixed(2) || '0.00'}</td>
                <td>${item.subtotal?.toFixed(2) || '0.00'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

