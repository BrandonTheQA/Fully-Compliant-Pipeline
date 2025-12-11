import React from 'react';
import { ProductList } from '../components/ProductList';
import { ShippingBanner } from '../components/ShippingBanner';
import { useAppContext } from '../context/AppContext';
import './ProductsPage.css';

export const CustomerProductsPage: React.FC = () => {
  const { cart, shippingRegion, freeShippingThreshold } = useAppContext();

  const cartTotal = cart.reduce(
    (sum, item) => sum + item.price * item.orderQuantity,
    0
  );

  return (
    <div className="page-container">
      <div className="products-page-header">
        <h1>Products</h1>
      </div>

      {cart.length > 0 && shippingRegion && freeShippingThreshold && (
        <ShippingBanner
          cartTotal={cartTotal}
          region={shippingRegion}
          threshold={freeShippingThreshold}
        />
      )}

      <ProductList showActions={true} />
    </div>
  );
};
