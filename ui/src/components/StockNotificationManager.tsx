import React, { useEffect, useState } from 'react';
import { stockService, type NotificationResponse } from '../services/stockService';
import { useAppContext } from '../context/AppContext';
import './StockNotificationManager.css';

export const StockNotificationManager: React.FC = () => {
  const { user } = useAppContext();
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (user?.userId) {
      loadNotifications();
    }
  }, [user?.userId]);

  const loadNotifications = async () => {
    if (!user?.userId) return;

    setLoading(true);
    setError(null);
    try {
      const data = await stockService.getUserNotifications(user.userId);
      setNotifications(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load notifications');
    } finally {
      setLoading(false);
    }
  };

  const handleUnsubscribe = async (notificationId: string) => {
    try {
      await stockService.unsubscribe(notificationId);
      setNotifications(notifications.filter(n => n.notificationId !== notificationId));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to unsubscribe');
    }
  };

  if (!user?.userId) {
    return (
      <div className="stock-notification-manager">
        <p>Please log in to manage your stock notifications.</p>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="stock-notification-manager">
        <div className="loading">Loading notifications...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="stock-notification-manager">
        <div className="error-message">
          {error}
          <button onClick={loadNotifications} className="btn btn-secondary">
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="stock-notification-manager">
      <h2>Stock Notifications</h2>
      <p className="notification-manager-description">
        You'll receive an email when these products are back in stock.
      </p>
      {notifications.length === 0 ? (
        <div className="no-notifications">
          <p>You don't have any active stock notifications.</p>
        </div>
      ) : (
        <div className="notifications-list">
          {notifications.map((notification) => (
            <div key={notification.notificationId} className="notification-item">
              <div className="notification-info">
                <h4>{notification.productName}</h4>
                <p className="notification-status">
                  Status: <span className={`status-${notification.status.toLowerCase()}`}>
                    {notification.status}
                  </span>
                </p>
                {notification.signupDate && (
                  <p className="notification-date">
                    Signed up: {new Date(notification.signupDate).toLocaleDateString()}
                  </p>
                )}
                {notification.notifiedDate && (
                  <p className="notification-date">
                    Notified: {new Date(notification.notifiedDate).toLocaleDateString()}
                  </p>
                )}
              </div>
              <div className="notification-actions">
                <button
                  onClick={() => handleUnsubscribe(notification.notificationId)}
                  className="btn btn-secondary"
                  type="button"
                >
                  Unsubscribe
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

