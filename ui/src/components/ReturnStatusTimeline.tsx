import React from 'react';
import type { ReturnStatusHistory } from '../types';
import './ReturnStatusTimeline.css';

interface ReturnStatusTimelineProps {
  statusHistory: ReturnStatusHistory[];
}

export const ReturnStatusTimeline: React.FC<ReturnStatusTimelineProps> = ({ statusHistory }) => {
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
    <div className="return-status-timeline">
      <h3>Status History</h3>
      <div className="timeline">
        {statusHistory.map((history, index) => (
          <div key={index} className="timeline-item">
            <div
              className="timeline-marker"
              style={{ backgroundColor: getStatusColor(history.status) }}
            ></div>
            <div className="timeline-content">
              <strong>{history.status.replace('_', ' ')}</strong>
              <p>{new Date(history.createdAt).toLocaleString()}</p>
              {history.notes && <p className="timeline-notes">{history.notes}</p>}
              {history.updatedBy && (
                <p className="timeline-updated-by">Updated by: {history.updatedBy}</p>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

