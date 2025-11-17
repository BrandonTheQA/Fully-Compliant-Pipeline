import React, { useState } from 'react';
import { ProductList } from '../components/ProductList';
import { ProductForm } from '../components/ProductForm';
import { ShippingBanner } from '../components/ShippingBanner';
import { useAppContext } from '../context/AppContext';
import './ProductsPage.css';

export const ProductsPage: React.FC = () => {
  const [showCreateForm, setShowCreateForm] = useState(false);
  const { cart, shippingRegion, freeShippingThreshold } = useAppContext();

  const cartTotal = cart.reduce(
    (sum, item) => sum + item.price * item.orderQuantity,
    0
  );

  return (
    <div className="page-container">
      <div className="products-page-header">
        <h1>Products</h1>
        <button
          onClick={() => setShowCreateForm(!showCreateForm)}
          className="btn btn-primary"
        >
          {showCreateForm ? 'Hide Create Form' : 'Create New Product'}
        </button>
      </div>

      {showCreateForm && (
        <div className="create-product-section">
          <ProductForm />
        </div>
      )}

      {cart.length > 0 && shippingRegion && freeShippingThreshold && (
        <ShippingBanner
          cartTotal={cartTotal}
          region={shippingRegion}
          threshold={freeShippingThreshold}
        />
      )}

      <ProductList />
    </div>
  );
};

