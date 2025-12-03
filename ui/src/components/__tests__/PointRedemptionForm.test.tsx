/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect, jest } from '@jest/globals';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { PointRedemptionForm } from '../PointRedemptionForm';
import type { RedeemPointsResponse } from '../../types';

describe('PointRedemptionForm', () => {
  const mockOnRedemptionSuccess = jest.fn();
  const mockOnError = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render point redemption form', () => {
    render(
      <PointRedemptionForm
        userId="user-123"
        currentBalance={1000}
        onRedemptionSuccess={mockOnRedemptionSuccess}
      />
    );

    expect(screen.getByText('Redeem Points')).toBeInTheDocument();
    expect(screen.getByText(/Available: 1,000 points/i)).toBeInTheDocument();
  });

  it('should display available balance', () => {
    render(
      <PointRedemptionForm
        userId="user-123"
        currentBalance={2500}
        onRedemptionSuccess={mockOnRedemptionSuccess}
      />
    );

    expect(screen.getByText(/Available: 2,500 points/i)).toBeInTheDocument();
  });

  it('should allow user to enter points to redeem', () => {
    render(
      <PointRedemptionForm
        userId="user-123"
        currentBalance={1000}
        onRedemptionSuccess={mockOnRedemptionSuccess}
      />
    );

    const pointsInput = screen.getByLabelText(/Points to redeem:/i);
    fireEvent.change(pointsInput, { target: { value: '500' } });

    expect(pointsInput).toHaveValue(500);
  });

  it('should calculate discount amount', async () => {
    render(
      <PointRedemptionForm
        userId="user-123"
        currentBalance={1000}
        orderTotal={100}
        onRedemptionSuccess={mockOnRedemptionSuccess}
      />
    );

    const pointsInput = screen.getByLabelText(/Points to redeem:/i);
    fireEvent.change(pointsInput, { target: { value: '500' } });

    await waitFor(() => {
      expect(screen.getByText(/Discount: \$5.00/i)).toBeInTheDocument();
    });
  });

  it('should apply 50% max discount limit', async () => {
    render(
      <PointRedemptionForm
        userId="user-123"
        currentBalance={10000}
        orderTotal={100}
        onRedemptionSuccess={mockOnRedemptionSuccess}
      />
    );

    const pointsInput = screen.getByLabelText(/Points to redeem:/i);
    // 10000 points = $100, but max is 50% of $100 = $50
    fireEvent.change(pointsInput, { target: { value: '10000' } });

    await waitFor(() => {
      expect(screen.getByText(/Discount: \$50.00/i)).toBeInTheDocument();
    });
  });

  it('should display error when points below minimum', () => {
    render(
      <PointRedemptionForm
        userId="user-123"
        currentBalance={1000}
        onRedemptionSuccess={mockOnRedemptionSuccess}
        onError={mockOnError}
      />
    );

    const pointsInput = screen.getByLabelText(/Points to redeem:/i);
    fireEvent.change(pointsInput, { target: { value: '400' } });

    const redeemButton = screen.getByText('Apply Points Discount');
    fireEvent.click(redeemButton);

    expect(screen.getByText(/Minimum redemption is 500 points/i)).toBeInTheDocument();
  });

  it('should display error when points exceed balance', () => {
    render(
      <PointRedemptionForm
        userId="user-123"
        currentBalance={1000}
        onRedemptionSuccess={mockOnRedemptionSuccess}
        onError={mockOnError}
      />
    );

    const pointsInput = screen.getByLabelText(/Points to redeem:/i);
    fireEvent.change(pointsInput, { target: { value: '1500' } });

    const redeemButton = screen.getByText('Apply Points Discount');
    fireEvent.click(redeemButton);

    expect(screen.getByText('Insufficient points')).toBeInTheDocument();
  });

  it('should call onRedemptionSuccess when redemption is valid', () => {
    render(
      <PointRedemptionForm
        userId="user-123"
        currentBalance={1000}
        orderTotal={100}
        onRedemptionSuccess={mockOnRedemptionSuccess}
      />
    );

    const pointsInput = screen.getByLabelText(/Points to redeem:/i);
    fireEvent.change(pointsInput, { target: { value: '500' } });

    const redeemButton = screen.getByText('Apply Points Discount');
    fireEvent.click(redeemButton);

    expect(mockOnRedemptionSuccess).toHaveBeenCalledWith(
      expect.objectContaining({
        pointsRedeemed: 500,
        discountAmount: 5,
        remainingBalance: 500,
      })
    );
  });

  it('should disable redeem button when points below minimum', () => {
    render(
      <PointRedemptionForm
        userId="user-123"
        currentBalance={1000}
        onRedemptionSuccess={mockOnRedemptionSuccess}
      />
    );

    const pointsInput = screen.getByLabelText(/Points to redeem:/i);
    fireEvent.change(pointsInput, { target: { value: '400' } });

    const redeemButton = screen.getByText('Apply Points Discount');
    expect((redeemButton as HTMLButtonElement).disabled).toBe(true);
  });

  it('should disable redeem button when points exceed balance', () => {
    render(
      <PointRedemptionForm
        userId="user-123"
        currentBalance={1000}
        onRedemptionSuccess={mockOnRedemptionSuccess}
      />
    );

    const pointsInput = screen.getByLabelText(/Points to redeem:/i);
    fireEvent.change(pointsInput, { target: { value: '1500' } });

    const redeemButton = screen.getByText('Apply Points Discount');
    expect((redeemButton as HTMLButtonElement).disabled).toBe(true);
  });

  it('should handle quick redeem buttons', () => {
    render(
      <PointRedemptionForm
        userId="user-123"
        currentBalance={2000}
        onRedemptionSuccess={mockOnRedemptionSuccess}
      />
    );

    const quick500Button = screen.getByText('500 pts');
    fireEvent.click(quick500Button);

    const pointsInput = screen.getByLabelText(/Points to redeem:/i);
    expect(pointsInput).toHaveValue(500);
  });

  it('should handle quick redeem 1000 points', () => {
    render(
      <PointRedemptionForm
        userId="user-123"
        currentBalance={2000}
        onRedemptionSuccess={mockOnRedemptionSuccess}
      />
    );

    const quick1000Button = screen.getByText('1,000 pts');
    fireEvent.click(quick1000Button);

    const pointsInput = screen.getByLabelText(/Points to redeem:/i);
    expect((pointsInput as HTMLInputElement).value).toBe('1000');
  });

  it('should handle max quick redeem', () => {
    render(
      <PointRedemptionForm
        userId="user-123"
        currentBalance={2500}
        onRedemptionSuccess={mockOnRedemptionSuccess}
      />
    );

    const maxButton = screen.getByText('Max');
    fireEvent.click(maxButton);

    const pointsInput = screen.getByLabelText(/Points to redeem:/i);
    // Max should be rounded down to nearest 100
    expect((pointsInput as HTMLInputElement).value).toBe('2400');
  });

  it('should disable quick redeem buttons when balance is insufficient', () => {
    render(
      <PointRedemptionForm
        userId="user-123"
        currentBalance={400}
        onRedemptionSuccess={mockOnRedemptionSuccess}
      />
    );

    const quick500Button = screen.getByText('500 pts');
    const quick1000Button = screen.getByText('1,000 pts');
    const maxButton = screen.getByText('Max');

    expect((quick500Button as HTMLButtonElement).disabled).toBe(true);
    expect((quick1000Button as HTMLButtonElement).disabled).toBe(true);
    expect((maxButton as HTMLButtonElement).disabled).toBe(true);
  });

  it('should not display discount when points are invalid', () => {
    render(
      <PointRedemptionForm
        userId="user-123"
        currentBalance={1000}
        onRedemptionSuccess={mockOnRedemptionSuccess}
      />
    );

    const pointsInput = screen.getByLabelText(/Points to redeem:/i);
    fireEvent.change(pointsInput, { target: { value: '400' } });

    expect(screen.queryByText(/Discount:/i)).not.toBeInTheDocument();
  });

  it('should clear discount when points are cleared', async () => {
    render(
      <PointRedemptionForm
        userId="user-123"
        currentBalance={1000}
        onRedemptionSuccess={mockOnRedemptionSuccess}
      />
    );

    const pointsInput = screen.getByLabelText(/Points to redeem:/i);
    fireEvent.change(pointsInput, { target: { value: '500' } });

    await waitFor(() => {
      expect(screen.getByText(/Discount:/i)).toBeInTheDocument();
    });

    fireEvent.change(pointsInput, { target: { value: '' } });

    await waitFor(() => {
      expect(screen.queryByText(/Discount:/i)).not.toBeInTheDocument();
    });
  });
});
