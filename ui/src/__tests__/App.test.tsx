/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect, beforeEach } from '@jest/globals';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import App from '../App';
import { AppProvider } from '../context/AppContext';
import type { User, CartItem, Product } from '../types';

const renderApp = () => {
  return render(
    <BrowserRouter>
      <AppProvider>
        <App />
      </AppProvider>
    </BrowserRouter>
  );
};

describe('App', () => {
  const mockUser: User = {
    userId: 'user-123',
    name: 'John Doe',
    email: 'john@example.com',
  };

  const mockProduct: Product = {
    id: 'product-1',
    name: 'Test Product',
    description: 'Test Description',
    price: 29.99,
    quantity: 10,
    category: 'Electronics',
  };

  const mockCartItem: CartItem = {
    ...mockProduct,
    orderQuantity: 2,
  };

  beforeEach(() => {
    sessionStorage.clear();
  });

  it('should render navigation with logo', () => {
    renderApp();
    expect(screen.getByText(/E-Commerce/i)).toBeInTheDocument();
  });

  it('should render navigation links', () => {
    renderApp();
    expect(screen.getByText('Home')).toBeInTheDocument();
    expect(screen.getByText('User')).toBeInTheDocument();
    expect(screen.getByText('Products')).toBeInTheDocument();
    expect(screen.getByText('Wishlist')).toBeInTheDocument();
    expect(screen.getByText('Orders')).toBeInTheDocument();
  });

  it('should render skip link for accessibility', () => {
    renderApp();
    const skipLink = screen.getByText('Skip to main content');
    expect(skipLink).toBeInTheDocument();
    expect(skipLink).toHaveAttribute('href', '#main-content');
  });

  it('should display cart badge when cart has items', () => {
    sessionStorage.setItem('cart', JSON.stringify([mockCartItem]));
    renderApp();
    
    const ordersLink = screen.getByText('Orders');
    expect(ordersLink).toBeInTheDocument();
    // Cart badge should be visible
    expect(screen.getByText('2')).toBeInTheDocument();
  });

  it('should not display cart badge when cart is empty', () => {
    renderApp();
    
    const ordersLink = screen.getByText('Orders');
    expect(ordersLink).toBeInTheDocument();
    // Cart badge should not be visible
    expect(screen.queryByText('2')).not.toBeInTheDocument();
  });

  it('should set aria-current for active navigation link', () => {
    renderApp();
    
    const homeLink = screen.getByText('Home').closest('a');
    expect(homeLink).toHaveAttribute('aria-current', 'page');
  });

  it('should render Home page by default', () => {
    renderApp();
    expect(screen.getByText(/Welcome to the E-Commerce Platform/i)).toBeInTheDocument();
  });

  it('should render main content area', () => {
    renderApp();
    const mainContent = screen.getByRole('main');
    expect(mainContent).toBeInTheDocument();
    expect(mainContent).toHaveAttribute('id', 'main-content');
  });
});
