import React from 'react';
import { useAppContext } from '../context/AppContext';
import './WishlistPage.css';

export const WishlistPage: React.FC = () => {
  const { wishlist, addToCart, removeFromWishlist, user } = useAppContext();

  if (!user) {
    return <div className="wishlist-page">Please log in to view your wishlist.</div>;
  }

  if (wishlist.length === 0) {
    return <div className="wishlist-page empty">Your wishlist is empty.</div>;
  }

  const handleMoveToCart = async (product: any) => {
    addToCart(product, 1);
    await removeFromWishlist(product.id);
  };

  return (
    <div className="wishlist-page">
      <h2>My Wishlist</h2>
      <div className="wishlist-grid">
        {wishlist.map((product) => (
          <div key={product.id} className="wishlist-card">
            <h3>{product.name}</h3>
            <p className="wishlist-price">${product.price.toFixed(2)}</p>
            <div className="wishlist-actions">
              <button 
                className="btn btn-primary"
                onClick={() => handleMoveToCart(product)}
              >
                Move to Cart
              </button>
              <button 
                className="btn btn-danger"
                onClick={() => removeFromWishlist(product.id)}
              >
                Remove
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
