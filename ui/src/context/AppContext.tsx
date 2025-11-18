import React, { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react';
import type { ReactNode } from 'react';
import type { User, Product, CartItem, RecommendationResponse } from '../types';
import { shippingService } from '../services/shippingService';

interface AppContextType {
  user: User | null;
  cart: CartItem[];
  products: Product[];
  shippingRegion: string | null;
  freeShippingThreshold: number | null;
  shippingCost: number | null;
  defaultShippingCost: number | null;
  recommendations: RecommendationResponse | null;
  loadingRecommendations: boolean;
  setUser: (user: User | null) => void;
  addToCart: (product: Product, quantity: number) => void;
  removeFromCart: (productId: string) => void;
  updateCartQuantity: (productId: string, quantity: number) => void;
  clearCart: () => void;
  setProducts: (products: Product[]) => void;
  updateShippingInfo: () => Promise<void>;
  updateRecommendations: () => Promise<void>;
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

  const [shippingCost, setShippingCost] = useState<number | null>(() => {
    if (typeof window !== 'undefined' && window.sessionStorage) {
      const savedCost = sessionStorage.getItem('shippingCost');
      return savedCost ? parseFloat(savedCost) : null;
    }
    return null;
  });

  const [defaultShippingCost, setDefaultShippingCost] = useState<number | null>(() => {
    if (typeof window !== 'undefined' && window.sessionStorage) {
      const savedDefaultCost = sessionStorage.getItem('defaultShippingCost');
      return savedDefaultCost ? parseFloat(savedDefaultCost) : null;
    }
    return null;
  });

  const [recommendations, setRecommendations] = useState<RecommendationResponse | null>(null);
  const [loadingRecommendations, setLoadingRecommendations] = useState<boolean>(false);
  const recommendationsDebounceTimer = useRef<NodeJS.Timeout | null>(null);

  const updateShippingInfo = useCallback(async () => {
    try {
      // Calculate current cart total
      const cartTotal = cart.reduce(
        (sum, item) => sum + item.price * item.orderQuantity,
        0
      );
      
      // Fetch shipping cost from API (includes threshold info)
      const costData = await shippingService.getShippingCost(
        cartTotal,
        shippingRegion || undefined
      );
      
      setShippingRegion(costData.region);
      setFreeShippingThreshold(costData.freeShippingThreshold);
      setShippingCost(costData.shippingCost);
      setDefaultShippingCost(costData.defaultShippingCost);
      
      // Persist to sessionStorage
      if (typeof window !== 'undefined' && window.sessionStorage) {
        sessionStorage.setItem('shippingRegion', costData.region);
        sessionStorage.setItem('freeShippingThreshold', costData.freeShippingThreshold.toString());
        sessionStorage.setItem('shippingCost', costData.shippingCost.toString());
        sessionStorage.setItem('defaultShippingCost', costData.defaultShippingCost.toString());
      }
    } catch (error) {
      // If API call fails, use fallback values
      const fallbackRegion = shippingRegion || 'US';
      const fallbackThreshold = 50.00;
      const fallbackShippingCost = 5.99;
      const cartTotal = cart.reduce(
        (sum, item) => sum + item.price * item.orderQuantity,
        0
      );
      const fallbackCost = cartTotal >= fallbackThreshold ? 0 : fallbackShippingCost;
      
      setShippingRegion(fallbackRegion);
      setFreeShippingThreshold(fallbackThreshold);
      setShippingCost(fallbackCost);
      setDefaultShippingCost(fallbackShippingCost);
      
      if (typeof window !== 'undefined' && window.sessionStorage) {
        sessionStorage.setItem('shippingRegion', fallbackRegion);
        sessionStorage.setItem('freeShippingThreshold', fallbackThreshold.toString());
        sessionStorage.setItem('shippingCost', fallbackCost.toString());
        sessionStorage.setItem('defaultShippingCost', fallbackShippingCost.toString());
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

  // Update recommendations when cart or shipping info changes (debounced)
  const updateRecommendations = useCallback(async () => {
    // Clear existing timer
    if (recommendationsDebounceTimer.current) {
      clearTimeout(recommendationsDebounceTimer.current);
    }

    // Set loading state immediately
    setLoadingRecommendations(true);

    // Debounce the API call by 200ms
    recommendationsDebounceTimer.current = setTimeout(async () => {
      try {
        // Calculate current cart total
        const cartTotal = cart.reduce(
          (sum, item) => sum + item.price * item.orderQuantity,
          0
        );

        // Get cart item IDs
        const cartItemIds = cart.map((item) => item.id);

        // Fetch recommendations
        const recommendationsData = await shippingService.getShippingRecommendations(
          cartTotal,
          cartItemIds.length > 0 ? cartItemIds : undefined,
          shippingRegion || undefined,
          user?.userId
        );

        setRecommendations(recommendationsData);
      } catch (error) {
        console.warn('Failed to fetch recommendations:', error);
        setRecommendations(null);
      } finally {
        setLoadingRecommendations(false);
      }
    }, 200);
  }, [cart, shippingRegion, user]);

  // Update recommendations when cart or shipping info changes
  useEffect(() => {
    updateRecommendations();

    // Cleanup timer on unmount
    return () => {
      if (recommendationsDebounceTimer.current) {
        clearTimeout(recommendationsDebounceTimer.current);
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cart, shippingRegion, freeShippingThreshold]); // Update when cart or shipping info changes

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
    shippingCost,
    defaultShippingCost,
    recommendations,
    loadingRecommendations,
    setUser,
    addToCart,
    removeFromCart,
    updateCartQuantity,
    clearCart,
    setProducts,
    updateShippingInfo,
    updateRecommendations,
  };

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
};

