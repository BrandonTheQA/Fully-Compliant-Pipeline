import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { GiftCardBalance } from '../GiftCardBalance';
import { giftCardService } from '../../services/giftCardService';

jest.mock('../../services/giftCardService');

describe('GiftCardBalance', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });
  
  it('should render balance inquiry form', () => {
    render(<GiftCardBalance />);
    
    expect(screen.getByText('Check Gift Card Balance')).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Enter gift card code/)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Check Balance/ })).toBeInTheDocument();
  });
  
  it('should check balance successfully', async () => {
    const mockBalance = {
      code: 'ABCD-EFGH-IJKL-MNOP',
      balance: 100,
      amount: 100,
      status: 'ACTIVE',
      expirationDate: '2025-01-01T00:00:00',
    };
    
    (giftCardService.checkBalance as jest.Mock).mockResolvedValue(mockBalance);
    
    render(<GiftCardBalance />);
    
    const codeInput = screen.getByPlaceholderText(/Enter gift card code/);
    const checkButton = screen.getByRole('button', { name: /Check Balance/ });
    
    fireEvent.change(codeInput, { target: { value: 'ABCD-EFGH-IJKL-MNOP' } });
    fireEvent.click(checkButton);
    
    await waitFor(() => {
      expect(screen.getByText('Gift Card Information')).toBeInTheDocument();
      expect(screen.getByText('ABCD-EFGH-IJKL-MNOP')).toBeInTheDocument();
    });
    
    // Check balance (there are multiple $100.00 elements, so use more specific query)
    const balanceValue = screen.getByText('Balance:').closest('.balance-row')?.querySelector('.balance-value');
    expect(balanceValue).toHaveTextContent('$100.00');
    
    expect(giftCardService.checkBalance).toHaveBeenCalledWith('ABCD-EFGH-IJKL-MNOP');
  });
  
  it('should display error message on failure', async () => {
    const errorMessage = 'Gift card not found';
    (giftCardService.checkBalance as jest.Mock).mockRejectedValue(new Error(errorMessage));
    
    render(<GiftCardBalance />);
    
    const codeInput = screen.getByPlaceholderText(/Enter gift card code/);
    const checkButton = screen.getByRole('button', { name: /Check Balance/ });
    
    fireEvent.change(codeInput, { target: { value: 'INVALID-CODE' } });
    fireEvent.click(checkButton);
    
    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeInTheDocument();
    });
  });
  
  it('should validate empty code', async () => {
    render(<GiftCardBalance />);
    
    const checkButton = screen.getByRole('button', { name: /Check Balance/ });
    fireEvent.click(checkButton);
    
    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Please enter a gift card code');
    });
    
    expect(giftCardService.checkBalance).not.toHaveBeenCalled();
  });
  
  it('should display balance details correctly', async () => {
    const mockBalance = {
      code: 'ABCD-EFGH-IJKL-MNOP',
      balance: 75.50,
      amount: 100,
      status: 'ACTIVE',
      expirationDate: '2025-01-01T00:00:00',
    };
    
    (giftCardService.checkBalance as jest.Mock).mockResolvedValue(mockBalance);
    
    render(<GiftCardBalance />);
    
    const codeInput = screen.getByPlaceholderText(/Enter gift card code/);
    const checkButton = screen.getByRole('button', { name: /Check Balance/ });
    
    fireEvent.change(codeInput, { target: { value: 'ABCD-EFGH-IJKL-MNOP' } });
    fireEvent.click(checkButton);
    
    await waitFor(() => {
      expect(screen.getByText('$75.50')).toBeInTheDocument();
      expect(screen.getByText('$100.00')).toBeInTheDocument();
      expect(screen.getByText('ACTIVE')).toBeInTheDocument();
    });
  });
});
