/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { ReturnPolicyPage } from '../ReturnPolicyPage';
import { returnService } from '../../services/returnService';
import type { ReturnPolicy } from '../../types';

jest.mock('../../services/returnService');

const renderWithRouter = (component: React.ReactElement) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe('ReturnPolicyPage', () => {
  const mockPolicy: ReturnPolicy = {
    returnWindowDays: 30,
    restockingFeePercentage: 10,
    freeReturnThreshold: 50.00,
    autoApproveThreshold: 25.00,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render return policy page header', async () => {
    (returnService.getReturnPolicy as jest.MockedFunction<typeof returnService.getReturnPolicy>).mockResolvedValue(mockPolicy);

    renderWithRouter(<ReturnPolicyPage />);

    await waitFor(() => {
      expect(screen.getByText('Return Policy')).toBeInTheDocument();
    });
  });

  it('should load return policy on mount', async () => {
    (returnService.getReturnPolicy as jest.MockedFunction<typeof returnService.getReturnPolicy>).mockResolvedValue(mockPolicy);

    renderWithRouter(<ReturnPolicyPage />);

    await waitFor(() => {
      expect(returnService.getReturnPolicy).toHaveBeenCalled();
    });

    await waitFor(() => {
      expect(screen.getAllByText(/You have/i).length).toBeGreaterThan(0);
      expect(screen.getAllByText(/30 days/i).length).toBeGreaterThan(0);
    });
  });

  it('should display loading state', async () => {
    (returnService.getReturnPolicy as jest.MockedFunction<typeof returnService.getReturnPolicy>).mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve(mockPolicy), 100))
    );

    renderWithRouter(<ReturnPolicyPage />);

    expect(screen.getByText('Loading return policy...')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.queryByText('Loading return policy...')).not.toBeInTheDocument();
    });
  });

  it('should display error message when loading fails', async () => {
    const error = new Error('Failed to load return policy');
    (returnService.getReturnPolicy as jest.MockedFunction<typeof returnService.getReturnPolicy>).mockRejectedValue(error);

    renderWithRouter(<ReturnPolicyPage />);

    await waitFor(() => {
      expect(screen.getByText('Failed to load return policy')).toBeInTheDocument();
    });
  });

  it('should display return window information', async () => {
    (returnService.getReturnPolicy as jest.MockedFunction<typeof returnService.getReturnPolicy>).mockResolvedValue(mockPolicy);

    renderWithRouter(<ReturnPolicyPage />);

    await waitFor(() => {
      expect(screen.getByText('Return Window')).toBeInTheDocument();
      expect(screen.getAllByText(/You have/i).length).toBeGreaterThan(0);
      expect(screen.getAllByText(/30 days/i).length).toBeGreaterThan(0);
      expect(screen.getAllByText(/from the delivery date to initiate a return/i).length).toBeGreaterThan(0);
    });
  });

  it('should display eligible items section', async () => {
    (returnService.getReturnPolicy as jest.MockedFunction<typeof returnService.getReturnPolicy>).mockResolvedValue(mockPolicy);

    renderWithRouter(<ReturnPolicyPage />);

    await waitFor(() => {
      expect(screen.getByText('Eligible Items')).toBeInTheDocument();
      expect(screen.getByText(/Most items are eligible for return, except:/i)).toBeInTheDocument();
      expect(screen.getByText(/Personalized or customized items/i)).toBeInTheDocument();
      expect(screen.getByText(/Items that have been used or damaged by the customer/i)).toBeInTheDocument();
      expect(screen.getByText(/Items returned without original packaging/i)).toBeInTheDocument();
    });
  });

  it('should display return methods section', async () => {
    (returnService.getReturnPolicy as jest.MockedFunction<typeof returnService.getReturnPolicy>).mockResolvedValue(mockPolicy);

    renderWithRouter(<ReturnPolicyPage />);

    await waitFor(() => {
      expect(screen.getByText('Return Methods')).toBeInTheDocument();
      expect(screen.getByText(/You can return items using:/i)).toBeInTheDocument();
      expect(screen.getByText(/Prepaid return shipping label/i)).toBeInTheDocument();
      expect(screen.getByText(/Customer-paid return shipping/i)).toBeInTheDocument();
    });
  });

  it('should display free return threshold when available', async () => {
    (returnService.getReturnPolicy as jest.MockedFunction<typeof returnService.getReturnPolicy>).mockResolvedValue(mockPolicy);

    renderWithRouter(<ReturnPolicyPage />);

    await waitFor(() => {
      expect(screen.getByText(/Orders over \$50.00 qualify for free return shipping/i)).toBeInTheDocument();
    });
  });

  it('should not display free return threshold when not available', async () => {
    const policyWithoutThreshold: ReturnPolicy = {
      ...mockPolicy,
      freeReturnThreshold: undefined,
    };

    (returnService.getReturnPolicy as jest.MockedFunction<typeof returnService.getReturnPolicy>).mockResolvedValue(policyWithoutThreshold);

    renderWithRouter(<ReturnPolicyPage />);

    await waitFor(() => {
      expect(screen.queryByText(/Orders over/i)).not.toBeInTheDocument();
    });
  });

  it('should display refund processing section', async () => {
    (returnService.getReturnPolicy as jest.MockedFunction<typeof returnService.getReturnPolicy>).mockResolvedValue(mockPolicy);

    renderWithRouter(<ReturnPolicyPage />);

    await waitFor(() => {
      expect(screen.getByText('Refund Processing')).toBeInTheDocument();
      expect(screen.getByText(/Refunds are typically processed within/i)).toBeInTheDocument();
      expect(screen.getByText(/1-3 business days/i)).toBeInTheDocument();
    });
  });

  it('should display restocking fees section when available', async () => {
    (returnService.getReturnPolicy as jest.MockedFunction<typeof returnService.getReturnPolicy>).mockResolvedValue(mockPolicy);

    renderWithRouter(<ReturnPolicyPage />);

    await waitFor(() => {
      expect(screen.getByText('Restocking Fees')).toBeInTheDocument();
      expect(screen.getByText(/A restocking fee of/i)).toBeInTheDocument();
      expect(screen.getByText(/10%/i)).toBeInTheDocument();
    });
  });

  it('should not display restocking fees section when not available', async () => {
    const policyWithoutFee: ReturnPolicy = {
      ...mockPolicy,
      restockingFeePercentage: undefined,
    };

    (returnService.getReturnPolicy as jest.MockedFunction<typeof returnService.getReturnPolicy>).mockResolvedValue(policyWithoutFee);

    renderWithRouter(<ReturnPolicyPage />);

    await waitFor(() => {
      expect(screen.queryByText('Restocking Fees')).not.toBeInTheDocument();
    });
  });

  it('should display return types section', async () => {
    (returnService.getReturnPolicy as jest.MockedFunction<typeof returnService.getReturnPolicy>).mockResolvedValue(mockPolicy);

    renderWithRouter(<ReturnPolicyPage />);

    await waitFor(() => {
      expect(screen.getByText('Return Types')).toBeInTheDocument();
      expect(screen.getByText(/Refund to Original Payment:/i)).toBeInTheDocument();
      expect(screen.getByText(/Store Credit:/i)).toBeInTheDocument();
      expect(screen.getByText(/Exchange:/i)).toBeInTheDocument();
    });
  });

  it('should display how to return section', async () => {
    (returnService.getReturnPolicy as jest.MockedFunction<typeof returnService.getReturnPolicy>).mockResolvedValue(mockPolicy);

    renderWithRouter(<ReturnPolicyPage />);

    await waitFor(() => {
      expect(screen.getByText('How to Return')).toBeInTheDocument();
      expect(screen.getByText(/Log in to your account and go to/i)).toBeInTheDocument();
      expect(screen.getByText(/Select the order and items you want to return/i)).toBeInTheDocument();
    });
  });

  it('should display questions section', async () => {
    (returnService.getReturnPolicy as jest.MockedFunction<typeof returnService.getReturnPolicy>).mockResolvedValue(mockPolicy);

    renderWithRouter(<ReturnPolicyPage />);

    await waitFor(() => {
      expect(screen.getByText('Questions?')).toBeInTheDocument();
      expect(screen.getByText(/If you have any questions about our return policy/i)).toBeInTheDocument();
    });
  });

  it('should handle zero restocking fee', async () => {
    const policyWithZeroFee: ReturnPolicy = {
      ...mockPolicy,
      restockingFeePercentage: 0,
    };

    (returnService.getReturnPolicy as jest.MockedFunction<typeof returnService.getReturnPolicy>).mockResolvedValue(policyWithZeroFee);

    renderWithRouter(<ReturnPolicyPage />);

    await waitFor(() => {
      expect(screen.queryByText('Restocking Fees')).not.toBeInTheDocument();
    });
  });

  it('should format free return threshold correctly', async () => {
    const policyWithDecimal: ReturnPolicy = {
      ...mockPolicy,
      freeReturnThreshold: 49.99,
    };

    (returnService.getReturnPolicy as jest.MockedFunction<typeof returnService.getReturnPolicy>).mockResolvedValue(policyWithDecimal);

    renderWithRouter(<ReturnPolicyPage />);

    await waitFor(() => {
      expect(screen.getByText(/Orders over \$49.99 qualify for free return shipping/i)).toBeInTheDocument();
    });
  });
});

