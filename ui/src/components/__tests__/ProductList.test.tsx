/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { render } from '@testing-library/react';
import { screen, waitFor } from '@testing-library/dom';
import { ProductList } from '../ProductList';
import { AppProvider } from '../../context/AppContext';
import { productService } from '../../services/productService';
import type { Product } from '../../types';

jest.mock('../../services/productService');

const renderWithProvider = (component: React.ReactElement) => {
  return render(<AppProvider>{component}</AppProvider>);
};

describe('ProductList', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render loading state', () => {
    (productService.getAllProducts as jest.MockedFunction<typeof productService.getAllProducts>).mockImplementation(
      () => new Promise(() => {})
    );

    renderWithProvider(<ProductList />);
    expect(screen.getByText('Loading products...')).toBeInTheDocument();
  });

  it('should render products list', async () => {
    const mockProducts = [
      {
        id: '1',
        name: 'Laptop',
        description: 'High-performance laptop',
        price: 999.99,
        quantity: 10,
        category: 'Electronics',
      },
      {
        id: '2',
        name: 'Mouse',
        description: 'Wireless mouse',
        price: 29.99,
        quantity: 50,
        category: 'Electronics',
      },
    ];

    (productService.getAllProducts as jest.MockedFunction<typeof productService.getAllProducts>).mockResolvedValue(mockProducts);

    renderWithProvider(<ProductList />);

    await waitFor(() => {
      expect(screen.getByText('Laptop')).toBeInTheDocument();
      expect(screen.getByText('Mouse')).toBeInTheDocument();
      expect(screen.getByText('$999.99')).toBeInTheDocument();
      expect(screen.getByText('$29.99')).toBeInTheDocument();
    });
  });

  it('should show error message on failure', async () => {
    const error = new Error('Failed to load products');
    (productService.getAllProducts as jest.MockedFunction<typeof productService.getAllProducts>).mockRejectedValue(error);

    renderWithProvider(<ProductList />);

    await waitFor(() => {
      expect(screen.getByText('Failed to load products')).toBeInTheDocument();
      expect(screen.getByText('Retry')).toBeInTheDocument();
    });
  });

  it('should show empty state when no products', async () => {
    (productService.getAllProducts as jest.MockedFunction<typeof productService.getAllProducts>).mockResolvedValue([]);

    renderWithProvider(<ProductList />);

    await waitFor(() => {
      expect(screen.getByText(/No products available/i)).toBeInTheDocument();
    });
  });
});

