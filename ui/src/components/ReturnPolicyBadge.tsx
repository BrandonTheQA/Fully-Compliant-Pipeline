import React, { useState, useEffect } from 'react';
import { returnService } from '../services/returnService';
import type { ReturnPolicy } from '../types';
import './ReturnPolicyBadge.css';

interface ReturnPolicyBadgeProps {
  onClick?: () => void;
}

export const ReturnPolicyBadge: React.FC<ReturnPolicyBadgeProps> = ({ onClick }) => {
  const [policy, setPolicy] = useState<ReturnPolicy | null>(null);

  useEffect(() => {
    returnService.getReturnPolicy()
      .then(setPolicy)
      .catch(() => {
        // Silently fail - use default
        setPolicy({ returnWindowDays: 30 });
      });
  }, []);

  if (!policy) {
    return null;
  }

  return (
    <div className="return-policy-badge" onClick={onClick}>
      <span className="badge-icon">↩</span>
      <span className="badge-text">
        {policy.returnWindowDays}-Day Returns
      </span>
    </div>
  );
};

