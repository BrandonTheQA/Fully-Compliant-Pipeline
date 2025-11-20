import React, { useEffect, useState, useMemo } from 'react';
import { shippingService, type ShippingCostResponse } from '../services/shippingService';
import type { Product } from '../types';
import { ShippingBanner } from './ShippingBanner';
import './ProductShippingPreview.css';

interface ProductShippingPreviewProps {
  product: Product;
  region?: string | null;
}

export const ProductShippingPreview: React.FC<ProductShippingPreviewProps> = React.memo(({
  product,
  region,
}) => {
  const [shippingData, setShippingData] = useState<ShippingCostResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Memoize product price to prevent unnecessary re-fetches
  const productPrice = useMemo(() => product.price, [product.price]);
  const regionValue = useMemo(() => region || undefined, [region]);

  useEffect(() => {
    let isMounted = true;
    let timeoutId: NodeJS.Timeout | null = null;

    const fetchShippingCost = async () => {
      setLoading(true);
      setError(null);

      try {
        // Use cached shipping service which already implements caching
        const data = await shippingService.getShippingCost(
          productPrice,
          regionValue
        );

        if (isMounted) {
          setShippingData(data);
          setLoading(false);
        }
      } catch (err) {
        if (isMounted) {
          setError(err instanceof Error ? err.message : 'Failed to load shipping cost');
          setLoading(false);
          // Set fallback data for graceful degradation
          setShippingData({
            region: regionValue || 'US',
            cartTotal: productPrice,
            shippingCost: productPrice >= 50 ? 0 : 5.99,
            freeShippingThreshold: 50.00,
            remainingAmount: Math.max(0, 50.00 - productPrice),
            qualifiesForFreeShipping: productPrice >= 50.00,
            defaultShippingCost: 5.99,
          });
        }
      }
    };

    // Small debounce to batch rapid changes (e.g., region changes)
    timeoutId = setTimeout(() => {
      fetchShippingCost();
    }, 100);

    return () => {
      isMounted = false;
      if (timeoutId) {
        clearTimeout(timeoutId);
      }
    };
  }, [productPrice, regionValue]);

  // Don't render anything if still loading and no data
  if (loading && !shippingData) {
    return (
      <div className="product-shipping-preview loading">
        <div className="shipping-loading-text">Calculating shipping...</div>
      </div>
    );
  }

  // Graceful degradation: show fallback message if error and no data
  if (error && !shippingData) {
    return (
      <div className="product-shipping-preview error">
        <div className="shipping-fallback-text">Shipping cost calculated at checkout</div>
      </div>
    );
  }

  if (!shippingData) {
    return null;
  }

  const {
    shippingCost,
    qualifiesForFreeShipping,
    freeShippingThreshold,
    remainingAmount,
    region: detectedRegion,
  } = shippingData;

  return (
    <div className="product-shipping-preview">
      <div className="shipping-preview-header">
        <span className="shipping-region-indicator">Shipping to {detectedRegion}</span>
      </div>

      <div className={`shipping-cost-display ${qualifiesForFreeShipping ? 'shipping-free' : ''}`}>
        <span className="shipping-cost-label">Estimated Shipping:</span>
        <span className="shipping-cost-value">
          {qualifiesForFreeShipping ? (
            <>
              <span className="shipping-free-text">FREE</span>
              <span className="shipping-free-icon">🎉</span>
            </>
          ) : (
            `$${shippingCost.toFixed(2)}`
          )}
        </span>
      </div>

      {!qualifiesForFreeShipping && (
        <div className="shipping-threshold-indicator">
          <ShippingBanner
            cartTotal={product.price}
            region={detectedRegion}
            threshold={freeShippingThreshold}
          />
        </div>
      )}

      {qualifiesForFreeShipping && (
        <div className="shipping-free-message">
          This item qualifies for FREE shipping!
        </div>
      )}
    </div>
  );
}, (prevProps, nextProps) => {
  // Custom comparison function for React.memo
  // Only re-render if product price or region changes
  return (
    prevProps.product.price === nextProps.product.price &&
    prevProps.product.id === nextProps.product.id &&
    prevProps.region === nextProps.region
  );
});

