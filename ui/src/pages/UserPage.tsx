import React from 'react';
import { UserForm } from '../components/UserForm';
import './UserPage.css';

export const UserPage: React.FC = () => {
  return (
    <div className="page-container">
      <UserForm />
    </div>
  );
};

