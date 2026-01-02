import React, { useState, useEffect } from 'react';
import { returnService } from '../services/returnService';
import type { ReturnPolicy } from '../types';
import './ReturnPolicyPage.css';

export const ReturnPolicyPage: React.FC = () => {
  const [policy, setPolicy] = useState<ReturnPolicy | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadPolicy();
  }, []);

  const loadPolicy = async () => {
    setLoading(true);
    setError(null);
    try {
      const policyData = await returnService.getReturnPolicy();
      setPolicy(policyData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load return policy');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="page-container">
        <div className="loading">Loading return policy...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-container">
        <div className="error-message">{error}</div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <h1>Return Policy</h1>

      {policy && (
        <div className="return-policy-content">
          <section className="policy-section">
            <h2>Return Window</h2>
            <p>
              You have <strong>{policy.returnWindowDays} days</strong> from the delivery date to initiate a return.
            </p>
          </section>

          <section className="policy-section">
            <h2>Eligible Items</h2>
            <p>Most items are eligible for return, except:</p>
            <ul>
              <li>Personalized or customized items</li>
              <li>Items that have been used or damaged by the customer</li>
              <li>Items returned without original packaging</li>
            </ul>
          </section>

          <section className="policy-section">
            <h2>Return Methods</h2>
            <p>You can return items using:</p>
            <ul>
              <li>Prepaid return shipping label (if eligible)</li>
              <li>Customer-paid return shipping</li>
            </ul>
            {policy.freeReturnThreshold && policy.freeReturnThreshold > 0 && (
              <p>
                Orders over ${policy.freeReturnThreshold.toFixed(2)} qualify for free return shipping.
              </p>
            )}
          </section>

          <section className="policy-section">
            <h2>Refund Processing</h2>
            <p>
              Refunds are typically processed within <strong>1-3 business days</strong> after we receive your return.
              Refunds will be issued to your original payment method.
            </p>
          </section>

          {policy.restockingFeePercentage && policy.restockingFeePercentage > 0 && (
            <section className="policy-section">
              <h2>Restocking Fees</h2>
              <p>
                A restocking fee of <strong>{policy.restockingFeePercentage}%</strong> may apply to certain returns.
              </p>
            </section>
          )}

          <section className="policy-section">
            <h2>Return Types</h2>
            <ul>
              <li><strong>Refund to Original Payment:</strong> Full refund to your original payment method</li>
              <li><strong>Store Credit:</strong> Receive store credit for future purchases</li>
              <li><strong>Exchange:</strong> Exchange for a different item or size/color</li>
            </ul>
          </section>

          <section className="policy-section">
            <h2>How to Return</h2>
            <ol>
              <li>Log in to your account and go to "Request a Return"</li>
              <li>Select the order and items you want to return</li>
              <li>Choose your return reason and return type</li>
              <li>Submit your return request</li>
              <li>You'll receive an RMA number and return instructions</li>
              <li>Print the return label and ship the items back</li>
              <li>Track your return status using your RMA number</li>
            </ol>
          </section>

          <section className="policy-section">
            <h2>Questions?</h2>
            <p>
              If you have any questions about our return policy, please contact our customer service team.
            </p>
          </section>
        </div>
      )}
    </div>
  );
};

