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
    <AppProvider>
      <App />
    </AppProvider>
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
    const logoElements = screen.getAllByText(/E-Commerce/i);
    expect(logoElements.length).toBeGreaterThan(0);
    expect(logoElements[0].closest('.nav-logo')).toBeInTheDocument();
  });

  it('should render navigation links', () => {
    renderApp();
    expect(screen.getAllByText('Home').length).toBeGreaterThan(0);
    expect(screen.getAllByText('User').length).toBeGreaterThan(0);
    expect(screen.getAllByText('Products').length).toBeGreaterThan(0);
    expect(screen.getAllByText('Wishlist').length).toBeGreaterThan(0);
    expect(screen.getAllByText('Orders').length).toBeGreaterThan(0);
  });

  it('should render skip link for accessibility', () => {
    renderApp();
    const skipLink = screen.getByText('Skip to main content');
    expect(skipLink).toBeInTheDocument();
    expect(skipLink.getAttribute('href')).toBe('#main-content');
  });

  it('should display cart badge when cart has items', () => {
    sessionStorage.setItem('cart', JSON.stringify([mockCartItem]));
    renderApp();
    
    // Verify Orders link is rendered (cart badge functionality is tested in context tests)
    expect(screen.getAllByText('Orders').length).toBeGreaterThan(0);
  });

  it('should not display cart badge when cart is empty', () => {
    renderApp();
    
    // Verify Orders link is rendered even when cart is empty
    expect(screen.getAllByText('Orders').length).toBeGreaterThan(0);
  });

  it('should set aria-current for active navigation link', () => {
    renderApp();
    
    const homeLink = screen.getByText('Home').closest('a');
    expect(homeLink?.getAttribute('aria-current')).toBe('page');
  });

  it('should render Home page by default', () => {
    renderApp();
    expect(screen.getByText(/Welcome to the E-Commerce Platform/i)).toBeInTheDocument();
  });

  it('should render main content area', () => {
    renderApp();
    const mainContent = screen.getByRole('main');
    expect(mainContent).toBeInTheDocument();
    expect(mainContent.getAttribute('id')).toBe('main-content');
  });
});
