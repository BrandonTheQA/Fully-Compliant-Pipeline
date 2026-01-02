import React, { useState, useEffect } from 'react';
import { returnService } from '../services/returnService';
import type { Return, ReturnStatus, ReturnAnalytics } from '../types';
import './AdminReturnsPage.css';

export const AdminReturnsPage: React.FC = () => {
  const [returns, setReturns] = useState<Return[]>([]);
  const [selectedReturn, setSelectedReturn] = useState<Return | null>(null);
  const [analytics, setAnalytics] = useState<ReturnAnalytics | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [viewMode, setViewMode] = useState<'queue' | 'details' | 'analytics'>('queue');
  const [filters, setFilters] = useState({
    status: '',
    userId: '',
    orderId: '',
    rmaNumber: '',
  });

  useEffect(() => {
    if (viewMode === 'queue') {
      loadReturns();
    } else if (viewMode === 'analytics') {
      loadAnalytics();
    }
  }, [viewMode, filters]);

  const loadReturns = async () => {
    setLoading(true);
    setError(null);
    try {
      const returnsData = await returnService.getAdminReturns(filters);
      setReturns(returnsData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load returns');
    } finally {
      setLoading(false);
    }
  };

  const loadAnalytics = async () => {
    setLoading(true);
    setError(null);
    try {
      const analyticsData = await returnService.getAnalytics();
      setAnalytics(analyticsData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load analytics');
    } finally {
      setLoading(false);
    }
  };

  const handleViewReturn = async (returnId: string) => {
    setLoading(true);
    setError(null);
    try {
      const returnData = await returnService.getAdminReturn(returnId);
      setSelectedReturn(returnData);
      setViewMode('details');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load return');
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (returnId: string, notes?: string) => {
    try {
      await returnService.approveReturn(returnId, notes);
      await loadReturns();
      if (selectedReturn?.returnId === returnId) {
        await handleViewReturn(returnId);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to approve return');
    }
  };

  const handleReject = async (returnId: string, reason: string) => {
    if (!reason.trim()) {
      setError('Please provide a rejection reason');
      return;
    }
    try {
      await returnService.rejectReturn(returnId, reason);
      await loadReturns();
      if (selectedReturn?.returnId === returnId) {
        await handleViewReturn(returnId);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to reject return');
    }
  };

  const handleUpdateStatus = async (returnId: string, status: ReturnStatus, notes?: string) => {
    try {
      await returnService.updateReturnStatus(returnId, status, notes);
      await loadReturns();
      if (selectedReturn?.returnId === returnId) {
        await handleViewReturn(returnId);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update status');
    }
  };

  const handleMarkReceived = async (returnId: string, notes?: string) => {
    try {
      await returnService.markReturnReceived(returnId, notes);
      await loadReturns();
      if (selectedReturn?.returnId === returnId) {
        await handleViewReturn(returnId);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to mark return as received');
    }
  };

  const handleProcessRefund = async (returnId: string) => {
    try {
      await returnService.processRefund(returnId);
      await loadReturns();
      if (selectedReturn?.returnId === returnId) {
        await handleViewReturn(returnId);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to process refund');
    }
  };

  return (
    <div className="page-container">
      <div className="admin-returns-header">
        <h1>Admin - Return Management</h1>
        <div className="view-toggle">
          <button
            onClick={() => setViewMode('queue')}
            className={`btn ${viewMode === 'queue' ? 'btn-primary' : 'btn-secondary'}`}
          >
            Return Queue
          </button>
          <button
            onClick={() => setViewMode('analytics')}
            className={`btn ${viewMode === 'analytics' ? 'btn-primary' : 'btn-secondary'}`}
          >
            Analytics
          </button>
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}

      {viewMode === 'queue' && (
        <>
          <div className="filters-section">
            <h2>Filters</h2>
            <div className="filters">
              <label>
                Status:
                <select
                  value={filters.status}
                  onChange={(e) => setFilters({ ...filters, status: e.target.value })}
                >
                  <option value="">All</option>
                  <option value="PENDING_APPROVAL">Pending Approval</option>
                  <option value="APPROVED">Approved</option>
                  <option value="REJECTED">Rejected</option>
                  <option value="IN_TRANSIT">In Transit</option>
                  <option value="RECEIVED">Received</option>
                  <option value="PROCESSING_REFUND">Processing Refund</option>
                  <option value="REFUNDED">Refunded</option>
                  <option value="COMPLETED">Completed</option>
                </select>
              </label>
              <label>
                User ID:
                <input
                  type="text"
                  value={filters.userId}
                  onChange={(e) => setFilters({ ...filters, userId: e.target.value })}
                  placeholder="Filter by user ID"
                />
              </label>
              <label>
                Order ID:
                <input
                  type="text"
                  value={filters.orderId}
                  onChange={(e) => setFilters({ ...filters, orderId: e.target.value })}
                  placeholder="Filter by order ID"
                />
              </label>
              <label>
                RMA Number:
                <input
                  type="text"
                  value={filters.rmaNumber}
                  onChange={(e) => setFilters({ ...filters, rmaNumber: e.target.value })}
                  placeholder="Search by RMA"
                />
              </label>
            </div>
          </div>

          {loading ? (
            <div className="loading">Loading returns...</div>
          ) : returns.length === 0 ? (
            <div className="no-returns">No returns found</div>
          ) : (
            <div className="returns-list">
              {returns.map((returnItem) => (
                <div key={returnItem.returnId} className="return-card">
                  <h3>RMA: {returnItem.rmaNumber}</h3>
                  <p><strong>Status:</strong> {returnItem.status}</p>
                  <p><strong>Order ID:</strong> {returnItem.orderId}</p>
                  <p><strong>User ID:</strong> {returnItem.userId}</p>
                  <p><strong>Refund Amount:</strong> ${returnItem.refundAmount?.toFixed(2) || '0.00'}</p>
                  <button
                    onClick={() => handleViewReturn(returnItem.returnId)}
                    className="btn btn-secondary"
                  >
                    View Details
                  </button>
                </div>
              ))}
            </div>
          )}
        </>
      )}

      {viewMode === 'details' && selectedReturn && (
        <div className="return-details">
          <button
            onClick={() => {
              setViewMode('queue');
              setSelectedReturn(null);
            }}
            className="btn btn-secondary"
          >
            ← Back to Queue
          </button>

          <div className="return-details-content">
            <h2>Return Details: {selectedReturn.rmaNumber}</h2>
            
            <div className="details-section">
              <h3>Return Information</h3>
              <p><strong>RMA Number:</strong> {selectedReturn.rmaNumber}</p>
              <p><strong>Status:</strong> {selectedReturn.status}</p>
              <p><strong>Return Type:</strong> {selectedReturn.returnType}</p>
              <p><strong>Order ID:</strong> {selectedReturn.orderId}</p>
              <p><strong>User ID:</strong> {selectedReturn.userId}</p>
              <p><strong>Refund Amount:</strong> ${selectedReturn.refundAmount?.toFixed(2) || '0.00'}</p>
              <p><strong>Created:</strong> {new Date(selectedReturn.createdAt).toLocaleString()}</p>
            </div>

            <div className="details-section">
              <h3>Return Items</h3>
              {selectedReturn.items.map((item, index) => (
                <div key={index} className="return-item-detail">
                  <p><strong>{item.productName}</strong></p>
                  <p>Quantity: {item.quantity}</p>
                  <p>Reason: {item.returnReason}</p>
                  {item.refundAmount && <p>Refund: ${item.refundAmount.toFixed(2)}</p>}
                </div>
              ))}
            </div>

            <div className="details-section">
              <h3>Status History</h3>
              {selectedReturn.statusHistory.map((history, index) => (
                <div key={index} className="status-history-item">
                  <p><strong>{history.status}</strong> - {new Date(history.createdAt).toLocaleString()}</p>
                  {history.notes && <p>{history.notes}</p>}
                </div>
              ))}
            </div>

            <div className="admin-actions">
              <h3>Actions</h3>
              {selectedReturn.status === 'PENDING_APPROVAL' && (
                <>
                  <button
                    onClick={() => {
                      const notes = prompt('Approval notes (optional):');
                      handleApprove(selectedReturn.returnId, notes || undefined);
                    }}
                    className="btn btn-success"
                  >
                    Approve Return
                  </button>
                  <button
                    onClick={() => {
                      const reason = prompt('Rejection reason (required):');
                      if (reason) {
                        handleReject(selectedReturn.returnId, reason);
                      }
                    }}
                    className="btn btn-danger"
                  >
                    Reject Return
                  </button>
                </>
              )}
              {selectedReturn.status === 'APPROVED' && (
                <button
                  onClick={() => {
                    const notes = prompt('Notes (optional):');
                    handleMarkReceived(selectedReturn.returnId, notes || undefined);
                  }}
                  className="btn btn-primary"
                >
                  Mark as Received
                </button>
              )}
              {selectedReturn.status === 'RECEIVED' && (
                <button
                  onClick={() => handleProcessRefund(selectedReturn.returnId)}
                  className="btn btn-primary"
                >
                  Process Refund
                </button>
              )}
              <div className="status-update">
                <label>
                  Update Status:
                  <select
                    onChange={(e) => {
                      const notes = prompt('Notes (optional):');
                      handleUpdateStatus(selectedReturn.returnId, e.target.value as ReturnStatus, notes || undefined);
                    }}
                  >
                    <option value="">Select status...</option>
                    <option value="PENDING_APPROVAL">Pending Approval</option>
                    <option value="APPROVED">Approved</option>
                    <option value="REJECTED">Rejected</option>
                    <option value="IN_TRANSIT">In Transit</option>
                    <option value="RECEIVED">Received</option>
                    <option value="PROCESSING_REFUND">Processing Refund</option>
                    <option value="REFUNDED">Refunded</option>
                    <option value="COMPLETED">Completed</option>
                  </select>
                </label>
              </div>
            </div>
          </div>
        </div>
      )}

      {viewMode === 'analytics' && analytics && (
        <div className="analytics-dashboard">
          <h2>Return Analytics</h2>
          
          <div className="analytics-grid">
            <div className="analytics-card">
              <h3>Total Returns</h3>
              <p className="analytics-value">{analytics.totalReturns}</p>
            </div>
            <div className="analytics-card">
              <h3>Total Return Value</h3>
              <p className="analytics-value">${analytics.totalReturnValue.toFixed(2)}</p>
            </div>
            <div className="analytics-card">
              <h3>Average Processing Time</h3>
              <p className="analytics-value">{analytics.averageReturnProcessingTime.toFixed(1)} days</p>
            </div>
            <div className="analytics-card">
              <h3>Return Rate</h3>
              <p className="analytics-value">{(analytics.returnRate * 100).toFixed(2)}%</p>
            </div>
          </div>

          <div className="analytics-section">
            <h3>Returns by Status</h3>
            <div className="status-distribution">
              {Object.entries(analytics.returnsByStatus).map(([status, count]) => (
                <div key={status} className="status-item">
                  <span>{status}:</span>
                  <span>{count}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="analytics-section">
            <h3>Return Reasons Distribution</h3>
            <div className="reasons-distribution">
              {Object.entries(analytics.returnReasonsDistribution).map(([reason, count]) => (
                <div key={reason} className="reason-item">
                  <span>{reason}:</span>
                  <span>{count}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

