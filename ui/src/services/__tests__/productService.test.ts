import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { productService } from '../productService';
import { productApi } from '../api';
import type { Product, CreateProductRequest } from '../../types';

jest.mock('../api');

describe('productService', () => {
  const mockProductApi = productApi as jest.Mocked<typeof productApi>;

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('getAllProducts', () => {
    it('should get all products successfully', async () => {
      const mockProducts: Product[] = [
        {
          id: '1',
          name: 'Laptop',
          description: 'High-performance laptop',
          price: 999.99,
          quantity: 10,
          category: 'Electronics',
        },
      ];

      mockProductApi.get.mockResolvedValue({ data: mockProducts });

      const result = await productService.getAllProducts();

      expect(mockProductApi.get).toHaveBeenCalledWith('/products');
      expect(result).toEqual(mockProducts);
    });

    it('should handle errors when getting products', async () => {
      const error = new Error('Failed to fetch products');
      mockProductApi.get.mockRejectedValue(error);

      await expect(productService.getAllProducts()).rejects.toThrow('Failed to fetch products');
    });
  });

  describe('getProduct', () => {
    it('should get a product successfully', async () => {
      const productId = '1';
      const mockProduct: Product = {
        id: '1',
        name: 'Laptop',
        description: 'High-performance laptop',
        price: 999.99,
        quantity: 10,
        category: 'Electronics',
      };

      mockProductApi.get.mockResolvedValue({ data: mockProduct });

      const result = await productService.getProduct(productId);

      expect(mockProductApi.get).toHaveBeenCalledWith('/products/1');
      expect(result).toEqual(mockProduct);
    });
  });

  describe('createProduct', () => {
    it('should create a product successfully', async () => {
      const productData: CreateProductRequest = {
        name: 'Laptop',
        description: 'High-performance laptop',
        price: 999.99,
        quantity: 10,
        category: 'Electronics',
      };

      const mockProduct: Product = {
        id: '1',
        ...productData,
      };

      mockProductApi.post.mockResolvedValue({ data: mockProduct });

      const result = await productService.createProduct(productData);

      expect(mockProductApi.post).toHaveBeenCalledWith('/products', productData);
      expect(result).toEqual(mockProduct);
    });
  });
});

