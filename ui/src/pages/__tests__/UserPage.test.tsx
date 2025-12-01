/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect, beforeEach } from '@jest/globals';
import { render } from '@testing-library/react';
import { screen } from '@testing-library/dom';
import { BrowserRouter } from 'react-router-dom';
import { UserPage } from '../UserPage';
import { AppProvider } from '../../context/AppContext';

const renderWithProvider = (component: React.ReactElement) => {
  return render(
    <BrowserRouter>
      <AppProvider>{component}</AppProvider>
    </BrowserRouter>
  );
};

describe('UserPage', () => {
  beforeEach(() => {
    sessionStorage.clear();
  });

  it('should render UserPage', () => {
    renderWithProvider(<UserPage />);
    // UserPage is a simple wrapper that renders UserForm
    // We can verify by checking that UserForm is rendered
    expect(screen.getByText('Create User Account')).toBeInTheDocument();
  });

  it('should render UserForm component', () => {
    renderWithProvider(<UserPage />);
    expect(screen.getByLabelText('Name')).toBeInTheDocument();
    expect(screen.getByLabelText('Email')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();
  });

  it('should have page container class', () => {
    const { container } = renderWithProvider(<UserPage />);
    const pageContainer = container.querySelector('.page-container');
    expect(pageContainer).toBeInTheDocument();
  });
});



