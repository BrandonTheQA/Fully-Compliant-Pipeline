import React, { useState, useEffect } from 'react';
import { productService } from '../services/productService';
import { returnService } from '../services/returnService';
import type { Product, ExchangeRequest } from '../types';
import './ExchangeForm.css';

interface ExchangeFormProps {
  returnId: string;
  onExchangeCreated?: (orderId: string) => void;
}

export const ExchangeForm: React.FC<ExchangeFormProps> = ({
  returnId,
  onExchangeCreated,
}) => {
  const [products, setProducts] = useState<Product[]>([]);
  const [selectedProductId, setSelectedProductId] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    try {
      const productsData = await productService.getAllProducts();
      setProducts(productsData);
    } catch (err) {
      setError('Failed to load products');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!selectedProductId) {
      setError('Please select a product for exchange');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const exchangeRequest: ExchangeRequest = {
        exchangeProductId: selectedProductId,
        quantity: quantity,
        notes: notes,
      };

      const exchangeOrder = await returnService.createExchange(returnId, exchangeRequest);
      if (onExchangeCreated) {
        onExchangeCreated(exchangeOrder.id);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create exchange');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="exchange-form">
      <h3>Exchange Item</h3>

      {error && <div className="error-message">{error}</div>}

      <label>
        Select Product:
        <select
          value={selectedProductId}
          onChange={(e) => setSelectedProductId(e.target.value)}
          required
        >
          <option value="">Select a product...</option>
          {products.map((product) => (
            <option key={product.id} value={product.id}>
              {product.name} - ${product.price.toFixed(2)}
            </option>
          ))}
        </select>
      </label>

      <label>
        Quantity:
        <input
          type="number"
          min="1"
          value={quantity}
          onChange={(e) => setQuantity(parseInt(e.target.value) || 1)}
          required
        />
      </label>

      <label>
        Notes (Optional):
        <textarea
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          rows={3}
        />
      </label>

      <button type="submit" className="btn btn-primary" disabled={loading}>
        {loading ? 'Processing...' : 'Create Exchange Order'}
      </button>
    </form>
  );
};

