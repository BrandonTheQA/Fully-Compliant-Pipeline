/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { WishlistPage } from '../WishlistPage';
import { AppProvider } from '../../context/AppContext';
import { wishlistService } from '../../services/wishlistService';
import type { User, Product } from '../../types';

jest.mock('../../services/wishlistService');

const renderWithProvider = (component: React.ReactElement) => {
  return render(
    <BrowserRouter>
      <AppProvider>{component}</AppProvider>
    </BrowserRouter>
  );
};

describe('WishlistPage', () => {
  const mockUser: User = {
    userId: 'user-123',
    name: 'John Doe',
    email: 'john@example.com',
  };

  const mockProduct1: Product = {
    id: 'product-1',
    name: 'Test Product 1',
    description: 'Test Description 1',
    price: 29.99,
    quantity: 10,
    category: 'Electronics',
  };

  const mockProduct2: Product = {
    id: 'product-2',
    name: 'Test Product 2',
    description: 'Test Description 2',
    price: 39.99,
    quantity: 5,
    category: 'Books',
  };

  beforeEach(() => {
    jest.clearAllMocks();
    sessionStorage.clear();
    (wishlistService.getWishlist as jest.MockedFunction<typeof wishlistService.getWishlist>).mockResolvedValue([]);
  });

  it('should display message when user is not logged in', () => {
    renderWithProvider(<WishlistPage />);
    
    expect(screen.getByText('Please log in to view your wishlist.')).toBeInTheDocument();
  });

  it('should display empty wishlist message when wishlist is empty', async () => {
    sessionStorage.setItem('user', JSON.stringify(mockUser));
    (wishlistService.getWishlist as jest.MockedFunction<typeof wishlistService.getWishlist>).mockResolvedValue([]);

    renderWithProvider(<WishlistPage />);

    await waitFor(() => {
      expect(screen.getByText('Your wishlist is empty.')).toBeInTheDocument();
    });
  });

  it('should display wishlist items when user is logged in', async () => {
    sessionStorage.setItem('user', JSON.stringify(mockUser));
    (wishlistService.getWishlist as jest.MockedFunction<typeof wishlistService.getWishlist>).mockResolvedValue([mockProduct1, mockProduct2]);

    renderWithProvider(<WishlistPage />);

    await waitFor(() => {
      expect(screen.getByText('My Wishlist')).toBeInTheDocument();
      expect(screen.getByText('Test Product 1')).toBeInTheDocument();
      expect(screen.getByText('Test Product 2')).toBeInTheDocument();
    });
  });

  it('should display product prices', async () => {
    sessionStorage.setItem('user', JSON.stringify(mockUser));
    (wishlistService.getWishlist as jest.MockedFunction<typeof wishlistService.getWishlist>).mockResolvedValue([mockProduct1]);

    renderWithProvider(<WishlistPage />);

    await waitFor(() => {
      expect(screen.getByText('$29.99')).toBeInTheDocument();
    });
  });

  it('should move product to cart when Move to Cart button is clicked', async () => {
    sessionStorage.setItem('user', JSON.stringify(mockUser));
    (wishlistService.getWishlist as jest.MockedFunction<typeof wishlistService.getWishlist>).mockResolvedValue([mockProduct1]);
    (wishlistService.removeItem as jest.MockedFunction<typeof wishlistService.removeItem>).mockResolvedValue(undefined);

    renderWithProvider(<WishlistPage />);

    await waitFor(() => {
      expect(screen.getByText('Test Product 1')).toBeInTheDocument();
    });

    const moveToCartButton = screen.getByText('Move to Cart');
    fireEvent.click(moveToCartButton);

    await waitFor(() => {
      expect(wishlistService.removeItem).toHaveBeenCalledWith('user-123', 'product-1');
    });
  });

  it('should remove product from wishlist when Remove button is clicked', async () => {
    sessionStorage.setItem('user', JSON.stringify(mockUser));
    (wishlistService.getWishlist as jest.MockedFunction<typeof wishlistService.getWishlist>).mockResolvedValue([mockProduct1]);
    (wishlistService.removeItem as jest.MockedFunction<typeof wishlistService.removeItem>).mockResolvedValue(undefined);

    renderWithProvider(<WishlistPage />);

    await waitFor(() => {
      expect(screen.getByText('Test Product 1')).toBeInTheDocument();
    });

    const removeButton = screen.getByText('Remove');
    fireEvent.click(removeButton);

    await waitFor(() => {
      expect(wishlistService.removeItem).toHaveBeenCalledWith('user-123', 'product-1');
    });
  });

  it('should handle multiple products in wishlist', async () => {
    sessionStorage.setItem('user', JSON.stringify(mockUser));
    (wishlistService.getWishlist as jest.MockedFunction<typeof wishlistService.getWishlist>).mockResolvedValue([mockProduct1, mockProduct2]);

    renderWithProvider(<WishlistPage />);

    await waitFor(() => {
      expect(screen.getByText('Test Product 1')).toBeInTheDocument();
      expect(screen.getByText('Test Product 2')).toBeInTheDocument();
      expect(screen.getAllByText('Move to Cart')).toHaveLength(2);
      expect(screen.getAllByText('Remove')).toHaveLength(2);
    });
  });

  it('should handle move to cart for specific product', async () => {
    sessionStorage.setItem('user', JSON.stringify(mockUser));
    (wishlistService.getWishlist as jest.MockedFunction<typeof wishlistService.getWishlist>).mockResolvedValue([mockProduct1, mockProduct2]);
    (wishlistService.removeItem as jest.MockedFunction<typeof wishlistService.removeItem>).mockResolvedValue(undefined);

    renderWithProvider(<WishlistPage />);

    await waitFor(() => {
      expect(screen.getByText('Test Product 1')).toBeInTheDocument();
    });

    const moveToCartButtons = screen.getAllByText('Move to Cart');
    fireEvent.click(moveToCartButtons[0]); // Click first product's button

    await waitFor(() => {
      expect(wishlistService.removeItem).toHaveBeenCalled();
    });
  });
});
