/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter, MemoryRouter, Routes, Route } from 'react-router-dom';
import { OrderTrackingPage } from '../OrderTrackingPage';
import { AppProvider } from '../../context/AppContext';
import { orderService } from '../../services/orderService';
import type { OrderTracking, Order, User } from '../../types';

jest.mock('../../services/orderService');

// Mock EventSource
global.EventSource = jest.fn().mockImplementation(() => ({
  addEventListener: jest.fn(),
  removeEventListener: jest.fn(),
  close: jest.fn(),
  onerror: null,
  readyState: 0,
})) as any;

const renderWithRouter = (orderId: string, user?: User) => {
  return render(
    <MemoryRouter initialEntries={[`/orders/${orderId}/tracking`]}>
      <AppProvider>
        <Routes>
          <Route path="/orders/:orderId/tracking" element={<OrderTrackingPage />} />
        </Routes>
      </AppProvider>
    </MemoryRouter>
  );
};

describe('OrderTrackingPage', () => {
  const mockUser: User = {
    userId: 'user-123',
    name: 'John Doe',
    email: 'john@example.com',
  };

  const mockTracking: OrderTracking = {
    orderId: 'order-123',
    status: 'SHIPPED',
    trackingNumber: 'TRACK123',
    carrierName: 'FedEx',
    estimatedDeliveryDate: '2024-01-15T10:00:00Z',
    shippingAddress: '123 Main St',
    shippingMethod: 'Standard',
    currentLocation: 'Distribution Center',
    statusHistory: [
      {
        id: 'status-1',
        status: 'PENDING',
        createdAt: '2024-01-10T10:00:00Z',
      },
      {
        id: 'status-2',
        status: 'SHIPPED',
        createdAt: '2024-01-12T10:00:00Z',
      },
    ],
  };

  const mockOrder: Order = {
    id: 'order-123',
    userId: 'user-123',
    items: [
      {
        productId: 'product-1',
        productName: 'Test Product',
        quantity: 1,
        price: 29.99,
        subtotal: 29.99,
      },
    ],
    totalAmount: 29.99,
    status: 'SHIPPED',
  };

  beforeEach(() => {
    jest.clearAllMocks();
    sessionStorage.clear();
  });

  it('should render loading state initially', async () => {
    (orderService.getOrderTracking as jest.MockedFunction<typeof orderService.getOrderTracking>).mockResolvedValue(mockTracking);
    (orderService.getOrder as jest.MockedFunction<typeof orderService.getOrder>).mockResolvedValue(mockOrder);
    (orderService.subscribeToOrderUpdates as jest.MockedFunction<typeof orderService.subscribeToOrderUpdates>).mockReturnValue(
      new EventSource('') as any
    );

    renderWithRouter('order-123');

    // Component should show loading state immediately
    expect(screen.getByText('Loading order tracking information...')).toBeInTheDocument();
    
    // Wait for loading to complete - data should load quickly since mocks resolve immediately
    await waitFor(() => {
      expect(screen.queryByText('Loading order tracking information...')).not.toBeInTheDocument();
    }, { timeout: 3000 });
    
    // Verify data loaded
    await waitFor(() => {
      expect(screen.getByText(/Order #/i)).toBeInTheDocument();
    }, { timeout: 2000 });
  });

  it('should load and display tracking information', async () => {
    (orderService.getOrderTracking as jest.MockedFunction<typeof orderService.getOrderTracking>).mockResolvedValue(mockTracking);
    (orderService.getOrder as jest.MockedFunction<typeof orderService.getOrder>).mockResolvedValue(mockOrder);
    (orderService.subscribeToOrderUpdates as jest.MockedFunction<typeof orderService.subscribeToOrderUpdates>).mockReturnValue(
      new EventSource('') as any
    );

    renderWithRouter('order-123');

    await waitFor(() => {
      expect(orderService.getOrderTracking).toHaveBeenCalledWith('order-123');
    }, { timeout: 1000 });

    await waitFor(() => {
      expect(screen.getByText(/Order #/i)).toBeInTheDocument();
    }, { timeout: 3000 });
    
    // SHIPPED appears multiple times, just verify it exists
    expect(screen.getAllByText(/SHIPPED/i).length).toBeGreaterThan(0);
  });

  it('should display error message when tracking fails to load', async () => {
    const error = new Error('Failed to load tracking');
    (orderService.getOrderTracking as jest.MockedFunction<typeof orderService.getOrderTracking>).mockRejectedValue(error);
    (orderService.subscribeToOrderUpdates as jest.MockedFunction<typeof orderService.subscribeToOrderUpdates>).mockReturnValue(
      new EventSource('') as any
    );

    renderWithRouter('order-123');

    await waitFor(() => {
      expect(screen.getByText('Failed to load tracking')).toBeInTheDocument();
      expect(screen.getByText('← Back to Orders')).toBeInTheDocument();
    }, { timeout: 2000 });
  });

  it('should display "Order not found" when tracking is null', async () => {
    (orderService.getOrderTracking as jest.MockedFunction<typeof orderService.getOrderTracking>).mockResolvedValue(null as any);
    (orderService.subscribeToOrderUpdates as jest.MockedFunction<typeof orderService.subscribeToOrderUpdates>).mockReturnValue(
      new EventSource('') as any
    );

    renderWithRouter('order-123');

    await waitFor(() => {
      expect(screen.getByText('Order not found')).toBeInTheDocument();
    }, { timeout: 2000 });
  });

  it('should handle order load failure gracefully', async () => {
    (orderService.getOrderTracking as jest.MockedFunction<typeof orderService.getOrderTracking>).mockResolvedValue(mockTracking);
    (orderService.getOrder as jest.MockedFunction<typeof orderService.getOrder>).mockRejectedValue(new Error('Order not found'));
    (orderService.subscribeToOrderUpdates as jest.MockedFunction<typeof orderService.subscribeToOrderUpdates>).mockReturnValue(
      new EventSource('') as any
    );

    renderWithRouter('order-123');

    await waitFor(() => {
      // Should still display tracking even if order fails
      expect(screen.getByText(/Order #/i)).toBeInTheDocument();
    }, { timeout: 2000 });
  });

  it('should render OrderTrackingHeader component', async () => {
    (orderService.getOrderTracking as jest.MockedFunction<typeof orderService.getOrderTracking>).mockResolvedValue(mockTracking);
    (orderService.getOrder as jest.MockedFunction<typeof orderService.getOrder>).mockResolvedValue(mockOrder);
    (orderService.subscribeToOrderUpdates as jest.MockedFunction<typeof orderService.subscribeToOrderUpdates>).mockReturnValue(
      new EventSource('') as any
    );

    renderWithRouter('order-123');

    await waitFor(() => {
      expect(screen.getByText(/Order #/i)).toBeInTheDocument();
    }, { timeout: 2000 });
  });

  it('should render OrderStatusTimeline component', async () => {
    (orderService.getOrderTracking as jest.MockedFunction<typeof orderService.getOrderTracking>).mockResolvedValue(mockTracking);
    (orderService.getOrder as jest.MockedFunction<typeof orderService.getOrder>).mockResolvedValue(mockOrder);
    (orderService.subscribeToOrderUpdates as jest.MockedFunction<typeof orderService.subscribeToOrderUpdates>).mockReturnValue(
      new EventSource('') as any
    );

    renderWithRouter('order-123');

    await waitFor(() => {
      expect(screen.getByText('Order Status Timeline')).toBeInTheDocument();
    }, { timeout: 2000 });
  });

  it('should render OrderDetailsCard component', async () => {
    (orderService.getOrderTracking as jest.MockedFunction<typeof orderService.getOrderTracking>).mockResolvedValue(mockTracking);
    (orderService.getOrder as jest.MockedFunction<typeof orderService.getOrder>).mockResolvedValue(mockOrder);
    (orderService.subscribeToOrderUpdates as jest.MockedFunction<typeof orderService.subscribeToOrderUpdates>).mockReturnValue(
      new EventSource('') as any
    );

    renderWithRouter('order-123');

    await waitFor(() => {
      expect(screen.getByText('Order Details')).toBeInTheDocument();
    }, { timeout: 2000 });
  });

  it('should render NotificationPreferences when user is logged in', async () => {
    sessionStorage.setItem('user', JSON.stringify(mockUser));
    (orderService.getOrderTracking as jest.MockedFunction<typeof orderService.getOrderTracking>).mockResolvedValue(mockTracking);
    (orderService.getOrder as jest.MockedFunction<typeof orderService.getOrder>).mockResolvedValue(mockOrder);
    (orderService.subscribeToOrderUpdates as jest.MockedFunction<typeof orderService.subscribeToOrderUpdates>).mockReturnValue(
      new EventSource('') as any
    );
    // Mock NotificationPreferences service calls
    (orderService.getNotificationPreferences as jest.MockedFunction<typeof orderService.getNotificationPreferences>).mockResolvedValue({
      userId: 'user-123',
      emailEnabled: true,
      smsEnabled: false,
      phoneNumber: '',
      notificationFrequency: 'ALL',
    });

    renderWithRouter('order-123', mockUser);

    await waitFor(() => {
      expect(screen.getByText('Notification Preferences')).toBeInTheDocument();
    }, { timeout: 3000 });
  });

  it('should not render NotificationPreferences when user is not logged in', async () => {
    (orderService.getOrderTracking as jest.MockedFunction<typeof orderService.getOrderTracking>).mockResolvedValue(mockTracking);
    (orderService.getOrder as jest.MockedFunction<typeof orderService.getOrder>).mockResolvedValue(mockOrder);
    (orderService.subscribeToOrderUpdates as jest.MockedFunction<typeof orderService.subscribeToOrderUpdates>).mockReturnValue(
      new EventSource('') as any
    );

    renderWithRouter('order-123');

    await waitFor(() => {
      expect(screen.queryByText('Notification Preferences')).not.toBeInTheDocument();
    }, { timeout: 2000 });
  });

  it('should subscribe to order updates via EventSource', async () => {
    (orderService.getOrderTracking as jest.MockedFunction<typeof orderService.getOrderTracking>).mockResolvedValue(mockTracking);
    (orderService.getOrder as jest.MockedFunction<typeof orderService.getOrder>).mockResolvedValue(mockOrder);
    (orderService.subscribeToOrderUpdates as jest.MockedFunction<typeof orderService.subscribeToOrderUpdates>).mockReturnValue(
      new EventSource('') as any
    );

    renderWithRouter('order-123');

    await waitFor(() => {
      expect(orderService.subscribeToOrderUpdates).toHaveBeenCalledWith('order-123', expect.any(Function));
    });
  });

  it('should display live updates indicator when connected', async () => {
    (orderService.getOrderTracking as jest.MockedFunction<typeof orderService.getOrderTracking>).mockResolvedValue(mockTracking);
    (orderService.getOrder as jest.MockedFunction<typeof orderService.getOrder>).mockResolvedValue(mockOrder);
    (orderService.subscribeToOrderUpdates as jest.MockedFunction<typeof orderService.subscribeToOrderUpdates>).mockReturnValue(
      new EventSource('') as any
    );

    renderWithRouter('order-123');

    await waitFor(() => {
      expect(screen.getByText('Live Updates')).toBeInTheDocument();
    });
  });

  it('should render back to orders link', async () => {
    (orderService.getOrderTracking as jest.MockedFunction<typeof orderService.getOrderTracking>).mockResolvedValue(mockTracking);
    (orderService.getOrder as jest.MockedFunction<typeof orderService.getOrder>).mockResolvedValue(mockOrder);
    (orderService.subscribeToOrderUpdates as jest.MockedFunction<typeof orderService.subscribeToOrderUpdates>).mockReturnValue(
      new EventSource('') as any
    );

    renderWithRouter('order-123');

    await waitFor(() => {
      const backLink = screen.getByText('← Back to Orders');
      expect(backLink).toBeInTheDocument();
      expect(backLink.closest('a')?.getAttribute('href')).toBe('/orders');
    }, { timeout: 2000 });
  });

  it('should reload tracking data when update is received', async () => {
    (orderService.getOrderTracking as jest.MockedFunction<typeof orderService.getOrderTracking>).mockResolvedValue(mockTracking);
    (orderService.getOrder as jest.MockedFunction<typeof orderService.getOrder>).mockResolvedValue(mockOrder);
    
    let updateCallback: (data: any) => void;
    (orderService.subscribeToOrderUpdates as jest.MockedFunction<typeof orderService.subscribeToOrderUpdates>).mockImplementation(
      (orderId, onUpdate) => {
        updateCallback = onUpdate;
        return new EventSource('') as any;
      }
    );

    renderWithRouter('order-123');

    await waitFor(() => {
      expect(orderService.getOrderTracking).toHaveBeenCalledTimes(1);
    });

    // Simulate update
    if (updateCallback!) {
      updateCallback({});
    }

    await waitFor(() => {
      expect(orderService.getOrderTracking).toHaveBeenCalledTimes(2);
    });
  });
});
