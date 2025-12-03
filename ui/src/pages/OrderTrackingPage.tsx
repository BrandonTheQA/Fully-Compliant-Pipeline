import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useAppContext } from '../context/AppContext';
import { orderService } from '../services/orderService';
import { OrderTrackingHeader } from '../components/OrderTrackingHeader';
import { OrderStatusTimeline } from '../components/OrderStatusTimeline';
import { OrderDetailsCard } from '../components/OrderDetailsCard';
import { NotificationPreferences } from '../components/NotificationPreferences';
import type { OrderTracking, Order } from '../types';
import './OrderTrackingPage.css';

export const OrderTrackingPage: React.FC = () => {
  const { orderId } = useParams<{ orderId: string }>();
  const { user } = useAppContext();
  const [tracking, setTracking] = useState<OrderTracking | null>(null);
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    if (orderId) {
      loadTrackingData();
      subscribeToUpdates();
    }

    return () => {
      // Cleanup SSE connection if needed
    };
  }, [orderId]);

  const loadTrackingData = async () => {
    if (!orderId) return;

    setLoading(true);
    setError(null);

    try {
      const [trackingData, orderData] = await Promise.all([
        orderService.getOrderTracking(orderId),
        orderService.getOrder(orderId).catch(() => null)
      ]);

      setTracking(trackingData);
      setOrder(orderData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load tracking information');
    } finally {
      setLoading(false);
    }
  };

  const subscribeToUpdates = () => {
    if (!orderId) return;

    const eventSource = orderService.subscribeToOrderUpdates(orderId, () => {
      setConnected(true);
      // Reload tracking data when status update is received
      loadTrackingData();
    });

    setConnected(true);

    return () => {
      eventSource.close();
    };
  };

  if (loading) {
    return (
      <div className="page-container">
        <div className="loading" role="status" aria-live="polite" aria-busy="true">
          Loading order tracking information...
        </div>
      </div>
    );
  }

  if (error || !tracking) {
    return (
      <div className="page-container">
        <div className="error-message">
          {error || 'Order not found'}
        </div>
        <Link to="/orders" className="btn btn-secondary">
          ← Back to Orders
        </Link>
      </div>
    );
  }

  return (
    <div className="page-container">
      <div className="tracking-page-header">
        <Link to="/orders" className="btn btn-secondary">
          ← Back to Orders
        </Link>
        {connected && (
          <div className="live-indicator" role="status" aria-live="polite" aria-label="Live updates enabled">
            <span className="live-dot" aria-hidden="true"></span>
            Live Updates
          </div>
        )}
      </div>

      <OrderTrackingHeader tracking={tracking} />
      
      <div className="tracking-content">
        <div className="tracking-main">
          <OrderStatusTimeline 
            statusHistory={tracking.statusHistory} 
            currentStatus={tracking.status}
          />
          <OrderDetailsCard order={order || undefined} tracking={tracking} />
        </div>

        {user && (
          <div className="tracking-sidebar">
            <NotificationPreferences userId={user.userId} />
          </div>
        )}
      </div>
    </div>
  );
};
