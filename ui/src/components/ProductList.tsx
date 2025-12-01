import React, { useEffect, useState } from 'react';
import { useAppContext } from '../context/AppContext';
import { productService } from '../services/productService';
import type { Product } from '../types';
import { ProductShippingPreview } from './ProductShippingPreview';
import { WishlistButton } from './WishlistButton';
import './ProductList.css';

interface ProductListProps {
  onAddToCart?: (product: Product) => void;
  showActions?: boolean;
}

export const ProductList: React.FC<ProductListProps> = ({ 
  onAddToCart, 
  showActions = true 
}) => {
  const { products, setProducts, addToCart, shippingRegion } = useAppContext();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    setLoading(true);
    setError(null);
    try {
      const fetchedProducts = await productService.getAllProducts();
      setProducts(fetchedProducts);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load products');
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = (product: Product) => {
    if (onAddToCart) {
      onAddToCart(product);
    } else {
      addToCart(product, 1);
    }
  };

  if (loading) {
    return <div className="loading">Loading products...</div>;
  }

  if (error) {
    return (
      <div className="error-message">
        {error}
        <button onClick={loadProducts} className="btn btn-secondary">
          Retry
        </button>
      </div>
    );
  }

  if (products.length === 0) {
    return <div className="no-products">No products available. Create one to get started!</div>;
  }

  return (
    <div className="product-list">
      <h2>Products</h2>
      <div className="products-grid">
        {products.map((product) => (
          <div key={product.id} className="product-card">
            <div className="product-header" style={{display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start'}}>
              <h3>{product.name}</h3>
              {showActions && <WishlistButton product={product} />}
            </div>
            <p className="product-description">{product.description}</p>
            <div className="product-details">
              <span className="product-price">${product.price.toFixed(2)}</span>
              <span className="product-category">{product.category}</span>
              <span className="product-quantity">Stock: {product.quantity}</span>
            </div>
            <ProductShippingPreview 
              key={`${product.id}-${shippingRegion || 'default'}`}
              product={product} 
              region={shippingRegion} 
            />
            {showActions && (
              <button
                onClick={() => handleAddToCart(product)}
                className="btn btn-primary"
                disabled={product.quantity === 0}
              >
                Add to Cart
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

