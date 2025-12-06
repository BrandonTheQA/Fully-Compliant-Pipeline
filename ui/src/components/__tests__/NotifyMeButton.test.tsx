/// <reference types="@testing-library/jest-dom" />
import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { NotifyMeButton } from '../NotifyMeButton';
import { stockService } from '../../services/stockService';

jest.mock('../../services/stockService');

describe('NotifyMeButton', () => {
  const mockStockService = stockService as jest.Mocked<typeof stockService>;

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render notify me button', () => {
    render(<NotifyMeButton productId="product-1" productName="Test Product" />);

    expect(screen.getByText('Notify Me When Available')).toBeInTheDocument();
  });

  it('should open modal when button is clicked', () => {
    render(<NotifyMeButton productId="product-1" productName="Test Product" />);

    const buttons = screen.getAllByText('Notify Me When Available');
    const button = buttons[0]; // Get the first button (the one that opens the modal)
    fireEvent.click(button);

    expect(screen.getByText(/We'll notify you when/i)).toBeInTheDocument();
    expect(screen.getByText(/Test Product/i)).toBeInTheDocument();
    expect(screen.getByText(/is back in stock/i)).toBeInTheDocument();
  });

  it('should close modal when close button is clicked', () => {
    render(<NotifyMeButton productId="product-1" productName="Test Product" />);

    const buttons = screen.getAllByText('Notify Me When Available');
    const button = buttons[0];
    fireEvent.click(button);

    const closeButton = screen.getByLabelText('Close');
    fireEvent.click(closeButton);

    expect(screen.queryByText(/We'll notify you when Test Product is back in stock/)).not.toBeInTheDocument();
  });

  it('should submit notification signup successfully', async () => {
    mockStockService.signUpForNotification.mockResolvedValue(undefined);

    render(<NotifyMeButton productId="product-1" productName="Test Product" />);

    const buttons = screen.getAllByText('Notify Me When Available');
    const button = buttons[0];
    fireEvent.click(button);

    const emailInput = screen.getByLabelText('Email Address');
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });

    const submitButton = screen.getByText('Notify Me');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockStockService.signUpForNotification).toHaveBeenCalledWith('product-1', 'test@example.com');
    });

    await waitFor(() => {
      expect(screen.getByText(/Successfully signed up!/)).toBeInTheDocument();
    });
  });

  it('should handle signup errors', async () => {
    const error = new Error('Failed to sign up');
    mockStockService.signUpForNotification.mockRejectedValue(error);

    render(<NotifyMeButton productId="product-1" productName="Test Product" />);

    const buttons = screen.getAllByText('Notify Me When Available');
    const button = buttons[0];
    fireEvent.click(button);

    const emailInput = screen.getByLabelText('Email Address');
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });

    const submitButton = screen.getByText('Notify Me');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Failed to sign up')).toBeInTheDocument();
    });
  });

  it('should validate email input', async () => {
    render(<NotifyMeButton productId="product-1" productName="Test Product" />);

    const buttons = screen.getAllByText('Notify Me When Available');
    const button = buttons[0];
    fireEvent.click(button);

    const submitButton = screen.getByText('Notify Me') as HTMLButtonElement;
    expect(submitButton.disabled).toBe(true);

    const emailInput = screen.getByLabelText('Email Address');
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });

    expect(submitButton.disabled).toBe(false);
  });
});

