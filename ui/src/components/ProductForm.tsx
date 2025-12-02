import React, { useState } from 'react';
import { useAppContext } from '../context/AppContext';
import { productService } from '../services/productService';
import type { CreateProductRequest } from '../types';
import './ProductForm.css';

export const ProductForm: React.FC = () => {
  const { setProducts, products } = useAppContext();
  const [formData, setFormData] = useState<CreateProductRequest>({
    name: '',
    description: '',
    price: 0,
    quantity: 0,
    category: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);
    setLoading(true);

    try {
      // Validate form
      if (!formData.name.trim()) {
        throw new Error('Product name is required');
      }
      if (!formData.description.trim()) {
        throw new Error('Description is required');
      }
      if (formData.price <= 0) {
        throw new Error('Price must be greater than 0');
      }
      if (formData.quantity < 0) {
        throw new Error('Quantity cannot be negative');
      }
      if (!formData.category.trim()) {
        throw new Error('Category is required');
      }

      const newProduct = await productService.createProduct(formData);
      setProducts([...products, newProduct]);
      setSuccess(true);
      setFormData({
        name: '',
        description: '',
        price: 0,
        quantity: 0,
        category: '',
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create product');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'price' || name === 'quantity' ? parseFloat(value) || 0 : value,
    }));
  };

  return (
    <div className="product-form-container">
      <h2>Create New Product</h2>
      <form onSubmit={handleSubmit} className="product-form">
        <div className="form-group">
          <label htmlFor="name">Product Name</label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
            disabled={loading}
            aria-describedby={error && (error.includes('name') || error.includes('Name')) ? 'product-error' : undefined}
          />
        </div>

        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            required
            disabled={loading}
            rows={3}
            aria-describedby={error && error.includes('Description') ? 'product-error' : undefined}
          />
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="price">Price</label>
            <input
              type="number"
              id="price"
              name="price"
              value={formData.price}
              onChange={handleChange}
              required
              min="0"
              step="0.01"
              disabled={loading}
              aria-describedby={error && error.includes('Price') ? 'product-error' : undefined}
            />
          </div>

          <div className="form-group">
            <label htmlFor="quantity">Quantity</label>
            <input
              type="number"
              id="quantity"
              name="quantity"
              value={formData.quantity}
              onChange={handleChange}
              required
              min="0"
              disabled={loading}
              aria-describedby={error && error.includes('Quantity') ? 'product-error' : undefined}
            />
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="category">Category</label>
          <input
            type="text"
            id="category"
            name="category"
            value={formData.category}
            onChange={handleChange}
            required
            disabled={loading}
            aria-describedby={error && error.includes('Category') ? 'product-error' : undefined}
          />
        </div>

        {error && (
          <div id="product-error" className="error-message" role="alert">
            {error}
          </div>
        )}
        {success && (
          <div className="success-message" role="alert" aria-live="polite">
            Product created successfully!
          </div>
        )}

        <button type="submit" disabled={loading} className="btn btn-primary">
          {loading ? 'Creating...' : 'Create Product'}
        </button>
      </form>
    </div>
  );
};

