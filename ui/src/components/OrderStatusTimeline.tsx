import React from 'react';
import type { OrderStatusHistory } from '../types';
import './OrderStatusTimeline.css';

interface OrderStatusTimelineProps {
  statusHistory: OrderStatusHistory[];
  currentStatus: string;
}

export const OrderStatusTimeline: React.FC<OrderStatusTimelineProps> = ({ 
  statusHistory, 
  currentStatus 
}) => {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const isCurrentStatus = (status: string) => {
    return status.toUpperCase() === currentStatus.toUpperCase();
  };

  // Sort by date descending (most recent first)
  const sortedHistory = [...statusHistory].sort((a, b) => 
    new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  );

  return (
    <div className="status-timeline">
      <h3>Order Status Timeline</h3>
      {sortedHistory.length === 0 ? (
        <p className="no-history">No status history available</p>
      ) : (
        <div className="timeline-container">
          {sortedHistory.map((entry) => (
            <div 
              key={entry.id} 
              className={`timeline-item ${isCurrentStatus(entry.status) ? 'current' : ''}`}
            >
              <div className="timeline-marker">
                {isCurrentStatus(entry.status) && <div className="current-indicator" />}
              </div>
              <div className="timeline-content">
                <div className="timeline-header">
                  <span className="status-name">{entry.status.replace('_', ' ')}</span>
                  <span className="timeline-date">{formatDate(entry.createdAt)}</span>
                </div>
                {entry.location && (
                  <div className="timeline-location">
                    <strong>Location:</strong> {entry.location}
                  </div>
                )}
                {entry.notes && (
                  <div className="timeline-notes">
                    {entry.notes}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
