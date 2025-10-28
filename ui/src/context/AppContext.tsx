import React, { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import type { User, Product, CartItem } from '../types';

interface AppContextType {
  user: User | null;
  cart: CartItem[];
  products: Product[];
  setUser: (user: User | null) => void;
  addToCart: (product: Product, quantity: number) => void;
  removeFromCart: (productId: string) => void;
  updateCartQuantity: (productId: string, quantity: number) => void;
  clearCart: () => void;
  setProducts: (products: Product[]) => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const useAppContext = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useAppContext must be used within AppProvider');
  }
  return context;
};

interface AppProviderProps {
  children: ReactNode;
}

export const AppProvider: React.FC<AppProviderProps> = ({ children }) => {
  const [user, setUserState] = useState<User | null>(() => {
    const savedUser = sessionStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });

  const [cart, setCart] = useState<CartItem[]>(() => {
    const savedCart = sessionStorage.getItem('cart');
    return savedCart ? JSON.parse(savedCart) : [];
  });

  const [products, setProducts] = useState<Product[]>([]);

  const setUser = (user: User | null) => {
    setUserState(user);
    if (user) {
      sessionStorage.setItem('user', JSON.stringify(user));
    } else {
      sessionStorage.removeItem('user');
    }
  };

  useEffect(() => {
    if (cart.length > 0) {
      sessionStorage.setItem('cart', JSON.stringify(cart));
    } else {
      sessionStorage.removeItem('cart');
    }
  }, [cart]);

  const addToCart = (product: Product, quantity: number) => {
    setCart((prevCart) => {
      const existingItem = prevCart.find((item) => item.id === product.id);
      if (existingItem) {
        return prevCart.map((item) =>
          item.id === product.id
            ? { ...item, orderQuantity: item.orderQuantity + quantity }
            : item
        );
      }
      return [...prevCart, { ...product, orderQuantity: quantity }];
    });
  };

  const removeFromCart = (productId: string) => {
    setCart((prevCart) => prevCart.filter((item) => item.id !== productId));
  };

  const updateCartQuantity = (productId: string, quantity: number) => {
    if (quantity <= 0) {
      removeFromCart(productId);
      return;
    }
    setCart((prevCart) =>
      prevCart.map((item) =>
        item.id === productId ? { ...item, orderQuantity: quantity } : item
      )
    );
  };

  const clearCart = () => {
    setCart([]);
    sessionStorage.removeItem('cart');
  };

  const value: AppContextType = {
    user,
    cart,
    products,
    setUser,
    addToCart,
    removeFromCart,
    updateCartQuantity,
    clearCart,
    setProducts,
  };

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
};

