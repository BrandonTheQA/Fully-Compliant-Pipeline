import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { returnService } from '../returnService';
import { orderApi } from '../api';
import type { Return, CreateReturnRequest, ReturnTracking, ReturnPolicy, ExchangeRequest, ReturnAnalytics, Order } from '../../types';

jest.mock('../api');

describe('returnService', () => {
  const mockOrderApi = orderApi as jest.Mocked<typeof orderApi>;

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('createReturn', () => {
    it('should create a return successfully', async () => {
      const returnData: CreateReturnRequest = {
        orderId: 'order-123',
        userId: 'user-123',
        items: [
          {
            orderItemId: 1,
            quantity: 1,
            returnReason: 'DEFECTIVE',
          },
        ],
        returnType: 'REFUND_TO_PAYMENT',
      };

      const mockReturn: Return = {
        returnId: 'return-123',
        orderId: 'order-123',
        userId: 'user-123',
        rmaNumber: 'RMA-20240101-00001',
        status: 'PENDING_APPROVAL',
        returnType: 'REFUND_TO_PAYMENT',
        items: [],
        statusHistory: [],
        createdAt: '2024-01-01T10:00:00Z',
        updatedAt: '2024-01-01T10:00:00Z',
      };

      mockOrderApi.post.mockResolvedValue({ data: mockReturn });

      const result = await returnService.createReturn(returnData);

      expect(mockOrderApi.post).toHaveBeenCalledWith('/returns', returnData);
      expect(result).toEqual(mockReturn);
    });

    it('should handle errors when creating return', async () => {
      const returnData: CreateReturnRequest = {
        orderId: 'order-123',
        userId: 'user-123',
        items: [
          {
            orderItemId: 1,
            quantity: 1,
            returnReason: 'DEFECTIVE',
          },
        ],
        returnType: 'REFUND_TO_PAYMENT',
      };

      const error = new Error('Failed to create return');
      mockOrderApi.post.mockRejectedValue(error);

      await expect(returnService.createReturn(returnData)).rejects.toThrow('Failed to create return');
    });
  });

  describe('getReturnByRMA', () => {
    it('should get return by RMA successfully', async () => {
      const rmaNumber = 'RMA-20240101-00001';
      const mockReturn: Return = {
        returnId: 'return-123',
        orderId: 'order-123',
        userId: 'user-123',
        rmaNumber: 'RMA-20240101-00001',
        status: 'PENDING_APPROVAL',
        returnType: 'REFUND_TO_PAYMENT',
        items: [],
        statusHistory: [],
        createdAt: '2024-01-01T10:00:00Z',
        updatedAt: '2024-01-01T10:00:00Z',
      };

      mockOrderApi.get.mockResolvedValue({ data: mockReturn });

      const result = await returnService.getReturnByRMA(rmaNumber);

      expect(mockOrderApi.get).toHaveBeenCalledWith('/returns/rma/RMA-20240101-00001');
      expect(result).toEqual(mockReturn);
    });
  });

  describe('getUserReturns', () => {
    it('should get user returns successfully', async () => {
      const userId = 'user-123';
      const mockReturns: Return[] = [
        {
          returnId: 'return-123',
          orderId: 'order-123',
          userId: 'user-123',
          rmaNumber: 'RMA-20240101-00001',
          status: 'PENDING_APPROVAL',
          returnType: 'REFUND_TO_PAYMENT',
          items: [],
          statusHistory: [],
          createdAt: '2024-01-01T10:00:00Z',
          updatedAt: '2024-01-01T10:00:00Z',
        },
      ];

      mockOrderApi.get.mockResolvedValue({ data: mockReturns });

      const result = await returnService.getUserReturns(userId);

      expect(mockOrderApi.get).toHaveBeenCalledWith('/returns/user/user-123');
      expect(result).toEqual(mockReturns);
    });
  });

  describe('getReturnTracking', () => {
    it('should get return tracking successfully', async () => {
      const returnId = 'return-123';
      const mockTracking: ReturnTracking = {
        returnId: 'return-123',
        rmaNumber: 'RMA-20240101-00001',
        status: 'APPROVED',
        returnType: 'REFUND_TO_PAYMENT',
        statusHistory: [],
        items: [],
      };

      mockOrderApi.get.mockResolvedValue({ data: mockTracking });

      const result = await returnService.getReturnTracking(returnId);

      expect(mockOrderApi.get).toHaveBeenCalledWith('/returns/return-123/tracking');
      expect(result).toEqual(mockTracking);
    });
  });

  describe('getReturnPolicy', () => {
    it('should get return policy successfully', async () => {
      const mockPolicy: ReturnPolicy = {
        returnWindowDays: 30,
        restockingFeePercentage: 10,
        freeReturnThreshold: 50.00,
      };

      mockOrderApi.get.mockResolvedValue({ data: mockPolicy });

      const result = await returnService.getReturnPolicy();

      expect(mockOrderApi.get).toHaveBeenCalledWith('/returns/policy');
      expect(result).toEqual(mockPolicy);
    });
  });

  describe('createExchange', () => {
    it('should create exchange successfully', async () => {
      const returnId = 'return-123';
      const exchangeData: ExchangeRequest = {
        exchangeProductId: 'product-456',
        quantity: 1,
        notes: 'Exchange notes',
      };

      const mockOrder: Order = {
        id: 'order-456',
        userId: 'user-123',
        items: [],
        totalAmount: 29.99,
        status: 'PENDING',
      };

      mockOrderApi.post.mockResolvedValue({ data: mockOrder });

      const result = await returnService.createExchange(returnId, exchangeData);

      expect(mockOrderApi.post).toHaveBeenCalledWith('/returns/return-123/exchange', exchangeData);
      expect(result).toEqual(mockOrder);
    });
  });

  describe('getAdminReturns', () => {
    it('should get admin returns without filters', async () => {
      const mockReturns: Return[] = [];

      mockOrderApi.get.mockResolvedValue({ data: mockReturns });

      const result = await returnService.getAdminReturns();

      expect(mockOrderApi.get).toHaveBeenCalledWith('/admin/returns');
      expect(result).toEqual(mockReturns);
    });

    it('should get admin returns with status filter', async () => {
      const mockReturns: Return[] = [];
      const params = { status: 'PENDING_APPROVAL' };

      mockOrderApi.get.mockResolvedValue({ data: mockReturns });

      const result = await returnService.getAdminReturns(params);

      expect(mockOrderApi.get).toHaveBeenCalledWith('/admin/returns?status=PENDING_APPROVAL');
      expect(result).toEqual(mockReturns);
    });

    it('should get admin returns with multiple filters', async () => {
      const mockReturns: Return[] = [];
      const params = {
        status: 'PENDING_APPROVAL',
        userId: 'user-123',
        orderId: 'order-123',
        rmaNumber: 'RMA-20240101-00001',
      };

      mockOrderApi.get.mockResolvedValue({ data: mockReturns });

      const result = await returnService.getAdminReturns(params);

      expect(mockOrderApi.get).toHaveBeenCalledWith(
        '/admin/returns?status=PENDING_APPROVAL&userId=user-123&orderId=order-123&rmaNumber=RMA-20240101-00001'
      );
      expect(result).toEqual(mockReturns);
    });
  });

  describe('getAdminReturn', () => {
    it('should get admin return successfully', async () => {
      const returnId = 'return-123';
      const mockReturn: Return = {
        returnId: 'return-123',
        orderId: 'order-123',
        userId: 'user-123',
        rmaNumber: 'RMA-20240101-00001',
        status: 'PENDING_APPROVAL',
        returnType: 'REFUND_TO_PAYMENT',
        items: [],
        statusHistory: [],
        createdAt: '2024-01-01T10:00:00Z',
        updatedAt: '2024-01-01T10:00:00Z',
      };

      mockOrderApi.get.mockResolvedValue({ data: mockReturn });

      const result = await returnService.getAdminReturn(returnId);

      expect(mockOrderApi.get).toHaveBeenCalledWith('/admin/returns/return-123');
      expect(result).toEqual(mockReturn);
    });
  });

  describe('approveReturn', () => {
    it('should approve return without notes', async () => {
      const returnId = 'return-123';

      mockOrderApi.post.mockResolvedValue({ data: {} });

      await returnService.approveReturn(returnId);

      expect(mockOrderApi.post).toHaveBeenCalledWith('/admin/returns/return-123/approve', {});
    });

    it('should approve return with notes', async () => {
      const returnId = 'return-123';
      const notes = 'Approval notes';

      mockOrderApi.post.mockResolvedValue({ data: {} });

      await returnService.approveReturn(returnId, notes);

      expect(mockOrderApi.post).toHaveBeenCalledWith('/admin/returns/return-123/approve', { notes });
    });

    it('should approve return with approvedBy', async () => {
      const returnId = 'return-123';
      const notes = 'Approval notes';
      const approvedBy = 'admin-1';

      mockOrderApi.post.mockResolvedValue({ data: {} });

      await returnService.approveReturn(returnId, notes, approvedBy);

      expect(mockOrderApi.post).toHaveBeenCalledWith('/admin/returns/return-123/approve?approvedBy=admin-1', { notes });
    });
  });

  describe('rejectReturn', () => {
    it('should reject return successfully', async () => {
      const returnId = 'return-123';
      const reason = 'Rejection reason';

      mockOrderApi.post.mockResolvedValue({ data: {} });

      await returnService.rejectReturn(returnId, reason);

      // URL encoding may vary, so check that it contains the key parts
      const callArgs = (mockOrderApi.post as jest.Mock).mock.calls[0][0];
      expect(callArgs).toContain('/admin/returns/return-123/reject');
      expect(callArgs).toContain('reason=');
      expect(callArgs).toContain('Rejection');
    });

    it('should reject return with rejectedBy', async () => {
      const returnId = 'return-123';
      const reason = 'Rejection reason';
      const rejectedBy = 'admin-1';

      mockOrderApi.post.mockResolvedValue({ data: {} });

      await returnService.rejectReturn(returnId, reason, rejectedBy);

      // URL encoding may vary, so check that it contains the key parts
      const callArgs = (mockOrderApi.post as jest.Mock).mock.calls[0][0];
      expect(callArgs).toContain('/admin/returns/return-123/reject');
      expect(callArgs).toContain('reason=');
      expect(callArgs).toContain('rejectedBy=admin-1');
    });
  });

  describe('updateReturnStatus', () => {
    it('should update return status without notes', async () => {
      const returnId = 'return-123';
      const status = 'IN_TRANSIT';

      mockOrderApi.put.mockResolvedValue({ data: {} });

      await returnService.updateReturnStatus(returnId, status);

      expect(mockOrderApi.put).toHaveBeenCalledWith('/admin/returns/return-123/status', { status, notes: undefined });
    });

    it('should update return status with notes', async () => {
      const returnId = 'return-123';
      const status = 'IN_TRANSIT';
      const notes = 'Status update notes';

      mockOrderApi.put.mockResolvedValue({ data: {} });

      await returnService.updateReturnStatus(returnId, status, notes);

      expect(mockOrderApi.put).toHaveBeenCalledWith('/admin/returns/return-123/status', { status, notes });
    });

    it('should update return status with updatedBy', async () => {
      const returnId = 'return-123';
      const status = 'IN_TRANSIT';
      const notes = 'Status update notes';
      const updatedBy = 'admin-1';

      mockOrderApi.put.mockResolvedValue({ data: {} });

      await returnService.updateReturnStatus(returnId, status, notes, updatedBy);

      expect(mockOrderApi.put).toHaveBeenCalledWith('/admin/returns/return-123/status?updatedBy=admin-1', { status, notes });
    });
  });

  describe('markReturnReceived', () => {
    it('should mark return as received without notes', async () => {
      const returnId = 'return-123';

      mockOrderApi.post.mockResolvedValue({ data: {} });

      await returnService.markReturnReceived(returnId);

      expect(mockOrderApi.post).toHaveBeenCalledWith('/admin/returns/return-123/received');
    });

    it('should mark return as received with notes', async () => {
      const returnId = 'return-123';
      const notes = 'Received notes';

      mockOrderApi.post.mockResolvedValue({ data: {} });

      await returnService.markReturnReceived(returnId, notes);

      // URL encoding may vary, so check that it contains the key parts
      const callArgs = (mockOrderApi.post as jest.Mock).mock.calls[0][0];
      expect(callArgs).toContain('/admin/returns/return-123/received');
      expect(callArgs).toContain('notes=');
      expect(callArgs).toContain('Received');
    });

    it('should mark return as received with receivedBy', async () => {
      const returnId = 'return-123';
      const notes = 'Received notes';
      const receivedBy = 'admin-1';

      mockOrderApi.post.mockResolvedValue({ data: {} });

      await returnService.markReturnReceived(returnId, notes, receivedBy);

      // URL encoding may vary, so check that it contains the key parts
      const callArgs = (mockOrderApi.post as jest.Mock).mock.calls[0][0];
      expect(callArgs).toContain('/admin/returns/return-123/received');
      expect(callArgs).toContain('notes=');
      expect(callArgs).toContain('receivedBy=admin-1');
    });
  });

  describe('processRefund', () => {
    it('should process refund successfully', async () => {
      const returnId = 'return-123';

      mockOrderApi.post.mockResolvedValue({ data: {} });

      await returnService.processRefund(returnId);

      expect(mockOrderApi.post).toHaveBeenCalledWith('/admin/returns/return-123/refund');
    });

    it('should process refund with processedBy', async () => {
      const returnId = 'return-123';
      const processedBy = 'admin-1';

      mockOrderApi.post.mockResolvedValue({ data: {} });

      await returnService.processRefund(returnId, processedBy);

      expect(mockOrderApi.post).toHaveBeenCalledWith('/admin/returns/return-123/refund?processedBy=admin-1');
    });
  });

  describe('getAnalytics', () => {
    it('should get analytics successfully', async () => {
      const mockAnalytics: ReturnAnalytics = {
        totalReturns: 100,
        totalReturnValue: 5000.00,
        averageReturnProcessingTime: 5.5,
        returnRate: 0.1,
        returnReasonsDistribution: {},
        returnRateByProduct: [],
        returnsByStatus: {},
        returnsByMonth: [],
      };

      mockOrderApi.get.mockResolvedValue({ data: mockAnalytics });

      const result = await returnService.getAnalytics();

      expect(mockOrderApi.get).toHaveBeenCalledWith('/admin/returns/analytics');
      expect(result).toEqual(mockAnalytics);
    });
  });
});

