import React, { useEffect, useState } from 'react';
import { useAppContext } from '../context/AppContext';
import { priceAlertService, type PriceAlert, type PriceHistory } from '../services/priceAlertService';
import './PriceAlertDashboard.css';

export const PriceAlertDashboard: React.FC = () => {
  const { user } = useAppContext();
  const [alerts, setAlerts] = useState<PriceAlert[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedAlertId, setSelectedAlertId] = useState<string | null>(null);
  const [priceHistory, setPriceHistory] = useState<PriceHistory[]>([]);
  const [loadingHistory, setLoadingHistory] = useState(false);

  useEffect(() => {
    if (user?.email) {
      loadAlerts();
    }
  }, [user]);

  const loadAlerts = async () => {
    if (!user?.email) return;
    
    try {
      setLoading(true);
      setError(null);
      const data = await priceAlertService.getAlerts(user.email, user.userId);
      setAlerts(data);
    } catch (err) {
      setError('Failed to load price alerts');
      console.error('Error loading price alerts:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadPriceHistory = async (alertId: string) => {
    if (selectedAlertId === alertId && priceHistory.length > 0) {
      setSelectedAlertId(null);
      setPriceHistory([]);
      return;
    }

    try {
      setLoadingHistory(true);
      const history = await priceAlertService.getPriceHistory(alertId);
      setPriceHistory(history);
      setSelectedAlertId(alertId);
    } catch (err) {
      console.error('Error loading price history:', err);
    } finally {
      setLoadingHistory(false);
    }
  };

  const handleDeleteAlert = async (alertId: string) => {
    if (!window.confirm('Are you sure you want to cancel this price alert?')) {
      return;
    }

    try {
      await priceAlertService.deleteAlert(alertId);
      await loadAlerts();
      if (selectedAlertId === alertId) {
        setSelectedAlertId(null);
        setPriceHistory([]);
      }
    } catch (err) {
      setError('Failed to cancel price alert');
      console.error('Error deleting alert:', err);
    }
  };

  const handleUpdateAlert = async (alertId: string, updates: { targetPrice?: number; notificationFrequency?: string }) => {
    try {
      await priceAlertService.updateAlert(alertId, updates);
      await loadAlerts();
    } catch (err) {
      setError('Failed to update price alert');
      console.error('Error updating alert:', err);
    }
  };

  if (!user) {
    return (
      <div className="price-alert-dashboard">
        <div className="price-alert-dashboard-error">
          Please log in to view your price alerts.
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="price-alert-dashboard loading">
        <div>Loading price alerts...</div>
      </div>
    );
  }

  if (error && alerts.length === 0) {
    return (
      <div className="price-alert-dashboard error">
        <div>{error}</div>
        <button onClick={loadAlerts} className="price-alert-dashboard-retry-btn">
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="price-alert-dashboard">
      <div className="price-alert-dashboard-header">
        <h2 className="price-alert-dashboard-title">Price Alerts</h2>
        <button onClick={loadAlerts} className="btn btn-secondary">
          Refresh
        </button>
      </div>

      {error && (
        <div className="price-alert-dashboard-error-banner">
          {error}
        </div>
      )}

      {alerts.length === 0 ? (
        <div className="price-alert-dashboard-empty">
          <p>You don't have any active price alerts.</p>
          <p>Create price alerts from product pages to be notified when prices drop.</p>
        </div>
      ) : (
        <div className="price-alert-dashboard-list">
          {alerts.map((alert) => (
            <div key={alert.alertId} className="price-alert-card">
              <div className="price-alert-card-header">
                <div className="price-alert-card-info">
                  <h3>Product ID: {alert.productId}</h3>
                  <div className="price-alert-card-details">
                    <span>Current Price: ${alert.currentPrice.toFixed(2)}</span>
                    {alert.targetPrice && (
                      <span>Target Price: ${alert.targetPrice.toFixed(2)}</span>
                    )}
                    <span>Status: {alert.status}</span>
                    <span>Frequency: {alert.notificationFrequency}</span>
                  </div>
                </div>
                <div className="price-alert-card-actions">
                  <button
                    onClick={() => loadPriceHistory(alert.alertId)}
                    className="btn btn-secondary"
                    disabled={loadingHistory}
                  >
                    {selectedAlertId === alert.alertId ? 'Hide History' : 'View History'}
                  </button>
                  <button
                    onClick={() => handleDeleteAlert(alert.alertId)}
                    className="btn btn-danger"
                  >
                    Cancel Alert
                  </button>
                </div>
              </div>

              {selectedAlertId === alert.alertId && (
                <div className="price-alert-history">
                  {loadingHistory ? (
                    <div>Loading price history...</div>
                  ) : priceHistory.length === 0 ? (
                    <div>No price history available.</div>
                  ) : (
                    <table className="price-history-table">
                      <thead>
                        <tr>
                          <th>Date</th>
                          <th>Price</th>
                          <th>Previous Price</th>
                          <th>Change</th>
                          <th>Change %</th>
                        </tr>
                      </thead>
                      <tbody>
                        {priceHistory.map((history) => (
                          <tr key={history.priceHistoryId}>
                            <td>{new Date(history.changedAt).toLocaleString()}</td>
                            <td>${history.price.toFixed(2)}</td>
                            <td>{history.previousPrice ? `$${history.previousPrice.toFixed(2)}` : 'N/A'}</td>
                            <td className={`change-type-${history.changeType.toLowerCase()}`}>
                              {history.changeType}
                            </td>
                            <td>
                              {history.changePercentage !== null && history.changePercentage !== undefined
                                ? `${history.changePercentage.toFixed(2)}%`
                                : 'N/A'}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

