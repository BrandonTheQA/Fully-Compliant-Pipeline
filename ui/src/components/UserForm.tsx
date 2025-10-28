import React, { useState } from 'react';
import { useAppContext } from '../context/AppContext';
import { userService } from '../services/userService';
import type { CreateUserRequest } from '../types';
import './UserForm.css';

export const UserForm: React.FC = () => {
  const { user, setUser } = useAppContext();
  const [formData, setFormData] = useState<CreateUserRequest>({
    name: '',
    email: '',
    password: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);
    setLoading(true);

    try {
      // Validate form
      if (!formData.name.trim()) {
        throw new Error('Name is required');
      }
      if (!formData.email.trim()) {
        throw new Error('Email is required');
      }
      if (!formData.password.trim() || formData.password.length < 6) {
        throw new Error('Password must be at least 6 characters');
      }

      const createdUser = await userService.createUser(formData);
      setUser(createdUser);
      setSuccess(true);
      setFormData({ name: '', email: '', password: '' });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create user');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  if (user) {
    return (
      <div className="user-form-container">
        <div className="user-info">
          <h2>Current User</h2>
          <p><strong>Name:</strong> {user.name}</p>
          <p><strong>Email:</strong> {user.email}</p>
          <p><strong>User ID:</strong> {user.userId}</p>
          <button onClick={() => setUser(null)} className="btn btn-secondary">
            Logout
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="user-form-container">
      <h2>Create User Account</h2>
      <form onSubmit={handleSubmit} className="user-form">
        <div className="form-group">
          <label htmlFor="name">Name</label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
            disabled={loading}
          />
        </div>

        <div className="form-group">
          <label htmlFor="email">Email</label>
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
            disabled={loading}
          />
        </div>

        <div className="form-group">
          <label htmlFor="password">Password</label>
          <input
            type="password"
            id="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            required
            minLength={6}
            disabled={loading}
          />
        </div>

        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">User created successfully!</div>}

        <button type="submit" disabled={loading} className="btn btn-primary">
          {loading ? 'Creating...' : 'Create User'}
        </button>
      </form>
    </div>
  );
};

