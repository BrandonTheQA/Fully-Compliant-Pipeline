/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { render, fireEvent } from '@testing-library/react';
import { screen, waitFor } from '@testing-library/dom';
import { ProductForm } from '../ProductForm';
import { AppProvider } from '../../context/AppContext';
import { productService } from '../../services/productService';
import type { Product } from '../../types';

jest.mock('../../services/productService');

const renderWithProvider = (component: React.ReactElement) => {
  return render(<AppProvider>{component}</AppProvider>);
};

describe('ProductForm', () => {
  const mockProduct: Product = {
    id: 'product-123',
    name: 'Test Product',
    description: 'Test Description',
    price: 29.99,
    quantity: 10,
    category: 'Electronics',
  };

  beforeEach(() => {
    jest.clearAllMocks();
    sessionStorage.clear();
  });

  it('should render product form', () => {
    renderWithProvider(<ProductForm />);
    expect(screen.getByText('Create New Product')).toBeInTheDocument();
    expect(screen.getByLabelText('Product Name')).toBeInTheDocument();
    expect(screen.getByLabelText('Description')).toBeInTheDocument();
    expect(screen.getByLabelText('Price')).toBeInTheDocument();
    expect(screen.getByLabelText('Quantity')).toBeInTheDocument();
    expect(screen.getByLabelText('Category')).toBeInTheDocument();
    expect(screen.getByText('Create Product')).toBeInTheDocument();
  });

  it('should update form fields when user types', () => {
    renderWithProvider(<ProductForm />);

    const nameInput = screen.getByLabelText('Product Name') as HTMLInputElement;
    fireEvent.change(nameInput, { target: { value: 'New Product' } });
    expect(nameInput.value).toBe('New Product');

    const descriptionInput = screen.getByLabelText('Description') as HTMLTextAreaElement;
    fireEvent.change(descriptionInput, { target: { value: 'New Description' } });
    expect(descriptionInput.value).toBe('New Description');

    const priceInput = screen.getByLabelText('Price') as HTMLInputElement;
    fireEvent.change(priceInput, { target: { value: '39.99' } });
    expect(priceInput.value).toBe('39.99');

    const quantityInput = screen.getByLabelText('Quantity') as HTMLInputElement;
    fireEvent.change(quantityInput, { target: { value: '5' } });
    expect(quantityInput.value).toBe('5');

    const categoryInput = screen.getByLabelText('Category') as HTMLInputElement;
    fireEvent.change(categoryInput, { target: { value: 'Books' } });
    expect(categoryInput.value).toBe('Books');
  });

  it('should create product successfully', async () => {
    (productService.createProduct as jest.MockedFunction<typeof productService.createProduct>).mockResolvedValue(mockProduct);

    renderWithProvider(<ProductForm />);

    fireEvent.change(screen.getByLabelText('Product Name'), { target: { value: 'Test Product' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'Test Description' } });
    fireEvent.change(screen.getByLabelText('Price'), { target: { value: '29.99' } });
    fireEvent.change(screen.getByLabelText('Quantity'), { target: { value: '10' } });
    fireEvent.change(screen.getByLabelText('Category'), { target: { value: 'Electronics' } });

    const submitButton = screen.getByText('Create Product');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(productService.createProduct).toHaveBeenCalledWith({
        name: 'Test Product',
        description: 'Test Description',
        price: 29.99,
        quantity: 10,
        category: 'Electronics',
      });
    });

    await waitFor(() => {
      expect(screen.getByText('Product created successfully!')).toBeInTheDocument();
    });
  });

  it('should reset form after successful creation', async () => {
    (productService.createProduct as jest.MockedFunction<typeof productService.createProduct>).mockResolvedValue(mockProduct);

    renderWithProvider(<ProductForm />);

    fireEvent.change(screen.getByLabelText('Product Name'), { target: { value: 'Test Product' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'Test Description' } });
    fireEvent.change(screen.getByLabelText('Price'), { target: { value: '29.99' } });
    fireEvent.change(screen.getByLabelText('Quantity'), { target: { value: '10' } });
    fireEvent.change(screen.getByLabelText('Category'), { target: { value: 'Electronics' } });

    const submitButton = screen.getByText('Create Product');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Product created successfully!')).toBeInTheDocument();
    });

    // Form should be reset
    expect((screen.getByLabelText('Product Name') as HTMLInputElement).value).toBe('');
    expect((screen.getByLabelText('Description') as HTMLTextAreaElement).value).toBe('');
    expect((screen.getByLabelText('Price') as HTMLInputElement).value).toBe('0');
    expect((screen.getByLabelText('Quantity') as HTMLInputElement).value).toBe('0');
    expect((screen.getByLabelText('Category') as HTMLInputElement).value).toBe('');
  });

  it('should show error when product name is empty', async () => {
    renderWithProvider(<ProductForm />);

    // Fill in all fields except name
    const nameInput = screen.getByLabelText('Product Name') as HTMLInputElement;
    const descriptionInput = screen.getByLabelText('Description') as HTMLTextAreaElement;
    const priceInput = screen.getByLabelText('Price') as HTMLInputElement;
    const quantityInput = screen.getByLabelText('Quantity') as HTMLInputElement;
    const categoryInput = screen.getByLabelText('Category') as HTMLInputElement;

    // Set name to empty string after initial render
    fireEvent.change(nameInput, { target: { value: '' } });
    fireEvent.change(descriptionInput, { target: { value: 'Test Description' } });
    fireEvent.change(priceInput, { target: { value: '29.99' } });
    fireEvent.change(quantityInput, { target: { value: '10' } });
    fireEvent.change(categoryInput, { target: { value: 'Electronics' } });

    // Remove required attribute to allow form submission
    nameInput.removeAttribute('required');

    const form = nameInput.closest('form');
    if (form) {
      fireEvent.submit(form);
    }

    await waitFor(() => {
      expect(screen.getByText('Product name is required')).toBeInTheDocument();
    }, { timeout: 3000 });
  });

  it('should show error when description is empty', async () => {
    renderWithProvider(<ProductForm />);

    const nameInput = screen.getByLabelText('Product Name') as HTMLInputElement;
    const descriptionInput = screen.getByLabelText('Description') as HTMLTextAreaElement;
    const priceInput = screen.getByLabelText('Price') as HTMLInputElement;
    const quantityInput = screen.getByLabelText('Quantity') as HTMLInputElement;
    const categoryInput = screen.getByLabelText('Category') as HTMLInputElement;

    fireEvent.change(nameInput, { target: { value: 'Test Product' } });
    fireEvent.change(descriptionInput, { target: { value: '' } });
    fireEvent.change(priceInput, { target: { value: '29.99' } });
    fireEvent.change(quantityInput, { target: { value: '10' } });
    fireEvent.change(categoryInput, { target: { value: 'Electronics' } });

    descriptionInput.removeAttribute('required');

    const form = descriptionInput.closest('form');
    if (form) {
      fireEvent.submit(form);
    }

    await waitFor(() => {
      expect(screen.getByText('Description is required')).toBeInTheDocument();
    }, { timeout: 3000 });
  });

  it('should show error when price is 0 or negative', async () => {
    renderWithProvider(<ProductForm />);

    fireEvent.change(screen.getByLabelText('Product Name'), { target: { value: 'Test Product' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'Test Description' } });
    fireEvent.change(screen.getByLabelText('Price'), { target: { value: '0' } });
    fireEvent.change(screen.getByLabelText('Quantity'), { target: { value: '10' } });
    fireEvent.change(screen.getByLabelText('Category'), { target: { value: 'Electronics' } });

    const submitButton = screen.getByText('Create Product');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Price must be greater than 0')).toBeInTheDocument();
    });
  });

  it('should show error when quantity is negative', async () => {
    renderWithProvider(<ProductForm />);

    const nameInput = screen.getByLabelText('Product Name') as HTMLInputElement;
    const descriptionInput = screen.getByLabelText('Description') as HTMLTextAreaElement;
    const priceInput = screen.getByLabelText('Price') as HTMLInputElement;
    const quantityInput = screen.getByLabelText('Quantity') as HTMLInputElement;
    const categoryInput = screen.getByLabelText('Category') as HTMLInputElement;

    fireEvent.change(nameInput, { target: { value: 'Test Product' } });
    fireEvent.change(descriptionInput, { target: { value: 'Test Description' } });
    fireEvent.change(priceInput, { target: { value: '29.99' } });
    fireEvent.change(quantityInput, { target: { value: '-1' } });
    fireEvent.change(categoryInput, { target: { value: 'Electronics' } });

    const form = quantityInput.closest('form');
    if (form) {
      fireEvent.submit(form);
    }

    await waitFor(() => {
      expect(screen.getByText('Quantity cannot be negative')).toBeInTheDocument();
    }, { timeout: 3000 });
  });

  it('should show error when category is empty', async () => {
    renderWithProvider(<ProductForm />);

    const nameInput = screen.getByLabelText('Product Name') as HTMLInputElement;
    const descriptionInput = screen.getByLabelText('Description') as HTMLTextAreaElement;
    const priceInput = screen.getByLabelText('Price') as HTMLInputElement;
    const quantityInput = screen.getByLabelText('Quantity') as HTMLInputElement;
    const categoryInput = screen.getByLabelText('Category') as HTMLInputElement;

    fireEvent.change(nameInput, { target: { value: 'Test Product' } });
    fireEvent.change(descriptionInput, { target: { value: 'Test Description' } });
    fireEvent.change(priceInput, { target: { value: '29.99' } });
    fireEvent.change(quantityInput, { target: { value: '10' } });
    fireEvent.change(categoryInput, { target: { value: '' } });

    categoryInput.removeAttribute('required');

    const form = categoryInput.closest('form');
    if (form) {
      fireEvent.submit(form);
    }

    await waitFor(() => {
      expect(screen.getByText('Category is required')).toBeInTheDocument();
    }, { timeout: 3000 });
  });

  it('should show error message on API failure', async () => {
    const error = new Error('Failed to create product');
    (productService.createProduct as jest.MockedFunction<typeof productService.createProduct>).mockRejectedValue(error);

    renderWithProvider(<ProductForm />);

    fireEvent.change(screen.getByLabelText('Product Name'), { target: { value: 'Test Product' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'Test Description' } });
    fireEvent.change(screen.getByLabelText('Price'), { target: { value: '29.99' } });
    fireEvent.change(screen.getByLabelText('Quantity'), { target: { value: '10' } });
    fireEvent.change(screen.getByLabelText('Category'), { target: { value: 'Electronics' } });

    const submitButton = screen.getByText('Create Product');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Failed to create product')).toBeInTheDocument();
    });
  });

  it('should show loading state while creating product', async () => {
    (productService.createProduct as jest.MockedFunction<typeof productService.createProduct>).mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve(mockProduct), 100))
    );

    renderWithProvider(<ProductForm />);

    fireEvent.change(screen.getByLabelText('Product Name'), { target: { value: 'Test Product' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'Test Description' } });
    fireEvent.change(screen.getByLabelText('Price'), { target: { value: '29.99' } });
    fireEvent.change(screen.getByLabelText('Quantity'), { target: { value: '10' } });
    fireEvent.change(screen.getByLabelText('Category'), { target: { value: 'Electronics' } });

    const submitButton = screen.getByText('Create Product');
    fireEvent.click(submitButton);

    expect(screen.getByText('Creating...')).toBeInTheDocument();
    expect((submitButton as HTMLButtonElement).disabled).toBe(true);

    await waitFor(() => {
      expect(screen.getByText('Product created successfully!')).toBeInTheDocument();
    });
  });

  it('should handle decimal price values', async () => {
    (productService.createProduct as jest.MockedFunction<typeof productService.createProduct>).mockResolvedValue(mockProduct);

    renderWithProvider(<ProductForm />);

    fireEvent.change(screen.getByLabelText('Product Name'), { target: { value: 'Test Product' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'Test Description' } });
    fireEvent.change(screen.getByLabelText('Price'), { target: { value: '19.99' } });
    fireEvent.change(screen.getByLabelText('Quantity'), { target: { value: '5' } });
    fireEvent.change(screen.getByLabelText('Category'), { target: { value: 'Electronics' } });

    const submitButton = screen.getByText('Create Product');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(productService.createProduct).toHaveBeenCalledWith({
        name: 'Test Product',
        description: 'Test Description',
        price: 19.99,
        quantity: 5,
        category: 'Electronics',
      });
    });
  });

  it('should handle empty string price and quantity as 0', () => {
    renderWithProvider(<ProductForm />);

    const priceInput = screen.getByLabelText('Price') as HTMLInputElement;
    fireEvent.change(priceInput, { target: { value: '' } });
    expect(priceInput.value).toBe('0');

    const quantityInput = screen.getByLabelText('Quantity') as HTMLInputElement;
    fireEvent.change(quantityInput, { target: { value: '' } });
    expect(quantityInput.value).toBe('0');
  });

  it('should disable form fields while loading', async () => {
    (productService.createProduct as jest.MockedFunction<typeof productService.createProduct>).mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve(mockProduct), 100))
    );

    renderWithProvider(<ProductForm />);

    fireEvent.change(screen.getByLabelText('Product Name'), { target: { value: 'Test Product' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'Test Description' } });
    fireEvent.change(screen.getByLabelText('Price'), { target: { value: '29.99' } });
    fireEvent.change(screen.getByLabelText('Quantity'), { target: { value: '10' } });
    fireEvent.change(screen.getByLabelText('Category'), { target: { value: 'Electronics' } });

    const submitButton = screen.getByText('Create Product');
    fireEvent.click(submitButton);

    expect((screen.getByLabelText('Product Name') as HTMLInputElement).disabled).toBe(true);
    expect((screen.getByLabelText('Description') as HTMLTextAreaElement).disabled).toBe(true);
    expect((screen.getByLabelText('Price') as HTMLInputElement).disabled).toBe(true);
    expect((screen.getByLabelText('Quantity') as HTMLInputElement).disabled).toBe(true);
    expect((screen.getByLabelText('Category') as HTMLInputElement).disabled).toBe(true);
  });
});

