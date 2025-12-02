import React, { useState, useEffect } from 'react';
import { orderService } from '../services/orderService';
import type { NotificationPreferences as NotificationPreferencesType } from '../types';
import './NotificationPreferences.css';

interface NotificationPreferencesProps {
  userId: string;
}

export const NotificationPreferences: React.FC<NotificationPreferencesProps> = ({ userId }) => {
  const [preferences, setPreferences] = useState<NotificationPreferencesType | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    loadPreferences();
  }, [userId]);

  const loadPreferences = async () => {
    setLoading(true);
    setError(null);
    try {
      const prefs = await orderService.getNotificationPreferences(userId);
      setPreferences(prefs);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load preferences');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    if (!preferences) return;

    setSaving(true);
    setError(null);
    setSuccess(false);

    try {
      const updated = await orderService.updateNotificationPreferences(userId, preferences);
      setPreferences(updated);
      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save preferences');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="notification-preferences loading">Loading preferences...</div>;
  }

  if (!preferences) {
    return <div className="notification-preferences error">Failed to load preferences</div>;
  }

  return (
    <div className="notification-preferences">
      <h3>Notification Preferences</h3>
      
      {error && <div className="error-message" role="alert">{error}</div>}
      {success && (
        <div className="success-message" role="alert" aria-live="polite">
          Preferences saved successfully!
        </div>
      )}

      <div className="preferences-form">
        <div className="preference-item">
          <label>
            <input
              type="checkbox"
              checked={preferences.emailEnabled}
              onChange={(e) => setPreferences({ ...preferences, emailEnabled: e.target.checked })}
            />
            <span>Email Notifications</span>
          </label>
        </div>

        <div className="preference-item">
          <label>
            <input
              type="checkbox"
              checked={preferences.smsEnabled}
              onChange={(e) => setPreferences({ ...preferences, smsEnabled: e.target.checked })}
            />
            <span>SMS Notifications</span>
          </label>
        </div>

        {preferences.smsEnabled && (
          <div className="preference-item">
            <label>
              Phone Number:
              <input
                type="tel"
                value={preferences.phoneNumber || ''}
                onChange={(e) => setPreferences({ ...preferences, phoneNumber: e.target.value })}
                placeholder="+1234567890"
              />
            </label>
          </div>
        )}

        <div className="preference-item">
          <label>
            Notification Frequency:
            <select
              value={preferences.notificationFrequency}
              onChange={(e) => setPreferences({ 
                ...preferences, 
                notificationFrequency: e.target.value as 'ALL' | 'CRITICAL_ONLY' | 'NONE' 
              })}
            >
              <option value="ALL">All Updates</option>
              <option value="CRITICAL_ONLY">Critical Only</option>
              <option value="NONE">None</option>
            </select>
          </label>
        </div>

        <button
          onClick={handleSave}
          disabled={saving}
          className="btn btn-primary"
        >
          {saving ? 'Saving...' : 'Save Preferences'}
        </button>
      </div>
    </div>
  );
};
