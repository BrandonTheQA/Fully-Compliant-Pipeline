import React, { useState, useEffect } from 'react';
import { useAppContext } from '../context/AppContext';
import { orderService } from '../services/orderService';
import { ShippingCostCalculator } from './ShippingCostCalculator';
import { ShippingRecommendations } from './ShippingRecommendations';
import { LoyaltyBalance } from './LoyaltyBalance';
import { PointRedemptionForm } from './PointRedemptionForm';
import { CartStockStatus } from './CartStockStatus';
import { loyaltyService } from '../services/loyaltyService';
import { giftCardService } from '../services/giftCardService';
import type { ApplyGiftCardResponse } from '../services/giftCardService';
import type { Order, RedeemPointsResponse } from '../types';
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
  const [pointsToRedeem, setPointsToRedeem] = useState<number>(0);
  const [pointsDiscount, setPointsDiscount] = useState<number>(0);
  const [loyaltyBalance, setLoyaltyBalance] = useState<number>(0);
  const [giftCardCode, setGiftCardCode] = useState('');
  const [appliedGiftCards, setAppliedGiftCards] = useState<Array<{code: string; appliedAmount: number; remainingBalance: number}>>([]);
  const [giftCardDiscount, setGiftCardDiscount] = useState<number>(0);
  const [giftCardError, setGiftCardError] = useState<string | null>(null);

  const subtotal = cart.reduce(
    (sum, item) => sum + item.price * item.orderQuantity,
    0
  );
  
  const currentShippingCost = shippingCost !== null ? shippingCost : (defaultShippingCost || 0);
  const totalAmount = Math.max(0, subtotal + currentShippingCost - pointsDiscount - giftCardDiscount);

  // Load loyalty balance when user is available
  useEffect(() => {
    if (user) {
      loyaltyService.getBalance(user.userId)
        .then(account => setLoyaltyBalance(account.currentPoints))
        .catch(() => {
          // Silently fail if loyalty service is not available
        });
    }
  }, [user]);

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

    // Check for out-of-stock items
    const outOfStockItems = cart.filter(item => 
      item.stockStatus === 'OUT_OF_STOCK' || item.quantity === 0
    );
    if (outOfStockItems.length > 0) {
      setError('Some items in your cart are out of stock. Please remove them before placing your order.');
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
        pointsToRedeem: pointsToRedeem > 0 ? pointsToRedeem : undefined,
        giftCardCodes: appliedGiftCards.length > 0 ? appliedGiftCards.map(gc => gc.code) : undefined,
      };

      const order = await orderService.createOrder(orderData);
      setCreatedOrder(order);
      clearCart();
      setPointsToRedeem(0);
      setPointsDiscount(0);
      setAppliedGiftCards([]);
      setGiftCardDiscount(0);
      setGiftCardCode('');
      
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
      <fieldset className="cart-items-fieldset">
        <legend>Cart Items</legend>
        <div className="cart-items">
          {cart.map((item) => (
          <div key={item.id} className="cart-item">
            <div className="cart-item-info">
              <h3>{item.name}</h3>
              <p className="cart-item-price">${item.price.toFixed(2)} each</p>
            </div>
            <CartStockStatus 
              item={item} 
              onRemove={() => removeFromCart(item.id)}
            />
            <div className="cart-item-controls">
              <div className="quantity-control">
                <button
                  onClick={() => updateCartQuantity(item.id, item.orderQuantity - 1)}
                  className="btn btn-sm"
                  aria-label={`Decrease quantity of ${item.name}`}
                >
                  -
                </button>
                <span aria-label={`Current quantity: ${item.orderQuantity}`}>{item.orderQuantity}</span>
                <button
                  onClick={() => updateCartQuantity(item.id, item.orderQuantity + 1)}
                  className="btn btn-sm"
                  disabled={item.orderQuantity >= item.quantity || item.stockStatus === 'OUT_OF_STOCK'}
                  aria-disabled={item.orderQuantity >= item.quantity || item.stockStatus === 'OUT_OF_STOCK'}
                  aria-label={`Increase quantity of ${item.name}`}
                  aria-describedby={item.orderQuantity >= item.quantity ? `max-quantity-${item.id}` : undefined}
                >
                  +
                </button>
                {item.orderQuantity >= item.quantity && (
                  <span id={`max-quantity-${item.id}`} className="sr-only">
                    Maximum quantity reached for {item.name}
                  </span>
                )}
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
      </fieldset>
      {shippingRegion && freeShippingThreshold !== null && shippingCost !== null && defaultShippingCost !== null && (
        <fieldset className="shipping-fieldset">
          <legend>Shipping Information</legend>
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
        </fieldset>
      )}
      {user && loyaltyBalance > 0 && (
        <fieldset className="loyalty-fieldset">
          <legend>Loyalty Points</legend>
          <div className="loyalty-section">
            <LoyaltyBalance userId={user.userId} onBalanceChange={setLoyaltyBalance} />
            <PointRedemptionForm
              userId={user.userId}
              currentBalance={loyaltyBalance}
              orderTotal={subtotal + currentShippingCost - giftCardDiscount}
              onRedemptionSuccess={(response: RedeemPointsResponse) => {
                setPointsToRedeem(response.pointsRedeemed);
                setPointsDiscount(response.discountAmount);
                setLoyaltyBalance(response.remainingBalance);
              }}
              onError={(errorMsg: string) => {
                setError(errorMsg);
              }}
            />
          </div>
        </fieldset>
      )}
      <fieldset className="gift-card-fieldset">
        <legend>Gift Cards</legend>
        <div className="gift-card-section">
          <div className="gift-card-input-group">
            <input
              type="text"
              value={giftCardCode}
              onChange={(e) => setGiftCardCode(e.target.value.toUpperCase())}
              placeholder="Enter gift card code"
              className="gift-card-code-input"
              maxLength={19}
            />
            <button
              type="button"
              onClick={async () => {
                if (!giftCardCode.trim()) {
                  setGiftCardError('Please enter a gift card code');
                  return;
                }
                
                if (appliedGiftCards.some(gc => gc.code === giftCardCode.trim())) {
                  setGiftCardError('This gift card is already applied');
                  return;
                }
                
                setGiftCardError(null);
                try {
                  const orderTotal = subtotal + currentShippingCost - pointsDiscount - giftCardDiscount;
                  const response: ApplyGiftCardResponse = await giftCardService.applyGiftCard({
                    code: giftCardCode.trim(),
                    orderTotal,
                  });
                  
                  setAppliedGiftCards([...appliedGiftCards, {
                    code: giftCardCode.trim(),
                    appliedAmount: response.appliedAmount,
                    remainingBalance: response.remainingBalance,
                  }]);
                  setGiftCardDiscount(giftCardDiscount + response.appliedAmount);
                  setGiftCardCode('');
                } catch (err: any) {
                  setGiftCardError(err.response?.data?.message || err.message || 'Failed to apply gift card');
                }
              }}
              className="btn btn-sm"
              disabled={!giftCardCode.trim() || loading}
            >
              Apply
            </button>
          </div>
          {giftCardError && (
            <div className="gift-card-error" role="alert">
              {giftCardError}
            </div>
          )}
          {appliedGiftCards.length > 0 && (
            <div className="applied-gift-cards">
              <h4>Applied Gift Cards:</h4>
              {appliedGiftCards.map((gc, index) => (
                <div key={index} className="applied-gift-card-item">
                  <span className="gift-card-code-display">{gc.code}</span>
                  <span className="gift-card-applied-amount">-${gc.appliedAmount.toFixed(2)}</span>
                  <span className="gift-card-remaining">Remaining: ${gc.remainingBalance.toFixed(2)}</span>
                  <button
                    type="button"
                    onClick={() => {
                      const updatedCards = appliedGiftCards.filter((_, i) => i !== index);
                      setAppliedGiftCards(updatedCards);
                      setGiftCardDiscount(giftCardDiscount - gc.appliedAmount);
                    }}
                    className="btn btn-sm btn-danger"
                  >
                    Remove
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </fieldset>
      <fieldset className="order-summary-fieldset">
        <legend>Order Summary</legend>
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
          {pointsDiscount > 0 && (
            <div className="summary-row summary-row-discount">
              <span>Points Discount:</span>
              <span className="discount-amount">-${pointsDiscount.toFixed(2)}</span>
            </div>
          )}
          {giftCardDiscount > 0 && (
            <div className="summary-row summary-row-discount">
              <span>Gift Card Discount:</span>
              <span className="discount-amount">-${giftCardDiscount.toFixed(2)}</span>
            </div>
          )}
          <div className="summary-row">
            <span>Total:</span>
            <span className="total-amount">${totalAmount.toFixed(2)}</span>
          </div>
        </div>
      </fieldset>
      <form onSubmit={handleSubmit}>
        {error && (
          <div id="order-error" className="error-message" role="alert">
            {error}
          </div>
        )}
        <button 
          type="submit" 
          disabled={loading} 
          className="btn btn-primary btn-large"
          aria-describedby={error ? 'order-error' : undefined}
        >
          {loading ? 'Creating Order...' : 'Place Order'}
        </button>
      </form>
    </div>
  );
};

