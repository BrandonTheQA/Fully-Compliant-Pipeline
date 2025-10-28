import { userApi } from './api';
import type { User, CreateUserRequest } from '../types';

export const userService = {
  createUser: async (userData: CreateUserRequest): Promise<User> => {
    const response = await userApi.post<User>('/users', userData);
    return response.data;
  },

  getUser: async (userId: string): Promise<User> => {
    const response = await userApi.get<User>(`/users/${userId}`);
    return response.data;
  },
};

