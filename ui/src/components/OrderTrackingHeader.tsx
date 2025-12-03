import React from 'react';
import type { OrderTracking } from '../types';
import './OrderTrackingHeader.css';

interface OrderTrackingHeaderProps {
  tracking: OrderTracking;
}

export const OrderTrackingHeader: React.FC<OrderTrackingHeaderProps> = ({ tracking }) => {
  const formatDate = (dateString?: string) => {
    if (!dateString) return 'Not available';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getStatusColor = (status: string) => {
    const statusUpper = status.toUpperCase();
    if (statusUpper === 'DELIVERED') return 'status-delivered';
    if (statusUpper === 'SHIPPED' || statusUpper === 'IN_TRANSIT' || statusUpper === 'OUT_FOR_DELIVERY') return 'status-shipped';
    if (statusUpper === 'PROCESSING' || statusUpper === 'CONFIRMED') return 'status-processing';
    return 'status-pending';
  };

  return (
    <div className="tracking-header">
      <div className="tracking-header-main">
        <div className="order-info">
          <h2>Order #{tracking.orderId.slice(0, 8)}</h2>
          <div 
            className={`status-badge ${getStatusColor(tracking.status)}`}
            aria-label={`Order status: ${tracking.status.replace('_', ' ')}`}
          >
            {tracking.status.replace('_', ' ')}
          </div>
        </div>
        
        {tracking.trackingNumber && (
          <div className="tracking-number">
            <strong>Tracking Number:</strong>
            <span className="tracking-code">{tracking.trackingNumber}</span>
            {tracking.carrierName && (
              <span className="carrier-name">via {tracking.carrierName}</span>
            )}
          </div>
        )}
      </div>

      {tracking.estimatedDeliveryDate && (
        <div className="delivery-info">
          <strong>Estimated Delivery:</strong>
          <span>{formatDate(tracking.estimatedDeliveryDate)}</span>
        </div>
      )}
    </div>
  );
};
