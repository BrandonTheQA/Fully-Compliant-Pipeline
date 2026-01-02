import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { useAppContext } from '../context/AppContext';
import { orderService } from '../services/orderService';
import { returnService } from '../services/returnService';
import type { Order, CreateReturnRequest, Return, ReturnReason, ReturnType } from '../types';
import './ReturnRequestPage.css';

export const ReturnRequestPage: React.FC = () => {
  const { user } = useAppContext();
  const location = useLocation();
  const [orders, setOrders] = useState<Order[]>([]);
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [selectedItems, setSelectedItems] = useState<Set<number>>(new Set());
  const [returnReasons, setReturnReasons] = useState<Map<number, ReturnReason>>(new Map());
  const [returnType, setReturnType] = useState<ReturnType>('REFUND_TO_PAYMENT');
  const [comments, setComments] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [createdReturn, setCreatedReturn] = useState<Return | null>(null);

  useEffect(() => {
    if (user) {
      loadOrders();
      
      // Check if order ID was passed via navigation state
      const state = location.state as { orderId?: string } | null;
      if (state?.orderId) {
        orderService.getOrder(state.orderId)
          .then(order => setSelectedOrder(order))
          .catch(err => console.error('Failed to load order:', err));
      }
    }
  }, [user, location]);

  const loadOrders = async () => {
    if (!user) return;

    setLoading(true);
    setError(null);
    try {
      const userOrders = await orderService.getUserOrders(user.userId);
      // Filter orders that are delivered or completed
      const eligibleOrders = userOrders.filter(
        order => order.status === 'DELIVERED' || order.status === 'CONFIRMED'
      );
      setOrders(eligibleOrders);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load orders');
    } finally {
      setLoading(false);
    }
  };

  const handleOrderSelect = (order: Order) => {
    setSelectedOrder(order);
    setSelectedItems(new Set());
    setReturnReasons(new Map());
  };

  const handleItemToggle = (orderItemIndex: number) => {
    const newSelected = new Set(selectedItems);
    if (newSelected.has(orderItemIndex)) {
      newSelected.delete(orderItemIndex);
      const newReasons = new Map(returnReasons);
      newReasons.delete(orderItemIndex);
      setReturnReasons(newReasons);
    } else {
      newSelected.add(orderItemIndex);
    }
    setSelectedItems(newSelected);
  };

  const handleReasonChange = (orderItemIndex: number, reason: ReturnReason) => {
    const newReasons = new Map(returnReasons);
    newReasons.set(orderItemIndex, reason);
    setReturnReasons(newReasons);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!user || !selectedOrder) {
      setError('Please select an order');
      return;
    }

    if (selectedItems.size === 0) {
      setError('Please select at least one item to return');
      return;
    }

    // Validate all selected items have reasons
    for (const itemIndex of selectedItems) {
      if (!returnReasons.has(itemIndex)) {
        setError('Please select a return reason for all items');
        return;
      }
    }

    setLoading(true);
    setError(null);

    try {
      const returnRequest: CreateReturnRequest = {
        orderId: selectedOrder.id,
        userId: user.userId,
        items: Array.from(selectedItems).map(itemIndex => {
          const orderItem = selectedOrder.items[itemIndex];
          // Note: orderItemId should be the database ID of the order item
          // If not available, we'll need to fetch the full order with item IDs
          // For now, using a workaround - the backend will need to handle this
          const orderItemId = orderItem.id || (itemIndex + 1); // Fallback to index+1 if ID not available
          return {
            orderItemId: orderItemId,
            quantity: orderItem.quantity,
            returnReason: returnReasons.get(itemIndex)!,
            comments: comments,
          };
        }),
        returnType: returnType,
        comments: comments,
      };

      const created = await returnService.createReturn(returnRequest);
      setCreatedReturn(created);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create return request');
    } finally {
      setLoading(false);
    }
  };

  if (createdReturn) {
    return (
      <div className="page-container">
        <div className="return-success">
          <h1>Return Request Submitted</h1>
          <p>Your return request has been submitted successfully!</p>
          <div className="rma-number">
            <strong>RMA Number:</strong> {createdReturn.rmaNumber}
          </div>
          <p>You can track your return status using this RMA number.</p>
          <button
            onClick={() => {
              setCreatedReturn(null);
              setSelectedOrder(null);
              setSelectedItems(new Set());
            }}
            className="btn btn-primary"
          >
            Create Another Return
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <h1>Request a Return</h1>

      {error && <div className="error-message">{error}</div>}

      {!user && (
        <div className="error-message">Please create a user account first</div>
      )}

      {user && (
        <>
          <div className="return-form-section">
            <h2>Select Order</h2>
            {loading && orders.length === 0 ? (
              <div className="loading">Loading orders...</div>
            ) : orders.length === 0 ? (
              <div className="no-orders">No eligible orders found for return</div>
            ) : (
              <div className="orders-list">
                {orders.map((order) => (
                  <button
                    key={order.id}
                    className={`order-card ${selectedOrder?.id === order.id ? 'selected' : ''}`}
                    onClick={() => handleOrderSelect(order)}
                  >
                    <h3>Order #{order.id.slice(0, 8)}</h3>
                    <p><strong>Date:</strong> {new Date(order.createdAt || '').toLocaleDateString()}</p>
                    <p><strong>Total:</strong> ${order.totalAmount.toFixed(2)}</p>
                    <p><strong>Status:</strong> {order.status}</p>
                  </button>
                ))}
              </div>
            )}
          </div>

          {selectedOrder && (
            <form onSubmit={handleSubmit} className="return-form">
              <div className="return-form-section">
                <h2>Select Items to Return</h2>
                <div className="return-items">
                  {selectedOrder.items.map((item, index) => (
                    <div key={index} className="return-item">
                      <label>
                        <input
                          type="checkbox"
                          checked={selectedItems.has(index)}
                          onChange={() => handleItemToggle(index)}
                        />
                        <span>
                          {item.productName || `Product ${item.productId}`} - 
                          Qty: {item.quantity} - 
                          ${(item.price || 0).toFixed(2)}
                        </span>
                      </label>
                      {selectedItems.has(index) && (
                        <div className="return-item-details">
                          <label>
                            Return Reason:
                            <select
                              value={returnReasons.get(index) || ''}
                              onChange={(e) => handleReasonChange(index, e.target.value as ReturnReason)}
                              required
                            >
                              <option value="">Select reason...</option>
                              <option value="DEFECTIVE">Defective</option>
                              <option value="WRONG_ITEM">Wrong Item</option>
                              <option value="NOT_AS_DESCRIBED">Not as Described</option>
                              <option value="CHANGED_MIND">Changed Mind</option>
                              <option value="SIZE_COLOR_ISSUE">Size/Color Issue</option>
                              <option value="OTHER">Other</option>
                            </select>
                          </label>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </div>

              <div className="return-form-section">
                <h2>Return Type</h2>
                <label>
                  <input
                    type="radio"
                    value="REFUND_TO_PAYMENT"
                    checked={returnType === 'REFUND_TO_PAYMENT'}
                    onChange={(e) => setReturnType(e.target.value as ReturnType)}
                  />
                  Refund to Original Payment Method
                </label>
                <label>
                  <input
                    type="radio"
                    value="STORE_CREDIT"
                    checked={returnType === 'STORE_CREDIT'}
                    onChange={(e) => setReturnType(e.target.value as ReturnType)}
                  />
                  Store Credit
                </label>
                <label>
                  <input
                    type="radio"
                    value="EXCHANGE"
                    checked={returnType === 'EXCHANGE'}
                    onChange={(e) => setReturnType(e.target.value as ReturnType)}
                  />
                  Exchange
                </label>
              </div>

              <div className="return-form-section">
                <label>
                  Additional Comments (Optional):
                  <textarea
                    value={comments}
                    onChange={(e) => setComments(e.target.value)}
                    rows={4}
                  />
                </label>
              </div>

              <button
                type="submit"
                className="btn btn-primary"
                disabled={loading || selectedItems.size === 0}
              >
                {loading ? 'Submitting...' : 'Submit Return Request'}
              </button>
            </form>
          )}
        </>
      )}
    </div>
  );
};

