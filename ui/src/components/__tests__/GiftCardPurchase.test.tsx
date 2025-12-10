import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { GiftCardPurchase } from '../GiftCardPurchase';
import { giftCardService } from '../../services/giftCardService';
import { useAppContext } from '../../context/AppContext';

jest.mock('../../services/giftCardService');
jest.mock('../../context/AppContext');

const mockUseAppContext = useAppContext as jest.MockedFunction<typeof useAppContext>;

describe('GiftCardPurchase', () => {
  const mockUser = {
    userId: 'user123',
    name: 'Test User',
    email: 'test@example.com',
  };
  
  beforeEach(() => {
    jest.clearAllMocks();
    mockUseAppContext.mockReturnValue({
      user: mockUser,
      cart: [],
      clearCart: jest.fn(),
      updateCartQuantity: jest.fn(),
      removeFromCart: jest.fn(),
      addToCart: jest.fn(),
      products: [],
      shippingRegion: null,
      freeShippingThreshold: null,
      shippingCost: null,
      defaultShippingCost: null,
      recommendations: [],
      loadingRecommendations: false,
    } as any);
  });
  
  it('should render purchase form', () => {
    render(<GiftCardPurchase />);
    
    expect(screen.getByRole('heading', { name: /Purchase Gift Card/i })).toBeInTheDocument();
    expect(screen.getByText('Select Amount')).toBeInTheDocument();
    expect(screen.getByText('Quantity')).toBeInTheDocument();
  });
  
  it('should select fixed amount', () => {
    render(<GiftCardPurchase />);
    
    const amountButtons = screen.getAllByRole('button', { name: /\$25/ });
    const amountButton = amountButtons[0];
    fireEvent.click(amountButton);
    
    expect(amountButton).toHaveClass('selected');
  });
  
  it('should enter custom amount', () => {
    render(<GiftCardPurchase />);
    
    const customInput = screen.getByPlaceholderText('Enter amount');
    fireEvent.change(customInput, { target: { value: '75.50' } });
    
    expect(customInput).toHaveValue('75.50');
  });
  
  it('should change quantity', () => {
    render(<GiftCardPurchase />);
    
    const quantityInput = screen.getByDisplayValue('1');
    const incrementButton = screen.getAllByText('+')[0];
    
    fireEvent.click(incrementButton);
    
    expect(screen.getByDisplayValue('2')).toBeInTheDocument();
  });
  
  it('should submit purchase successfully', async () => {
    const mockResponse = {
      giftCards: [{
        giftCardId: 'gc123',
        code: 'ABCD-EFGH-IJKL-MNOP',
        amount: 100,
        balance: 100,
        status: 'ACTIVE',
        purchaserEmail: 'test@example.com',
        purchaseDate: '2024-01-01T00:00:00',
        expirationDate: '2025-01-01T00:00:00',
      }],
      totalAmount: 100,
    };
    
    (giftCardService.purchaseGiftCard as jest.Mock).mockResolvedValue(mockResponse);
    
    render(<GiftCardPurchase />);
    
    const amountButton = screen.getByRole('button', { name: /\$100/ });
    fireEvent.click(amountButton);
    
    const purchaseButton = screen.getByRole('button', { name: /Purchase Gift Card/i });
    fireEvent.click(purchaseButton);
    
    await waitFor(() => {
      expect(screen.getByText('Gift Card(s) Purchased Successfully!')).toBeInTheDocument();
      expect(screen.getByText('ABCD-EFGH-IJKL-MNOP')).toBeInTheDocument();
    });
    
    expect(giftCardService.purchaseGiftCard).toHaveBeenCalled();
  });
  
  it('should validate amount before submission', async () => {
    render(<GiftCardPurchase />);
    
    const purchaseButton = screen.getByRole('button', { name: /Purchase Gift Card/i });
    
    expect(purchaseButton).toBeDisabled();
  });
  
  it('should validate custom amount range', async () => {
    render(<GiftCardPurchase />);
    
    const customInput = screen.getByPlaceholderText('Enter amount');
    const purchaseButton = screen.getByRole('button', { name: /Purchase Gift Card/i });
    
    fireEvent.change(customInput, { target: { value: '5' } });
    fireEvent.click(purchaseButton);
    
    // The button should remain disabled or error should appear
    await waitFor(() => {
      const errorMessage = screen.queryByRole('alert') || screen.queryByText(/at least/i);
      if (errorMessage) {
        expect(errorMessage).toBeInTheDocument();
      } else {
        // If no error message, button should be disabled
        expect(purchaseButton).toBeDisabled();
      }
    }, { timeout: 2000 });
  });
  
  it('should display total amount correctly', () => {
    render(<GiftCardPurchase />);
    
    const amountButtons = screen.getAllByRole('button', { name: /\$50/ });
    const amountButton = amountButtons[0];
    fireEvent.click(amountButton);
    
    const incrementButtons = screen.getAllByRole('button', { name: '+' });
    fireEvent.click(incrementButtons[0]);
    
    expect(screen.getByText(/Total:/)).toBeInTheDocument();
    // Check total amount in the total section specifically
    const totalSection = screen.getByText(/Total:/).closest('.total-section');
    expect(totalSection).toHaveTextContent('$100.00');
  });
  
  it('should handle purchase error', async () => {
    const errorMessage = 'Purchase failed';
    (giftCardService.purchaseGiftCard as jest.Mock).mockRejectedValue(new Error(errorMessage));
    
    render(<GiftCardPurchase />);
    
    const amountButton = screen.getByRole('button', { name: /\$100/ });
    fireEvent.click(amountButton);
    
    const purchaseButton = screen.getByRole('button', { name: /Purchase Gift Card/i });
    fireEvent.click(purchaseButton);
    
    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(errorMessage);
    });
  });
});
