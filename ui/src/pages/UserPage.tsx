import React from 'react';
import { UserForm } from '../components/UserForm';
import { LoyaltyDashboard } from '../components/LoyaltyDashboard';
import { useAppContext } from '../context/AppContext';
import './UserPage.css';

export const UserPage: React.FC = () => {
  const { user } = useAppContext();

  return (
    <div className="page-container">
      <UserForm />
      {user && (
        <div style={{ marginTop: '2rem' }}>
          <LoyaltyDashboard userId={user.userId} />
        </div>
      )}
    </div>
  );
};

