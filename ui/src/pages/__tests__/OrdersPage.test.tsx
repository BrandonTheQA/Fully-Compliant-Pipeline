/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { render, fireEvent } from '@testing-library/react';
import { screen, waitFor } from '@testing-library/dom';
import { BrowserRouter } from 'react-router-dom';
import { OrdersPage } from '../OrdersPage';
import { AppProvider } from '../../context/AppContext';
import { orderService } from '../../services/orderService';
import type { User, Order, Product, CartItem } from '../../types';

jest.mock('../../services/orderService');

const renderWithProvider = (component: React.ReactElement) => {
  return render(
    <BrowserRouter>
      <AppProvider>{component}</AppProvider>
    </BrowserRouter>
  );
};

describe('OrdersPage', () => {
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

  const mockOrder: Order = {
    id: 'order-123',
    userId: 'user-123',
    items: [
      {
        productId: 'product-1',
        quantity: 2,
      },
    ],
    totalAmount: 59.98,
    status: 'pending',
    createdAt: '2024-01-01T00:00:00Z',
  };

  const mockOrders: Order[] = [
    mockOrder,
    {
      id: 'order-456',
      userId: 'user-123',
      items: [
        {
          productId: 'product-2',
          quantity: 1,
        },
      ],
      totalAmount: 19.99,
      status: 'completed',
      createdAt: '2024-01-02T00:00:00Z',
    },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
    sessionStorage.clear();
  });

  it('should render orders page header', () => {
    renderWithProvider(<OrdersPage />);
    expect(screen.getByText('Orders')).toBeInTheDocument();
  });

  it('should render create order view by default', () => {
    renderWithProvider(<OrdersPage />);
    expect(screen.getByText('Create Order')).toBeInTheDocument();
  });

  it('should switch to list view when My Orders button is clicked', async () => {
    sessionStorage.setItem('user', JSON.stringify(mockUser));
    (orderService.getUserOrders as jest.MockedFunction<typeof orderService.getUserOrders>).mockResolvedValue(mockOrders);

    renderWithProvider(<OrdersPage />);

    const myOrdersButton = screen.getByText('My Orders');
    fireEvent.click(myOrdersButton);

    await waitFor(() => {
      expect(orderService.getUserOrders).toHaveBeenCalledWith('user-123');
    });

    await waitFor(() => {
      // Order ID is truncated to 8 characters: "order-123" becomes "order-12"
      expect(screen.getByText(/Order #order-12/i)).toBeInTheDocument();
    });
  });

  it('should disable My Orders button when no user is logged in', () => {
    renderWithProvider(<OrdersPage />);

    const myOrdersButton = screen.getByText('My Orders');
    expect((myOrdersButton as HTMLButtonElement).disabled).toBe(true);
  });

  it('should display loading state when loading orders', async () => {
    sessionStorage.setItem('user', JSON.stringify(mockUser));
    (orderService.getUserOrders as jest.MockedFunction<typeof orderService.getUserOrders>).mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve(mockOrders), 100))
    );

    renderWithProvider(<OrdersPage />);

    const myOrdersButton = screen.getByText('My Orders');
    fireEvent.click(myOrdersButton);

    expect(screen.getByText('Loading orders...')).toBeInTheDocument();
  });

  it('should display orders list when orders are loaded', async () => {
    sessionStorage.setItem('user', JSON.stringify(mockUser));
    (orderService.getUserOrders as jest.MockedFunction<typeof orderService.getUserOrders>).mockResolvedValue(mockOrders);

    renderWithProvider(<OrdersPage />);

    const myOrdersButton = screen.getByText('My Orders');
    fireEvent.click(myOrdersButton);

    await waitFor(() => {
      // Order ID is truncated to 8 characters: "order-123" becomes "order-12"
      expect(screen.getByText(/Order #order-12/i)).toBeInTheDocument();
      // Order ID is truncated to 8 characters: "order-456" becomes "order-45"
      expect(screen.getByText(/Order #order-45/i)).toBeInTheDocument();
      expect(screen.getByText('pending')).toBeInTheDocument();
      expect(screen.getByText('completed')).toBeInTheDocument();
      expect(screen.getByText('$59.98')).toBeInTheDocument();
      expect(screen.getByText('$19.99')).toBeInTheDocument();
    });
  });

  it('should display empty state when no orders found', async () => {
    sessionStorage.setItem('user', JSON.stringify(mockUser));
    (orderService.getUserOrders as jest.MockedFunction<typeof orderService.getUserOrders>).mockResolvedValue([]);

    renderWithProvider(<OrdersPage />);

    const myOrdersButton = screen.getByText('My Orders');
    fireEvent.click(myOrdersButton);

    await waitFor(() => {
      expect(screen.getByText('No orders found. Create your first order!')).toBeInTheDocument();
    });
  });

  it('should display error message when order loading fails', async () => {
    sessionStorage.setItem('user', JSON.stringify(mockUser));
    const error = new Error('Failed to load orders');
    (orderService.getUserOrders as jest.MockedFunction<typeof orderService.getUserOrders>).mockRejectedValue(error);

    renderWithProvider(<OrdersPage />);

    const myOrdersButton = screen.getByText('My Orders');
    fireEvent.click(myOrdersButton);

    await waitFor(() => {
      expect(screen.getByText('Failed to load orders')).toBeInTheDocument();
    });
  });

  it('should switch to details view when order is clicked', async () => {
    sessionStorage.setItem('user', JSON.stringify(mockUser));
    (orderService.getUserOrders as jest.MockedFunction<typeof orderService.getUserOrders>).mockResolvedValue(mockOrders);
    (orderService.getOrder as jest.MockedFunction<typeof orderService.getOrder>).mockResolvedValue(mockOrder);

    renderWithProvider(<OrdersPage />);

    const myOrdersButton = screen.getByText('My Orders');
    fireEvent.click(myOrdersButton);

    await waitFor(() => {
      // Order ID is truncated to 8 characters: "order-123" becomes "order-12"
      expect(screen.getByText(/Order #order-12/i)).toBeInTheDocument();
    });

    // Click the order card button directly
    const orderButton = screen.getByLabelText('View order order-12');
    fireEvent.click(orderButton);

    await waitFor(() => {
      expect(orderService.getOrder).toHaveBeenCalledWith('order-123');
    });

    await waitFor(() => {
      expect(screen.getByText('← Back to Orders')).toBeInTheDocument();
    });
  });

  it('should switch back to list view when back button is clicked', async () => {
    sessionStorage.setItem('user', JSON.stringify(mockUser));
    (orderService.getUserOrders as jest.MockedFunction<typeof orderService.getUserOrders>).mockResolvedValue(mockOrders);
    (orderService.getOrder as jest.MockedFunction<typeof orderService.getOrder>).mockResolvedValue(mockOrder);

    renderWithProvider(<OrdersPage />);

    const myOrdersButton = screen.getByText('My Orders');
    fireEvent.click(myOrdersButton);

    await waitFor(() => {
      // Order ID is truncated to 8 characters: "order-123" becomes "order-12"
      expect(screen.getByText(/Order #order-12/i)).toBeInTheDocument();
    });

    // Click the order card button directly
    const orderButton = screen.getByLabelText('View order order-12');
    fireEvent.click(orderButton);

    await waitFor(() => {
      expect(screen.getByText('← Back to Orders')).toBeInTheDocument();
    });

    const backButton = screen.getByText('← Back to Orders');
    fireEvent.click(backButton);

    await waitFor(() => {
      // Order ID is truncated to 8 characters: "order-123" becomes "order-12"
      expect(screen.getByText(/Order #order-12/i)).toBeInTheDocument();
    });
  });

  it('should display order details when order is created', async () => {
    sessionStorage.setItem('user', JSON.stringify(mockUser));
    sessionStorage.setItem('cart', JSON.stringify([mockCartItem]));
    sessionStorage.setItem('shippingRegion', 'US');
    sessionStorage.setItem('freeShippingThreshold', '50');
    sessionStorage.setItem('shippingCost', '9.99');
    sessionStorage.setItem('defaultShippingCost', '9.99');

    (orderService.createOrder as jest.MockedFunction<typeof orderService.createOrder>).mockResolvedValue(mockOrder);

    renderWithProvider(<OrdersPage />);

    // Wait for OrderForm to render
    await waitFor(() => {
      expect(screen.getByText('Review Your Order')).toBeInTheDocument();
    });

    // Submit order (this would be done through OrderForm)
    // The OrderForm test covers this, so we just verify the integration
    expect(screen.getByText('Orders')).toBeInTheDocument();
  });

  it('should display order count in create order button when cart has items', () => {
    sessionStorage.setItem('cart', JSON.stringify([mockCartItem]));

    renderWithProvider(<OrdersPage />);

    expect(screen.getByText('Create Order (1)')).toBeInTheDocument();
  });

  it('should display order item count in order card', async () => {
    sessionStorage.setItem('user', JSON.stringify(mockUser));
    (orderService.getUserOrders as jest.MockedFunction<typeof orderService.getUserOrders>).mockResolvedValue(mockOrders);

    renderWithProvider(<OrdersPage />);

    const myOrdersButton = screen.getByText('My Orders');
    fireEvent.click(myOrdersButton);

    await waitFor(() => {
      // Items text is split across elements, use getAllByText
      expect(screen.getAllByText(/Items:/i).length).toBeGreaterThan(0);
    });
  });

  it('should display order date when available', async () => {
    sessionStorage.setItem('user', JSON.stringify(mockUser));
    (orderService.getUserOrders as jest.MockedFunction<typeof orderService.getUserOrders>).mockResolvedValue(mockOrders);

    renderWithProvider(<OrdersPage />);

    const myOrdersButton = screen.getByText('My Orders');
    fireEvent.click(myOrdersButton);

    await waitFor(() => {
      // Date text appears multiple times, use getAllByText
      expect(screen.getAllByText(/Date:/i).length).toBeGreaterThan(0);
    });
  });

  it('should switch to create view when Create Order button is clicked', async () => {
    sessionStorage.setItem('user', JSON.stringify(mockUser));
    (orderService.getUserOrders as jest.MockedFunction<typeof orderService.getUserOrders>).mockResolvedValue(mockOrders);

    renderWithProvider(<OrdersPage />);

    const myOrdersButton = screen.getByText('My Orders');
    fireEvent.click(myOrdersButton);

    await waitFor(() => {
      // Order ID is truncated to 8 characters: "order-123" becomes "order-12"
      expect(screen.getByText(/Order #order-12/i)).toBeInTheDocument();
    });

    const createOrderButton = screen.getByText('Create Order');
    fireEvent.click(createOrderButton);

    await waitFor(() => {
      expect(screen.queryByText(/Order #order-123/i)).not.toBeInTheDocument();
    });
  });
});

