import React from 'react';
import { useAppContext } from '../context/AppContext';
import { Product } from '../types';
import './WishlistButton.css';

interface WishlistButtonProps {
  product: Product;
}

export const WishlistButton: React.FC<WishlistButtonProps> = ({ product }) => {
  const { user, wishlist, addToWishlist, removeFromWishlist } = useAppContext();
  const isInWishlist = wishlist.some((item) => item.id === product.id);

  if (!user) return null;

  const handleClick = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (isInWishlist) {
      await removeFromWishlist(product.id);
    } else {
      await addToWishlist(product);
    }
  };

  return (
    <button 
      className={`wishlist-button ${isInWishlist ? 'active' : ''}`} 
      onClick={handleClick}
      aria-label={isInWishlist ? "Remove from wishlist" : "Add to wishlist"}
    >
      {isInWishlist ? '♥' : '♡'}
    </button>
  );
};
