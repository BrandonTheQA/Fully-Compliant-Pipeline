/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { render, fireEvent, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AdminReturnsPage } from '../AdminReturnsPage';
import { returnService } from '../../services/returnService';
import type { Return, ReturnAnalytics, ReturnStatus } from '../../types';

jest.mock('../../services/returnService');

// Mock window.prompt
const mockPrompt = jest.fn();
Object.defineProperty(window, 'prompt', {
  writable: true,
  value: mockPrompt,
});

const renderWithRouter = (component: React.ReactElement) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe('AdminReturnsPage', () => {
  const mockReturn: Return = {
    returnId: 'return-1',
    orderId: 'order-1',
    userId: 'user-1',
    rmaNumber: 'RMA-20240101-00001',
    status: 'PENDING_APPROVAL',
    returnType: 'REFUND_TO_PAYMENT',
    refundAmount: 29.99,
    items: [
      {
        orderItemId: 1,
        productId: 'product-1',
        productName: 'Product 1',
        quantity: 1,
        returnReason: 'DEFECTIVE',
        originalPrice: 29.99,
        refundAmount: 29.99,
      },
    ],
    statusHistory: [
      {
        status: 'PENDING_APPROVAL',
        createdAt: '2024-01-01T10:00:00Z',
      },
    ],
    createdAt: '2024-01-01T10:00:00Z',
    updatedAt: '2024-01-01T10:00:00Z',
  };

  const mockReturns: Return[] = [mockReturn];

  const mockAnalytics: ReturnAnalytics = {
    totalReturns: 100,
    totalReturnValue: 5000.00,
    averageReturnProcessingTime: 5.5,
    returnRate: 0.1,
    returnReasonsDistribution: {
      DEFECTIVE: 30,
      WRONG_ITEM: 20,
      CHANGED_MIND: 50,
    },
    returnRateByProduct: [],
    returnsByStatus: {
      PENDING_APPROVAL: 10,
      APPROVED: 20,
      REFUNDED: 70,
    },
    returnsByMonth: [],
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockPrompt.mockClear();
  });

  it('should render admin returns page header', () => {
    renderWithRouter(<AdminReturnsPage />);

    expect(screen.getByText('Admin - Return Management')).toBeInTheDocument();
    expect(screen.getByText('Return Queue')).toBeInTheDocument();
    expect(screen.getByText('Analytics')).toBeInTheDocument();
  });

  it('should load returns on mount', async () => {
    (returnService.getAdminReturns as jest.MockedFunction<typeof returnService.getAdminReturns>).mockResolvedValue(mockReturns);

    renderWithRouter(<AdminReturnsPage />);

    await waitFor(() => {
      expect(returnService.getAdminReturns).toHaveBeenCalled();
    });

    await waitFor(() => {
      // Component displays "RMA: RMA-20240101-00001"
      expect(screen.getByText(/RMA: RMA-20240101-00001/i)).toBeInTheDocument();
    });
  });

  it('should display loading state', async () => {
    (returnService.getAdminReturns as jest.MockedFunction<typeof returnService.getAdminReturns>).mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve(mockReturns), 100))
    );

    renderWithRouter(<AdminReturnsPage />);

    expect(screen.getByText('Loading returns...')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.queryByText('Loading returns...')).not.toBeInTheDocument();
    });
  });

  it('should display error message when loading fails', async () => {
    const error = new Error('Failed to load returns');
    (returnService.getAdminReturns as jest.MockedFunction<typeof returnService.getAdminReturns>).mockRejectedValue(error);

    renderWithRouter(<AdminReturnsPage />);

    await waitFor(() => {
      expect(screen.getByText('Failed to load returns')).toBeInTheDocument();
    });
  });

  it('should display empty state when no returns', async () => {
    (returnService.getAdminReturns as jest.MockedFunction<typeof returnService.getAdminReturns>).mockResolvedValue([]);

    renderWithRouter(<AdminReturnsPage />);

    await waitFor(() => {
      expect(screen.getByText('No returns found')).toBeInTheDocument();
    });
  });

  it('should filter returns by status', async () => {
    (returnService.getAdminReturns as jest.MockedFunction<typeof returnService.getAdminReturns>).mockResolvedValue(mockReturns);

    renderWithRouter(<AdminReturnsPage />);

    await waitFor(() => {
      // Component displays "RMA: RMA-20240101-00001"
      expect(screen.getByText(/RMA: RMA-20240101-00001/i)).toBeInTheDocument();
    });

    const statusSelect = screen.getByLabelText(/Status:/i) as HTMLSelectElement;
    fireEvent.change(statusSelect, { target: { value: 'APPROVED' } });

    await waitFor(() => {
      expect(returnService.getAdminReturns).toHaveBeenCalledWith(
        expect.objectContaining({ status: 'APPROVED' })
      );
    });
  });

  it('should filter returns by user ID', async () => {
    (returnService.getAdminReturns as jest.MockedFunction<typeof returnService.getAdminReturns>).mockResolvedValue(mockReturns);

    renderWithRouter(<AdminReturnsPage />);

    await waitFor(() => {
      // Component displays "RMA: RMA-20240101-00001"
      expect(screen.getByText(/RMA: RMA-20240101-00001/i)).toBeInTheDocument();
    });

    const userIdInput = screen.getByPlaceholderText('Filter by user ID') as HTMLInputElement;
    fireEvent.change(userIdInput, { target: { value: 'user-1' } });

    await waitFor(() => {
      expect(returnService.getAdminReturns).toHaveBeenCalledWith(
        expect.objectContaining({ userId: 'user-1' })
      );
    });
  });

  it('should filter returns by order ID', async () => {
    (returnService.getAdminReturns as jest.MockedFunction<typeof returnService.getAdminReturns>).mockResolvedValue(mockReturns);

    renderWithRouter(<AdminReturnsPage />);

    await waitFor(() => {
      // Component displays "RMA: RMA-20240101-00001"
      expect(screen.getByText(/RMA: RMA-20240101-00001/i)).toBeInTheDocument();
    });

    const orderIdInput = screen.getByPlaceholderText('Filter by order ID') as HTMLInputElement;
    fireEvent.change(orderIdInput, { target: { value: 'order-1' } });

    await waitFor(() => {
      expect(returnService.getAdminReturns).toHaveBeenCalledWith(
        expect.objectContaining({ orderId: 'order-1' })
      );
    });
  });

  it('should filter returns by RMA number', async () => {
    (returnService.getAdminReturns as jest.MockedFunction<typeof returnService.getAdminReturns>).mockResolvedValue(mockReturns);

    renderWithRouter(<AdminReturnsPage />);

    await waitFor(() => {
      // Component displays "RMA: RMA-20240101-00001"
      expect(screen.getByText(/RMA: RMA-20240101-00001/i)).toBeInTheDocument();
    });

    const rmaInput = screen.getByPlaceholderText('Search by RMA') as HTMLInputElement;
    fireEvent.change(rmaInput, { target: { value: 'RMA-20240101-00001' } });

    await waitFor(() => {
      expect(returnService.getAdminReturns).toHaveBeenCalledWith(
        expect.objectContaining({ rmaNumber: 'RMA-20240101-00001' })
      );
    });
  });

  it('should view return details', async () => {
    (returnService.getAdminReturns as jest.MockedFunction<typeof returnService.getAdminReturns>).mockResolvedValue(mockReturns);
    (returnService.getAdminReturn as jest.MockedFunction<typeof returnService.getAdminReturn>).mockResolvedValue(mockReturn);

    renderWithRouter(<AdminReturnsPage />);

    await waitFor(() => {
      // Component displays "RMA: RMA-20240101-00001"
      expect(screen.getByText(/RMA: RMA-20240101-00001/i)).toBeInTheDocument();
    });

    const viewButton = screen.getByText('View Details');
    fireEvent.click(viewButton);

    await waitFor(() => {
      expect(returnService.getAdminReturn).toHaveBeenCalledWith('return-1');
    });

    await waitFor(() => {
      expect(screen.getByText('Return Details: RMA-20240101-00001')).toBeInTheDocument();
      expect(screen.getByText('← Back to Queue')).toBeInTheDocument();
    });
  });

  it('should switch to analytics view', async () => {
    (returnService.getAnalytics as jest.MockedFunction<typeof returnService.getAnalytics>).mockResolvedValue(mockAnalytics);

    renderWithRouter(<AdminReturnsPage />);

    const analyticsButton = screen.getByText('Analytics');
    fireEvent.click(analyticsButton);

    await waitFor(() => {
      expect(returnService.getAnalytics).toHaveBeenCalled();
    });

    await waitFor(() => {
      expect(screen.getByText('Return Analytics')).toBeInTheDocument();
      expect(screen.getByText('100')).toBeInTheDocument(); // totalReturns
    });
  });

  it('should approve return', async () => {
    (returnService.getAdminReturns as jest.MockedFunction<typeof returnService.getAdminReturns>).mockResolvedValue(mockReturns);
    (returnService.getAdminReturn as jest.MockedFunction<typeof returnService.getAdminReturn>).mockResolvedValue(mockReturn);
    (returnService.approveReturn as jest.MockedFunction<typeof returnService.approveReturn>).mockResolvedValue(undefined);

    mockPrompt.mockReturnValue('Approval notes');

    renderWithRouter(<AdminReturnsPage />);

    await waitFor(() => {
      expect(screen.getByText('View Details')).toBeInTheDocument();
    });

    const viewButton = screen.getByText('View Details');
    fireEvent.click(viewButton);

    await waitFor(() => {
      expect(screen.getByText('Approve Return')).toBeInTheDocument();
    });

    const approveButton = screen.getByText('Approve Return');
    fireEvent.click(approveButton);

    await waitFor(() => {
      expect(returnService.approveReturn).toHaveBeenCalledWith('return-1', 'Approval notes');
    });
  });

  it('should reject return', async () => {
    (returnService.getAdminReturns as jest.MockedFunction<typeof returnService.getAdminReturns>).mockResolvedValue(mockReturns);
    (returnService.getAdminReturn as jest.MockedFunction<typeof returnService.getAdminReturn>).mockResolvedValue(mockReturn);
    (returnService.rejectReturn as jest.MockedFunction<typeof returnService.rejectReturn>).mockResolvedValue(undefined);

    mockPrompt.mockReturnValue('Rejection reason');

    renderWithRouter(<AdminReturnsPage />);

    await waitFor(() => {
      expect(screen.getByText('View Details')).toBeInTheDocument();
    });

    const viewButton = screen.getByText('View Details');
    fireEvent.click(viewButton);

    await waitFor(() => {
      expect(screen.getByText('Reject Return')).toBeInTheDocument();
    });

    const rejectButton = screen.getByText('Reject Return');
    fireEvent.click(rejectButton);

    await waitFor(() => {
      expect(returnService.rejectReturn).toHaveBeenCalledWith('return-1', 'Rejection reason');
    });
  });

  it('should not reject return when reason is empty', async () => {
    (returnService.getAdminReturns as jest.MockedFunction<typeof returnService.getAdminReturns>).mockResolvedValue(mockReturns);
    (returnService.getAdminReturn as jest.MockedFunction<typeof returnService.getAdminReturn>).mockResolvedValue(mockReturn);

    mockPrompt.mockReturnValue('');

    renderWithRouter(<AdminReturnsPage />);

    await waitFor(() => {
      expect(screen.getByText('View Details')).toBeInTheDocument();
    });

    const viewButton = screen.getByText('View Details');
    fireEvent.click(viewButton);

    await waitFor(() => {
      expect(screen.getByText('Reject Return')).toBeInTheDocument();
    });

    const rejectButton = screen.getByText('Reject Return');
    fireEvent.click(rejectButton);

    // Component checks if (reason) before calling handleReject, so when prompt returns '',
    // handleReject is never called, and rejectReturn service is not called
    // This test verifies that empty reason doesn't trigger rejection
    await waitFor(() => {
      expect(returnService.rejectReturn).not.toHaveBeenCalled();
    });
  });

  it('should mark return as received', async () => {
    const approvedReturn: Return = {
      ...mockReturn,
      status: 'APPROVED',
    };

    (returnService.getAdminReturns as jest.MockedFunction<typeof returnService.getAdminReturns>).mockResolvedValue([approvedReturn]);
    (returnService.getAdminReturn as jest.MockedFunction<typeof returnService.getAdminReturn>).mockResolvedValue(approvedReturn);
    (returnService.markReturnReceived as jest.MockedFunction<typeof returnService.markReturnReceived>).mockResolvedValue(undefined);

    mockPrompt.mockReturnValue('Received notes');

    renderWithRouter(<AdminReturnsPage />);

    await waitFor(() => {
      expect(screen.getByText('View Details')).toBeInTheDocument();
    });

    const viewButton = screen.getByText('View Details');
    fireEvent.click(viewButton);

    await waitFor(() => {
      expect(screen.getByText('Mark as Received')).toBeInTheDocument();
    });

    const markReceivedButton = screen.getByText('Mark as Received');
    fireEvent.click(markReceivedButton);

    await waitFor(() => {
      expect(returnService.markReturnReceived).toHaveBeenCalledWith('return-1', 'Received notes');
    });
  });

  it('should process refund', async () => {
    const receivedReturn: Return = {
      ...mockReturn,
      status: 'RECEIVED',
    };

    (returnService.getAdminReturns as jest.MockedFunction<typeof returnService.getAdminReturns>).mockResolvedValue([receivedReturn]);
    (returnService.getAdminReturn as jest.MockedFunction<typeof returnService.getAdminReturn>).mockResolvedValue(receivedReturn);
    (returnService.processRefund as jest.MockedFunction<typeof returnService.processRefund>).mockResolvedValue(undefined);

    renderWithRouter(<AdminReturnsPage />);

    await waitFor(() => {
      expect(screen.getByText('View Details')).toBeInTheDocument();
    });

    const viewButton = screen.getByText('View Details');
    fireEvent.click(viewButton);

    await waitFor(() => {
      expect(screen.getByText('Process Refund')).toBeInTheDocument();
    });

    const processRefundButton = screen.getByText('Process Refund');
    fireEvent.click(processRefundButton);

    await waitFor(() => {
      expect(returnService.processRefund).toHaveBeenCalledWith('return-1');
    });
  });

  it('should update return status', async () => {
    (returnService.getAdminReturns as jest.MockedFunction<typeof returnService.getAdminReturns>).mockResolvedValue(mockReturns);
    (returnService.getAdminReturn as jest.MockedFunction<typeof returnService.getAdminReturn>).mockResolvedValue(mockReturn);
    (returnService.updateReturnStatus as jest.MockedFunction<typeof returnService.updateReturnStatus>).mockResolvedValue(undefined);

    mockPrompt.mockReturnValue('Status update notes');

    renderWithRouter(<AdminReturnsPage />);

    await waitFor(() => {
      expect(screen.getByText('View Details')).toBeInTheDocument();
    });

    const viewButton = screen.getByText('View Details');
    fireEvent.click(viewButton);

    await waitFor(() => {
      expect(screen.getByLabelText(/Update Status:/i)).toBeInTheDocument();
    });

    const statusSelect = screen.getByLabelText(/Update Status:/i) as HTMLSelectElement;
    fireEvent.change(statusSelect, { target: { value: 'IN_TRANSIT' } });

    await waitFor(() => {
      expect(returnService.updateReturnStatus).toHaveBeenCalledWith('return-1', 'IN_TRANSIT', 'Status update notes');
    });
  });

  it('should go back to queue from details view', async () => {
    (returnService.getAdminReturns as jest.MockedFunction<typeof returnService.getAdminReturns>).mockResolvedValue(mockReturns);
    (returnService.getAdminReturn as jest.MockedFunction<typeof returnService.getAdminReturn>).mockResolvedValue(mockReturn);

    renderWithRouter(<AdminReturnsPage />);

    await waitFor(() => {
      expect(screen.getByText('View Details')).toBeInTheDocument();
    });

    const viewButton = screen.getByText('View Details');
    fireEvent.click(viewButton);

    await waitFor(() => {
      expect(screen.getByText('← Back to Queue')).toBeInTheDocument();
    });

    const backButton = screen.getByText('← Back to Queue');
    fireEvent.click(backButton);

    await waitFor(() => {
      expect(screen.getByText('View Details')).toBeInTheDocument();
    });
  });

  it('should display analytics data', async () => {
    (returnService.getAnalytics as jest.MockedFunction<typeof returnService.getAnalytics>).mockResolvedValue(mockAnalytics);

    renderWithRouter(<AdminReturnsPage />);

    const analyticsButton = screen.getByText('Analytics');
    fireEvent.click(analyticsButton);

    await waitFor(() => {
      expect(screen.getByText('Total Returns')).toBeInTheDocument();
      expect(screen.getByText('100')).toBeInTheDocument();
      expect(screen.getByText('Total Return Value')).toBeInTheDocument();
      expect(screen.getByText('$5000.00')).toBeInTheDocument();
      expect(screen.getByText('Average Processing Time')).toBeInTheDocument();
      expect(screen.getByText('5.5 days')).toBeInTheDocument();
      expect(screen.getByText('Return Rate')).toBeInTheDocument();
      expect(screen.getByText('10.00%')).toBeInTheDocument();
    });
  });

  it('should display returns by status in analytics', async () => {
    (returnService.getAnalytics as jest.MockedFunction<typeof returnService.getAnalytics>).mockResolvedValue(mockAnalytics);

    renderWithRouter(<AdminReturnsPage />);

    const analyticsButton = screen.getByText('Analytics');
    fireEvent.click(analyticsButton);

    await waitFor(() => {
      expect(returnService.getAnalytics).toHaveBeenCalled();
    });

    await waitFor(() => {
      expect(screen.getByText('Returns by Status')).toBeInTheDocument();
    }, { timeout: 3000 });

    // Check for status items - they may be rendered in different formats
    expect(screen.getByText(/PENDING_APPROVAL/i)).toBeInTheDocument();
  });

  it('should display return reasons distribution in analytics', async () => {
    (returnService.getAnalytics as jest.MockedFunction<typeof returnService.getAnalytics>).mockResolvedValue(mockAnalytics);

    renderWithRouter(<AdminReturnsPage />);

    const analyticsButton = screen.getByText('Analytics');
    fireEvent.click(analyticsButton);

    await waitFor(() => {
      expect(returnService.getAnalytics).toHaveBeenCalled();
    });

    await waitFor(() => {
      expect(screen.getByText('Return Reasons Distribution')).toBeInTheDocument();
    }, { timeout: 3000 });

    // Check for reason items - they may be rendered in different formats
    expect(screen.getByText(/DEFECTIVE/i)).toBeInTheDocument();
  });
});

