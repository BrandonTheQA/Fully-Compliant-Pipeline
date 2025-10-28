/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { render, fireEvent } from '@testing-library/react';
import { screen, waitFor } from '@testing-library/dom';
import { UserForm } from '../UserForm';
import { AppProvider } from '../../context/AppContext';
import { userService } from '../../services/userService';
import type { User } from '../../types';

jest.mock('../../services/userService');

const renderWithProvider = (component: React.ReactElement) => {
  return render(<AppProvider>{component}</AppProvider>);
};

describe('UserForm', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    sessionStorage.clear();
  });

  it('should render user form', () => {
    renderWithProvider(<UserForm />);
    expect(screen.getByText('Create User Account')).toBeInTheDocument();
    expect(screen.getByLabelText('Name')).toBeInTheDocument();
    expect(screen.getByLabelText('Email')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();
  });

  it('should create user successfully', async () => {
    const mockUser: User = {
      userId: '123',
      name: 'John Doe',
      email: 'john@example.com',
    };

    (userService.createUser as jest.MockedFunction<typeof userService.createUser>).mockResolvedValue(mockUser);

    renderWithProvider(<UserForm />);

    fireEvent.change(screen.getByLabelText('Name'), { target: { value: 'John Doe' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'john@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password123' } });

    fireEvent.click(screen.getByText('Create User'));

    await waitFor(() => {
      expect(userService.createUser).toHaveBeenCalledWith({
        name: 'John Doe',
        email: 'john@example.com',
        password: 'password123',
      });
    });

    await waitFor(() => {
      expect(screen.getByText('Current User')).toBeInTheDocument();
      expect(screen.getByText('John Doe')).toBeInTheDocument();
    });
  });

  it('should show error on validation failure', async () => {
    renderWithProvider(<UserForm />);

    fireEvent.change(screen.getByLabelText('Name'), { target: { value: 'John Doe' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'john@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'pass' } });

    const submitButton = screen.getByText('Create User');
    fireEvent.click(submitButton);

    // Wait for async validation to complete
    await waitFor(() => {
      expect(screen.getByText('Password must be at least 6 characters')).toBeInTheDocument();
    });
  });

  it('should show error message on API failure', async () => {
    const error = new Error('Email already exists');
    (userService.createUser as jest.MockedFunction<typeof userService.createUser>).mockRejectedValue(error);

    renderWithProvider(<UserForm />);

    fireEvent.change(screen.getByLabelText('Name'), { target: { value: 'John Doe' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'john@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password123' } });

    fireEvent.click(screen.getByText('Create User'));

    await waitFor(() => {
      expect(screen.getByText('Email already exists')).toBeInTheDocument();
    });
  });

  it('should display current user when logged in', () => {
    const mockUser: User = {
      userId: '123',
      name: 'John Doe',
      email: 'john@example.com',
    };

    sessionStorage.setItem('user', JSON.stringify(mockUser));

    renderWithProvider(<UserForm />);

    expect(screen.getByText('Current User')).toBeInTheDocument();
    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('123')).toBeInTheDocument();
  });
});

