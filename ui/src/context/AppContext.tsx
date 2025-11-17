import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import type { ReactNode } from 'react';
import type { User, Product, CartItem } from '../types';
import { shippingService } from '../services/shippingService';

interface AppContextType {
  user: User | null;
  cart: CartItem[];
  products: Product[];
  shippingRegion: string | null;
  freeShippingThreshold: number | null;
  setUser: (user: User | null) => void;
  addToCart: (product: Product, quantity: number) => void;
  removeFromCart: (productId: string) => void;
  updateCartQuantity: (productId: string, quantity: number) => void;
  clearCart: () => void;
  setProducts: (products: Product[]) => void;
  updateShippingInfo: () => Promise<void>;
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
  
  const [shippingRegion, setShippingRegion] = useState<string | null>(() => {
    if (typeof window !== 'undefined' && window.sessionStorage) {
      const savedRegion = sessionStorage.getItem('shippingRegion');
      return savedRegion || null;
    }
    return null;
  });
  
  const [freeShippingThreshold, setFreeShippingThreshold] = useState<number | null>(() => {
    if (typeof window !== 'undefined' && window.sessionStorage) {
      const savedThreshold = sessionStorage.getItem('freeShippingThreshold');
      return savedThreshold ? parseFloat(savedThreshold) : null;
    }
    return null;
  });

  const updateShippingInfo = useCallback(async () => {
    try {
      // Calculate current cart total
      const cartTotal = cart.reduce(
        (sum, item) => sum + item.price * item.orderQuantity,
        0
      );
      
      // Fetch shipping threshold from API
      const thresholdData = await shippingService.getShippingThreshold(
        cartTotal,
        shippingRegion || undefined
      );
      
      setShippingRegion(thresholdData.region);
      setFreeShippingThreshold(thresholdData.freeShippingThreshold);
      
      // Persist to sessionStorage
      if (typeof window !== 'undefined' && window.sessionStorage) {
        sessionStorage.setItem('shippingRegion', thresholdData.region);
        sessionStorage.setItem('freeShippingThreshold', thresholdData.freeShippingThreshold.toString());
      }
    } catch (error) {
      // If API call fails, use fallback values
      const fallbackRegion = shippingRegion || 'US';
      const fallbackThreshold = 50.00;
      
      setShippingRegion(fallbackRegion);
      setFreeShippingThreshold(fallbackThreshold);
      
      if (typeof window !== 'undefined' && window.sessionStorage) {
        sessionStorage.setItem('shippingRegion', fallbackRegion);
        sessionStorage.setItem('freeShippingThreshold', fallbackThreshold.toString());
      }
    }
  }, [cart, shippingRegion]);

  // Initialize shipping info on app load
  useEffect(() => {
    updateShippingInfo();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Only run on mount

  // Update shipping info when cart changes
  useEffect(() => {
    if (cart.length > 0 || shippingRegion) {
      updateShippingInfo();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cart]); // Update when cart changes

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
    shippingRegion,
    freeShippingThreshold,
    setUser,
    addToCart,
    removeFromCart,
    updateCartQuantity,
    clearCart,
    setProducts,
    updateShippingInfo,
  };

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
};

