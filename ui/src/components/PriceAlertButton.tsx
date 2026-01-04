import React, { useState } from 'react';
import { useAppContext } from '../context/AppContext';
import { priceAlertService } from '../services/priceAlertService';
import type { Product } from '../types';
import './PriceAlertButton.css';

interface PriceAlertButtonProps {
  product: Product;
}

export const PriceAlertButton: React.FC<PriceAlertButtonProps> = ({ product }) => {
  const { user } = useAppContext();
  const [showModal, setShowModal] = useState(false);
  const [email, setEmail] = useState(user?.email || '');
  const [targetPrice, setTargetPrice] = useState('');
  const [notificationFrequency, setNotificationFrequency] = useState('IMMEDIATE');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleOpenModal = () => {
    setShowModal(true);
    setEmail(user?.email || '');
    setTargetPrice('');
    setNotificationFrequency('IMMEDIATE');
    setSuccess(false);
    setError(null);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEmail(user?.email || '');
    setTargetPrice('');
    setNotificationFrequency('IMMEDIATE');
    setSuccess(false);
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      await priceAlertService.createAlert({
        productId: product.id,
        email: email,
        targetPrice: targetPrice ? parseFloat(targetPrice) : undefined,
        notificationFrequency: notificationFrequency,
        userId: user?.userId,
      });
      setSuccess(true);
      setTimeout(() => {
        handleCloseModal();
      }, 2000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create price alert');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <button
        onClick={handleOpenModal}
        className="btn btn-secondary price-alert-button"
        type="button"
      >
        Notify Me When Price Drops
      </button>

      {showModal && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Price Drop Alert</h3>
              <button
                className="modal-close"
                onClick={handleCloseModal}
                aria-label="Close"
                type="button"
              >
                ×
              </button>
            </div>
            <div className="modal-body">
              <p>We'll notify you when <strong>{product.name}</strong> drops in price.</p>
              <p className="current-price">Current Price: ${product.price.toFixed(2)}</p>
              {success ? (
                <div className="success-message">
                  ✓ Price alert created! You'll receive an email when the price drops.
                </div>
              ) : (
                <form onSubmit={handleSubmit}>
                  <div className="form-group">
                    <label htmlFor="price-alert-email">Email Address</label>
                    <input
                      id="price-alert-email"
                      type="email"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                      placeholder="your@email.com"
                      disabled={loading}
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="price-alert-target">Target Price (Optional)</label>
                    <input
                      id="price-alert-target"
                      type="number"
                      step="0.01"
                      min="0.01"
                      value={targetPrice}
                      onChange={(e) => setTargetPrice(e.target.value)}
                      placeholder={`${product.price.toFixed(2)}`}
                      disabled={loading}
                    />
                    <small>Leave empty to be notified of any price drop (5% or more)</small>
                  </div>
                  <div className="form-group">
                    <label htmlFor="price-alert-frequency">Notification Frequency</label>
                    <select
                      id="price-alert-frequency"
                      value={notificationFrequency}
                      onChange={(e) => setNotificationFrequency(e.target.value)}
                      disabled={loading}
                    >
                      <option value="IMMEDIATE">Immediate</option>
                      <option value="DAILY_DIGEST">Daily Digest</option>
                      <option value="WEEKLY_DIGEST">Weekly Digest</option>
                    </select>
                  </div>
                  {error && <div className="error-message">{error}</div>}
                  <div className="modal-actions">
                    <button
                      type="button"
                      className="btn btn-secondary"
                      onClick={handleCloseModal}
                      disabled={loading}
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      className="btn btn-primary"
                      disabled={loading || !email}
                    >
                      {loading ? 'Creating...' : 'Create Alert'}
                    </button>
                  </div>
                </form>
              )}
            </div>
          </div>
        </div>
      )}
    </>
  );
};

