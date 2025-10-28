import React, { useState, useEffect } from 'react';
import { useAppContext } from '../context/AppContext';
import { orderService } from '../services/orderService';
import { OrderForm } from '../components/OrderForm';
import { OrderDetails } from '../components/OrderDetails';
import type { Order } from '../types';
import './OrdersPage.css';

export const OrdersPage: React.FC = () => {
  const { user, cart } = useAppContext();
  const [orders, setOrders] = useState<Order[]>([]);
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [viewMode, setViewMode] = useState<'create' | 'list' | 'details'>('create');

  useEffect(() => {
    if (user && viewMode === 'list') {
      loadOrders();
    }
  }, [user, viewMode]);

  const loadOrders = async () => {
    if (!user) return;

    setLoading(true);
    setError(null);
    try {
      const userOrders = await orderService.getUserOrders(user.userId);
      setOrders(userOrders);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load orders');
    } finally {
      setLoading(false);
    }
  };

  const handleOrderCreated = (order: Order) => {
    setSelectedOrder(order);
    setViewMode('details');
    setOrders([order, ...orders]);
  };

  const handleViewOrder = async (orderId: string) => {
    setLoading(true);
    setError(null);
    try {
      const order = await orderService.getOrder(orderId);
      setSelectedOrder(order);
      setViewMode('details');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load order');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container">
      <div className="orders-page-header">
        <h1>Orders</h1>
        <div className="view-toggle">
          <button
            onClick={() => setViewMode('create')}
            className={`btn ${viewMode === 'create' ? 'btn-primary' : 'btn-secondary'}`}
          >
            {cart.length > 0 ? `Create Order (${cart.length})` : 'Create Order'}
          </button>
          <button
            onClick={() => {
              setViewMode('list');
              loadOrders();
            }}
            className={`btn ${viewMode === 'list' ? 'btn-primary' : 'btn-secondary'}`}
            disabled={!user}
          >
            My Orders
          </button>
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}

      {viewMode === 'create' && (
        <OrderForm onOrderCreated={handleOrderCreated} />
      )}

      {viewMode === 'list' && (
        <div className="orders-list">
          {loading ? (
            <div className="loading">Loading orders...</div>
          ) : orders.length === 0 ? (
            <div className="no-orders">No orders found. Create your first order!</div>
          ) : (
            <div className="orders-grid">
              {orders.map((order) => (
                <div key={order.id} className="order-card" onClick={() => handleViewOrder(order.id)}>
                  <h3>Order #{order.id.slice(0, 8)}</h3>
                  <p><strong>Status:</strong> {order.status}</p>
                  <p><strong>Total:</strong> ${order.totalAmount.toFixed(2)}</p>
                  <p><strong>Items:</strong> {order.items.length}</p>
                  {order.createdAt && (
                    <p><strong>Date:</strong> {new Date(order.createdAt).toLocaleDateString()}</p>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {viewMode === 'details' && selectedOrder && (
        <div>
          <button
            onClick={() => setViewMode('list')}
            className="btn btn-secondary"
          >
            ← Back to Orders
          </button>
          <OrderDetails order={selectedOrder} />
        </div>
      )}
    </div>
  );
};

