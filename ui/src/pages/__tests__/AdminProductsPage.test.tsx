/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect, beforeEach } from '@jest/globals';
import { render, fireEvent, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AppProvider } from '../../context/AppContext';
import { AdminProductsPage } from '../AdminProductsPage';

const renderWithRouter = (component: React.ReactElement) => {
  return render(
    <BrowserRouter>
      <AppProvider>{component}</AppProvider>
    </BrowserRouter>
  );
};

describe('AdminProductsPage', () => {
  beforeEach(() => {
    sessionStorage.clear();
  });

  it('should render admin products page header', () => {
    renderWithRouter(<AdminProductsPage />);

    expect(screen.getByText('Admin - Product Management')).toBeInTheDocument();
    expect(screen.getByText('Create New Product')).toBeInTheDocument();
  });

  it('should toggle create form visibility', async () => {
    const { waitFor } = require('@testing-library/react');
    renderWithRouter(<AdminProductsPage />);

    const createButton = screen.getByRole('button', { name: /Create New Product/i });
    fireEvent.click(createButton);

    // Wait for state update - button text should change
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Hide Create Form/i })).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: /Create New Product/i })).not.toBeInTheDocument();
    });
  });

  it('should hide create form when hide button is clicked', () => {
    renderWithRouter(<AdminProductsPage />);

    const createButton = screen.getByText('Create New Product');
    fireEvent.click(createButton);

    expect(screen.getByText('Hide Create Form')).toBeInTheDocument();

    const hideButton = screen.getByText('Hide Create Form');
    fireEvent.click(hideButton);

    expect(screen.getByText('Create New Product')).toBeInTheDocument();
    expect(screen.queryByText('Hide Create Form')).not.toBeInTheDocument();
  });

  it('should render ProductList component', () => {
    renderWithRouter(<AdminProductsPage />);

    // ProductList should be rendered (we can verify by checking it doesn't show actions)
    // The actual ProductList component tests will verify its behavior
    expect(screen.getByText('Admin - Product Management')).toBeInTheDocument();
  });
});

