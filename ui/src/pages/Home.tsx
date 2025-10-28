import React from 'react';
import { Link } from 'react-router-dom';
import { useAppContext } from '../context/AppContext';
import './Home.css';

export const Home: React.FC = () => {
  const { user, cart } = useAppContext();

  return (
    <div className="home-container">
      <h1>Welcome to the E-Commerce Platform</h1>
      <p className="subtitle">Complete the happy path workflow: Create User → Browse Products → Place Order</p>

      <div className="workflow-steps">
        <div className="step">
          <div className="step-number">1</div>
          <h3>Create User</h3>
          <p>Start by creating a user account</p>
          <Link to="/user" className="btn btn-primary">
            {user ? 'View/Update User' : 'Create User'}
          </Link>
        </div>

        <div className="step">
          <div className="step-number">2</div>
          <h3>Products</h3>
          <p>Browse and manage products</p>
          <Link to="/products" className="btn btn-primary">
            View Products
          </Link>
        </div>

        <div className="step">
          <div className="step-number">3</div>
          <h3>Orders</h3>
          <p>Create and view orders</p>
          <Link to="/orders" className="btn btn-primary">
            {cart.length > 0 ? `Cart (${cart.length})` : 'View Orders'}
          </Link>
        </div>
      </div>

      {user && (
        <div className="user-status">
          <p>Logged in as: <strong>{user.name}</strong> ({user.email})</p>
        </div>
      )}
    </div>
  );
};

