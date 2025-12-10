import React, { useState } from 'react';
import { useAppContext } from '../context/AppContext';
import { giftCardService } from '../services/giftCardService';
import type { PurchaseGiftCardRequest, PurchaseGiftCardResponse } from '../services/giftCardService';
import './GiftCardPurchase.css';

const FIXED_AMOUNTS = [25, 50, 100, 150, 200, 250, 500];

export const GiftCardPurchase: React.FC = () => {
  const { user } = useAppContext();
  const [selectedAmount, setSelectedAmount] = useState<number | null>(null);
  const [customAmount, setCustomAmount] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [recipientEmail, setRecipientEmail] = useState('');
  const [recipientName, setRecipientName] = useState('');
  const [personalMessage, setPersonalMessage] = useState('');
  const [design, setDesign] = useState('general');
  const [scheduledDeliveryDate, setScheduledDeliveryDate] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<PurchaseGiftCardResponse | null>(null);

  const handleAmountSelect = (amount: number) => {
    setSelectedAmount(amount);
    setCustomAmount('');
  };

  const handleCustomAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    if (value === '' || /^\d*\.?\d{0,2}$/.test(value)) {
      setCustomAmount(value);
      setSelectedAmount(null);
    }
  };

  const getAmount = (): number | null => {
    if (selectedAmount !== null) {
      return selectedAmount;
    }
    if (customAmount) {
      const amount = parseFloat(customAmount);
      return isNaN(amount) || amount < 10 || amount > 1000 ? null : amount;
    }
    return null;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    const amount = getAmount();
    if (!amount) {
      setError('Please select or enter a valid amount ($10-$1,000)');
      return;
    }

    if (quantity < 1 || quantity > 10) {
      setError('Quantity must be between 1 and 10');
      return;
    }

    setLoading(true);

    try {
      const request: PurchaseGiftCardRequest = {
        amount,
        quantity,
        purchaserId: user?.userId,
        purchaserEmail: user?.email || '',
        recipientEmail: recipientEmail || undefined,
        recipientName: recipientName || undefined,
        personalMessage: personalMessage || undefined,
        design: design || undefined,
        scheduledDeliveryDate: scheduledDeliveryDate || undefined,
      };

      const response = await giftCardService.purchaseGiftCard(request);
      setSuccess(response);
      
      // Reset form
      setSelectedAmount(null);
      setCustomAmount('');
      setQuantity(1);
      setRecipientEmail('');
      setRecipientName('');
      setPersonalMessage('');
      setDesign('general');
      setScheduledDeliveryDate('');
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to purchase gift card');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  };

  const totalAmount = getAmount() ? (getAmount()! * quantity) : 0;

  if (success) {
    return (
      <div className="gift-card-purchase">
        <div className="success-message">
          <h2>Gift Card(s) Purchased Successfully!</h2>
          <p>Total Amount: {formatCurrency(success.totalAmount)}</p>
          <div className="gift-cards-list">
            {success.giftCards.map((card, index) => (
              <div key={card.giftCardId} className="gift-card-preview">
                <h3>Gift Card {index + 1}</h3>
                <div className="gift-card-code">{card.code}</div>
                <div className="gift-card-amount">{formatCurrency(card.amount)}</div>
                {card.recipientEmail && (
                  <p>Will be sent to: {card.recipientEmail}</p>
                )}
              </div>
            ))}
          </div>
          <button onClick={() => setSuccess(null)} className="purchase-another-btn">
            Purchase Another Gift Card
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="gift-card-purchase">
      <h2>Purchase Gift Card</h2>
      
      <form onSubmit={handleSubmit} className="purchase-form">
        <div className="form-section">
          <h3>Select Amount</h3>
          <div className="fixed-amounts">
            {FIXED_AMOUNTS.map((amount) => (
              <button
                key={amount}
                type="button"
                className={`amount-btn ${selectedAmount === amount ? 'selected' : ''}`}
                onClick={() => handleAmountSelect(amount)}
              >
                {formatCurrency(amount)}
              </button>
            ))}
          </div>
          
          <div className="custom-amount">
            <label htmlFor="custom-amount">Or enter custom amount ($10-$1,000)</label>
            <input
              id="custom-amount"
              type="text"
              value={customAmount}
              onChange={handleCustomAmountChange}
              placeholder="Enter amount"
              className="custom-amount-input"
            />
          </div>
        </div>

        <div className="form-section">
          <h3>Quantity</h3>
          <div className="quantity-selector">
            <button
              type="button"
              onClick={() => setQuantity(Math.max(1, quantity - 1))}
              className="quantity-btn"
            >
              -
            </button>
            <input
              type="number"
              value={quantity}
              onChange={(e) => {
                const val = parseInt(e.target.value) || 1;
                setQuantity(Math.min(10, Math.max(1, val)));
              }}
              min="1"
              max="10"
              className="quantity-input"
            />
            <button
              type="button"
              onClick={() => setQuantity(Math.min(10, quantity + 1))}
              className="quantity-btn"
            >
              +
            </button>
          </div>
        </div>

        <div className="form-section">
          <h3>Recipient Information (Optional)</h3>
          <div className="form-group">
            <label htmlFor="recipient-name">Recipient Name</label>
            <input
              id="recipient-name"
              type="text"
              value={recipientName}
              onChange={(e) => setRecipientName(e.target.value)}
              placeholder="Recipient's name"
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="recipient-email">Recipient Email</label>
            <input
              id="recipient-email"
              type="email"
              value={recipientEmail}
              onChange={(e) => setRecipientEmail(e.target.value)}
              placeholder="recipient@example.com"
            />
            <small>If provided, gift card will be emailed to recipient</small>
          </div>
        </div>

        <div className="form-section">
          <h3>Personal Message (Optional)</h3>
          <textarea
            value={personalMessage}
            onChange={(e) => setPersonalMessage(e.target.value)}
            placeholder="Add a personal message (max 500 characters)"
            maxLength={500}
            rows={4}
            className="message-textarea"
          />
          <small>{personalMessage.length}/500 characters</small>
        </div>

        <div className="form-section">
          <h3>Design (Optional)</h3>
          <select
            value={design}
            onChange={(e) => setDesign(e.target.value)}
            className="design-select"
          >
            <option value="general">General</option>
            <option value="birthday">Birthday</option>
            <option value="holiday">Holiday</option>
            <option value="thank-you">Thank You</option>
          </select>
        </div>

        <div className="form-section">
          <h3>Schedule Delivery (Optional)</h3>
          <input
            type="datetime-local"
            value={scheduledDeliveryDate}
            onChange={(e) => setScheduledDeliveryDate(e.target.value)}
            className="delivery-date-input"
          />
          <small>Schedule gift card delivery for a future date</small>
        </div>

        <div className="form-section total-section">
          <div className="total-display">
            <span className="total-label">Total:</span>
            <span className="total-amount">{formatCurrency(totalAmount)}</span>
          </div>
        </div>

        {error && (
          <div className="error-message" role="alert">
            {error}
          </div>
        )}

        <button type="submit" disabled={loading || !getAmount()} className="purchase-btn">
          {loading ? 'Processing...' : `Purchase Gift Card${quantity > 1 ? 's' : ''}`}
        </button>
      </form>
    </div>
  );
};
