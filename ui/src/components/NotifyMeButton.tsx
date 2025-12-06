import React, { useState } from 'react';
import { stockService } from '../services/stockService';
import './NotifyMeButton.css';

interface NotifyMeButtonProps {
  productId: string;
  productName: string;
}

export const NotifyMeButton: React.FC<NotifyMeButtonProps> = ({ productId, productName }) => {
  const [showModal, setShowModal] = useState(false);
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleOpenModal = () => {
    setShowModal(true);
    setEmail('');
    setSuccess(false);
    setError(null);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEmail('');
    setSuccess(false);
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      await stockService.signUpForNotification(productId, email);
      setSuccess(true);
      setTimeout(() => {
        handleCloseModal();
      }, 2000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to sign up for notifications');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <button
        onClick={handleOpenModal}
        className="btn btn-secondary notify-me-button"
        type="button"
      >
        Notify Me When Available
      </button>

      {showModal && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Notify Me When Available</h3>
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
              <p>We'll notify you when <strong>{productName}</strong> is back in stock.</p>
              {success ? (
                <div className="success-message">
                  ✓ Successfully signed up! You'll receive an email when this product is available.
                </div>
              ) : (
                <form onSubmit={handleSubmit}>
                  <div className="form-group">
                    <label htmlFor="notify-email">Email Address</label>
                    <input
                      id="notify-email"
                      type="email"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                      placeholder="your@email.com"
                      disabled={loading}
                    />
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
                      {loading ? 'Signing up...' : 'Notify Me'}
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

