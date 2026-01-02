import React, { useState } from 'react';
import { returnService } from '../services/returnService';
import type { ReturnTracking } from '../types';
import './ReturnTrackingPage.css';

export const ReturnTrackingPage: React.FC = () => {
  const [rmaNumber, setRmaNumber] = useState('');
  const [tracking, setTracking] = useState<ReturnTracking | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleLookup = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!rmaNumber.trim()) {
      setError('Please enter an RMA number');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const returnData = await returnService.getReturnByRMA(rmaNumber);
      const trackingData = await returnService.getReturnTracking(returnData.returnId);
      setTracking(trackingData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Return not found');
      setTracking(null);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'APPROVED':
        return '#28a745';
      case 'REJECTED':
        return '#dc3545';
      case 'REFUNDED':
      case 'COMPLETED':
        return '#17a2b8';
      case 'PENDING_APPROVAL':
        return '#ffc107';
      default:
        return '#6c757d';
    }
  };

  return (
    <div className="page-container">
      <h1>Track Your Return</h1>

      <form onSubmit={handleLookup} className="rma-lookup-form">
        <label>
          Enter RMA Number:
          <input
            type="text"
            value={rmaNumber}
            onChange={(e) => setRmaNumber(e.target.value.toUpperCase())}
            placeholder="RMA-YYYYMMDD-XXXXX"
            className="rma-input"
          />
        </label>
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Looking up...' : 'Track Return'}
        </button>
      </form>

      {error && <div className="error-message">{error}</div>}

      {tracking && (
        <div className="return-tracking">
          <div className="tracking-header">
            <h2>Return Status: {tracking.status}</h2>
            <div
              className="status-badge"
              style={{ backgroundColor: getStatusColor(tracking.status) }}
            >
              {tracking.status.replace('_', ' ')}
            </div>
          </div>

          <div className="tracking-info">
            <p><strong>RMA Number:</strong> {tracking.rmaNumber}</p>
            <p><strong>Return Type:</strong> {tracking.returnType.replace('_', ' ')}</p>
            {tracking.returnTrackingNumber && (
              <p><strong>Tracking Number:</strong> {tracking.returnTrackingNumber}</p>
            )}
            {tracking.returnCarrier && (
              <p><strong>Carrier:</strong> {tracking.returnCarrier}</p>
            )}
            {tracking.refundAmount && (
              <p><strong>Refund Amount:</strong> ${tracking.refundAmount.toFixed(2)}</p>
            )}
            {tracking.refundDate && (
              <p><strong>Refund Date:</strong> {new Date(tracking.refundDate).toLocaleDateString()}</p>
            )}
            {tracking.estimatedRefundDate && (
              <p><strong>Estimated Refund Date:</strong> {new Date(tracking.estimatedRefundDate).toLocaleDateString()}</p>
            )}
          </div>

          {tracking.returnLabelUrl && (
            <div className="return-label">
              <a href={tracking.returnLabelUrl} target="_blank" rel="noopener noreferrer" className="btn btn-secondary">
                Download Return Label
              </a>
            </div>
          )}

          <div className="status-timeline">
            <h3>Status History</h3>
            <div className="timeline">
              {tracking.statusHistory.map((history, index) => (
                <div key={index} className="timeline-item">
                  <div className="timeline-marker" style={{ backgroundColor: getStatusColor(history.status) }}></div>
                  <div className="timeline-content">
                    <strong>{history.status.replace('_', ' ')}</strong>
                    <p>{new Date(history.createdAt).toLocaleString()}</p>
                    {history.notes && <p className="timeline-notes">{history.notes}</p>}
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="return-items-list">
            <h3>Return Items</h3>
            {tracking.items.map((item, index) => (
              <div key={index} className="return-item-card">
                <p><strong>{item.productName}</strong></p>
                <p>Quantity: {item.quantity}</p>
                <p>Reason: {item.returnReason.replace('_', ' ')}</p>
                {item.refundAmount && (
                  <p>Refund: ${item.refundAmount.toFixed(2)}</p>
                )}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

