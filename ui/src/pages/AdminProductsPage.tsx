import React, { useState } from 'react';
import { ProductList } from '../components/ProductList';
import { ProductForm } from '../components/ProductForm';
import './ProductsPage.css';

export const AdminProductsPage: React.FC = () => {
  const [showCreateForm, setShowCreateForm] = useState(false);

  return (
    <div className="page-container">
      <div className="products-page-header">
        <h1>Admin - Product Management</h1>
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

      <ProductList showActions={false} />
    </div>
  );
};
