import React, { useState } from 'react';
import type { RecommendationResponse, RecommendedProduct, OptimizationPath } from '../types';
import './ShippingRecommendations.css';

interface ShippingRecommendationsProps {
  recommendations: RecommendationResponse | null;
  loading?: boolean;
  onAddToCart: (productId: string) => void;
}

export const ShippingRecommendations: React.FC<ShippingRecommendationsProps> = ({
  recommendations,
  loading = false,
  onAddToCart,
}) => {
  const [selectedPathIndex, setSelectedPathIndex] = useState<number>(0);

  // Don't render if already qualifies for free shipping or no recommendations
  if (!recommendations || recommendations.qualifiesForFreeShipping || recommendations.optimizationPaths.length === 0) {
    return null;
  }

  if (loading) {
    return (
      <div className="shipping-recommendations">
        <div className="shipping-recommendations-loading">Loading recommendations...</div>
      </div>
    );
  }

  const handleAddProduct = (productId: string) => {
    onAddToCart(productId);
  };

  const handleAddAllFromPath = (path: OptimizationPath) => {
    path.products.forEach((product) => {
      handleAddProduct(product.id);
    });
  };

  return (
    <div className="shipping-recommendations">
      <div className="shipping-recommendations-header">
        <h3>Get FREE Shipping!</h3>
        <p className="shipping-recommendations-subtitle">
          Add ${recommendations.remainingAmount.toFixed(2)} more to qualify for free shipping
        </p>
      </div>

      {recommendations.optimizationPaths.length === 1 ? (
        // Single path - show directly
        <div className="shipping-recommendations-single-path">
          <OptimizationPathCard
            path={recommendations.optimizationPaths[0]}
            onAddProduct={handleAddProduct}
            onAddAll={handleAddAllFromPath}
          />
        </div>
      ) : (
        // Multiple paths - show with tabs/accordion
        <div className="shipping-recommendations-paths">
          <div className="shipping-recommendations-tabs">
            {recommendations.optimizationPaths.map((path, index) => (
              <button
                key={index}
                className={`shipping-recommendations-tab ${selectedPathIndex === index ? 'active' : ''}`}
                onClick={() => setSelectedPathIndex(index)}
                aria-selected={selectedPathIndex === index}
                role="tab"
              >
                {path.pathType === 'single' ? 'Single Product' : path.pathType === 'bundle' ? 'Bundle' : 'Category'}
              </button>
            ))}
          </div>

          <div className="shipping-recommendations-tab-content">
            {recommendations.optimizationPaths.map((path, index) => (
              <div
                key={index}
                className={`shipping-recommendations-path-panel ${selectedPathIndex === index ? 'active' : ''}`}
                role="tabpanel"
                hidden={selectedPathIndex !== index}
              >
                <OptimizationPathCard
                  path={path}
                  onAddProduct={handleAddProduct}
                  onAddAll={handleAddAllFromPath}
                />
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

interface OptimizationPathCardProps {
  path: OptimizationPath;
  onAddProduct: (productId: string) => void;
  onAddAll: (path: OptimizationPath) => void;
}

const OptimizationPathCard: React.FC<OptimizationPathCardProps> = ({
  path,
  onAddProduct,
  onAddAll,
}) => {
  return (
    <div className="optimization-path-card">
      <div className="optimization-path-header">
        <div className="optimization-path-message">{path.message}</div>
        <div className="optimization-path-savings">
          Save ${path.savingsAmount.toFixed(2)} on shipping
        </div>
      </div>

      <div className="optimization-path-products">
        {path.products.map((product) => (
          <ProductRecommendationCard
            key={product.id}
            product={product}
            onAddToCart={() => onAddProduct(product.id)}
          />
        ))}
      </div>

      {path.products.length > 1 && (
        <div className="optimization-path-actions">
          <button
            className="btn btn-primary optimization-path-add-all"
            onClick={() => onAddAll(path)}
          >
            Add All (${path.totalCost.toFixed(2)})
          </button>
        </div>
      )}
    </div>
  );
};

interface ProductRecommendationCardProps {
  product: RecommendedProduct;
  onAddToCart: () => void;
}

const ProductRecommendationCard: React.FC<ProductRecommendationCardProps> = ({
  product,
  onAddToCart,
}) => {
  return (
    <div className="product-recommendation-card">
      {product.imageUrl && (
        <div className="product-recommendation-image">
          <img src={product.imageUrl} alt={product.name} />
        </div>
      )}
      <div className="product-recommendation-details">
        <div className="product-recommendation-name">{product.name}</div>
        {product.description && (
          <div className="product-recommendation-description">{product.description}</div>
        )}
        {product.category && (
          <div className="product-recommendation-category">{product.category}</div>
        )}
        {product.savingsMessage && (
          <div className="product-recommendation-savings">{product.savingsMessage}</div>
        )}
        <div className="product-recommendation-price">${product.price.toFixed(2)}</div>
        <button
          className="btn btn-primary product-recommendation-add-btn"
          onClick={onAddToCart}
        >
          Add to Cart
        </button>
      </div>
    </div>
  );
};

