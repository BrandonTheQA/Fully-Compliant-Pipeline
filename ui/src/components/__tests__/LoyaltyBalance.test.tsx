import { render, screen, waitFor } from '@testing-library/react';
import { LoyaltyBalance } from '../LoyaltyBalance';
import { loyaltyService } from '../../services/loyaltyService';

jest.mock('../../services/loyaltyService');

describe('LoyaltyBalance', () => {
  const mockUserId = 'user123';
  
  beforeEach(() => {
    jest.clearAllMocks();
  });
  
  it('should display loyalty balance', async () => {
    const mockAccount = {
      accountId: 'acc123',
      userId: mockUserId,
      currentPoints: 1500,
      currentTier: 'SILVER',
      lifetimePointsEarned: 1500,
      lifetimePointsRedeemed: 0,
      referralCode: 'REF123',
      enrollmentDate: '2024-01-01',
      isActive: true,
    };
    
    (loyaltyService.getBalance as jest.Mock).mockResolvedValue(mockAccount);
    
    render(<LoyaltyBalance userId={mockUserId} />);
    
    await waitFor(() => {
      expect(screen.getByText(/1,500/)).toBeInTheDocument();
    });
    
    expect(screen.getByText(/Points:/)).toBeInTheDocument();
  });
  
  it('should handle loading state', () => {
    (loyaltyService.getBalance as jest.Mock).mockImplementation(
      () => new Promise(() => {}) // Never resolves
    );
    
    render(<LoyaltyBalance userId={mockUserId} />);
    
    expect(screen.getByText(/Loading/)).toBeInTheDocument();
  });
  
  it('should call onBalanceChange callback', async () => {
    const mockAccount = {
      accountId: 'acc123',
      userId: mockUserId,
      currentPoints: 2000,
      currentTier: 'GOLD',
      lifetimePointsEarned: 2000,
      lifetimePointsRedeemed: 0,
      referralCode: 'REF123',
      enrollmentDate: '2024-01-01',
      isActive: true,
    };
    
    const onBalanceChange = jest.fn();
    (loyaltyService.getBalance as jest.Mock).mockResolvedValue(mockAccount);
    
    render(<LoyaltyBalance userId={mockUserId} onBalanceChange={onBalanceChange} />);
    
    await waitFor(() => {
      expect(onBalanceChange).toHaveBeenCalledWith(2000);
    });
  });
});
