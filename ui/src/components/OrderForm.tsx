import React, { useState } from 'react';
import { useAppContext } from '../context/AppContext';
import { orderService } from '../services/orderService';
import { ShippingCostCalculator } from './ShippingCostCalculator';
import { ShippingRecommendations } from './ShippingRecommendations';
import type { Order } from '../types';
import './OrderForm.css';

interface OrderFormProps {
  onOrderCreated?: (order: Order) => void;
}

export const OrderForm: React.FC<OrderFormProps> = ({ onOrderCreated }) => {
  const { 
    user, 
    cart, 
    clearCart, 
    updateCartQuantity, 
    removeFromCart,
    addToCart,
    products,
    shippingRegion, 
    freeShippingThreshold,
    shippingCost,
    defaultShippingCost,
    recommendations,
    loadingRecommendations
  } = useAppContext();
  
  const qualifiesForFreeShipping = shippingCost !== null && shippingCost === 0;
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [createdOrder, setCreatedOrder] = useState<Order | null>(null);

  const subtotal = cart.reduce(
    (sum, item) => sum + item.price * item.orderQuantity,
    0
  );
  
  const currentShippingCost = shippingCost !== null ? shippingCost : (defaultShippingCost || 0);
  const totalAmount = subtotal + currentShippingCost;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!user) {
      setError('Please create a user account first');
      return;
    }

    if (cart.length === 0) {
      setError('Your cart is empty. Add products to create an order.');
      return;
    }

    setError(null);
    setLoading(true);

    try {
      const orderData = {
        userId: user.userId,
        items: cart.map((item) => ({
          productId: item.id,
          quantity: item.orderQuantity,
        })),
      };

      const order = await orderService.createOrder(orderData);
      setCreatedOrder(order);
      clearCart();
      
      if (onOrderCreated) {
        onOrderCreated(order);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create order');
    } finally {
      setLoading(false);
    }
  };

  if (createdOrder) {
    return (
      <div className="order-success">
        <h2>Order Created Successfully!</h2>
        <p>Order ID: {createdOrder.id}</p>
        <p>Total Amount: ${createdOrder.totalAmount.toFixed(2)}</p>
        <p>Status: {createdOrder.status}</p>
        <button
          onClick={() => {
            setCreatedOrder(null);
            if (onOrderCreated) {
              onOrderCreated(createdOrder);
            }
          }}
          className="btn btn-primary"
        >
          View Order Details
        </button>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="order-form-container">
        <div className="info-message">
          Please create a user account first before placing an order.
        </div>
      </div>
    );
  }

  if (cart.length === 0) {
    return (
      <div className="order-form-container">
        <div className="info-message">
          Your cart is empty. Browse products and add them to your cart.
        </div>
      </div>
    );
  }

  return (
    <div className="order-form-container">
      <h2>Review Your Order</h2>
      <div className="cart-items">
        {cart.map((item) => (
          <div key={item.id} className="cart-item">
            <div className="cart-item-info">
              <h4>{item.name}</h4>
              <p className="cart-item-price">${item.price.toFixed(2)} each</p>
            </div>
            <div className="cart-item-controls">
              <div className="quantity-control">
                <button
                  onClick={() => updateCartQuantity(item.id, item.orderQuantity - 1)}
                  className="btn btn-sm"
                >
                  -
                </button>
                <span>{item.orderQuantity}</span>
                <button
                  onClick={() => updateCartQuantity(item.id, item.orderQuantity + 1)}
                  className="btn btn-sm"
                  disabled={item.orderQuantity >= item.quantity}
                >
                  +
                </button>
              </div>
              <p className="cart-item-subtotal">
                ${(item.price * item.orderQuantity).toFixed(2)}
              </p>
              <button
                onClick={() => removeFromCart(item.id)}
                className="btn btn-sm btn-danger"
              >
                Remove
              </button>
            </div>
          </div>
        ))}
      </div>
      {shippingRegion && freeShippingThreshold !== null && shippingCost !== null && defaultShippingCost !== null && (
        <>
          {/* Show recommendations only when cart doesn't qualify for free shipping */}
          {!qualifiesForFreeShipping && (
            <ShippingRecommendations
              recommendations={recommendations}
              loading={loadingRecommendations}
              onAddToCart={(productId: string) => {
                // Find product and add to cart
                const product = products.find((p) => p.id === productId);
                if (product) {
                  addToCart(product, 1);
                }
              }}
            />
          )}
          <ShippingCostCalculator
            cartTotal={subtotal}
            region={shippingRegion}
            shippingCost={shippingCost}
            freeShippingThreshold={freeShippingThreshold}
            remainingAmount={Math.max(0, freeShippingThreshold - subtotal)}
            qualifiesForFreeShipping={subtotal >= freeShippingThreshold}
          />
        </>
      )}
      <div className="order-summary">
        <div className="summary-row">
          <span>Subtotal:</span>
          <span>${subtotal.toFixed(2)}</span>
        </div>
        <div className="summary-row">
          <span>Shipping:</span>
          <span>
            {shippingCost !== null && shippingCost === 0 ? (
              <span className="shipping-free-text">FREE</span>
            ) : (
              `$${currentShippingCost.toFixed(2)}`
            )}
          </span>
        </div>
        <div className="summary-row">
          <span>Total:</span>
          <span className="total-amount">${totalAmount.toFixed(2)}</span>
        </div>
      </div>
      <form onSubmit={handleSubmit}>
        {error && <div className="error-message">{error}</div>}
        <button type="submit" disabled={loading} className="btn btn-primary btn-large">
          {loading ? 'Creating Order...' : 'Place Order'}
        </button>
      </form>
    </div>
  );
};

