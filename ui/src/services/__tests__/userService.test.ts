import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { userService } from '../userService';
import { userApi } from '../api';
import type { User, CreateUserRequest } from '../../types';

jest.mock('../api');

describe('userService', () => {
  const mockUserApi = userApi as jest.Mocked<typeof userApi>;

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('createUser', () => {
    it('should create a user successfully', async () => {
      const userData: CreateUserRequest = {
        name: 'John Doe',
        email: 'john@example.com',
        password: 'password123',
      };

      const mockUser: User = {
        userId: '123',
        name: 'John Doe',
        email: 'john@example.com',
        createdAt: '2024-01-01T00:00:00',
      };

      mockUserApi.post.mockResolvedValue({ data: mockUser });

      const result = await userService.createUser(userData);

      expect(mockUserApi.post).toHaveBeenCalledWith('/users', userData);
      expect(result).toEqual(mockUser);
    });

    it('should handle errors when creating user', async () => {
      const userData: CreateUserRequest = {
        name: 'John Doe',
        email: 'john@example.com',
        password: 'password123',
      };

      const error = new Error('Failed to create user');
      mockUserApi.post.mockRejectedValue(error);

      await expect(userService.createUser(userData)).rejects.toThrow('Failed to create user');
    });
  });

  describe('getUser', () => {
    it('should get a user successfully', async () => {
      const userId = '123';
      const mockUser: User = {
        userId: '123',
        name: 'John Doe',
        email: 'john@example.com',
        createdAt: '2024-01-01T00:00:00',
      };

      mockUserApi.get.mockResolvedValue({ data: mockUser });

      const result = await userService.getUser(userId);

      expect(mockUserApi.get).toHaveBeenCalledWith('/users/123');
      expect(result).toEqual(mockUser);
    });

    it('should handle errors when getting user', async () => {
      const userId = '123';
      const error = new Error('User not found');
      mockUserApi.get.mockRejectedValue(error);

      await expect(userService.getUser(userId)).rejects.toThrow('User not found');
    });
  });
});

