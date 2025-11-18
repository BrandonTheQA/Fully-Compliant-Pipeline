/// <reference types="@testing-library/jest-dom" />
import { describe, it, expect, beforeEach } from '@jest/globals';
import { render, screen, fireEvent } from '@testing-library/react';
import { ShippingRecommendations } from '../ShippingRecommendations';
import type { RecommendationResponse, OptimizationPath, RecommendedProduct } from '../../types';

describe('ShippingRecommendations', () => {
  const mockOnAddToCart = jest.fn();
  
  beforeEach(() => {
    mockOnAddToCart.mockClear();
  });

  const createMockProduct = (id: string, name: string, price: number): RecommendedProduct => ({
    id,
    name,
    description: `Description for ${name}`,
    price,
    category: 'Electronics',
    savingsMessage: `Add this to get FREE shipping and save $5.99`,
    imageUrl: `https://example.com/images/${id}.jpg`,
  });

  const createMockPath = (
    products: RecommendedProduct[],
    pathType: 'single' | 'bundle' | 'category' = 'single'
  ): OptimizationPath => ({
    products,
    totalCost: products.reduce((sum, p) => sum + p.price, 0),
    savingsAmount: 5.99,
    message: `Add ${products.map(p => p.name).join(', ')} → FREE shipping`,
    pathType,
  });

  const createMockRecommendations = (
    paths: OptimizationPath[],
    qualifiesForFreeShipping: boolean = false,
    remainingAmount: number = 15.00
  ): RecommendationResponse => ({
    optimizationPaths: paths,
    qualifiesForFreeShipping,
    remainingAmount,
    region: 'US',
    cartTotal: 35.00,
    freeShippingThreshold: 50.00,
  });

  it('should not render when recommendations are null', () => {
    const { container } = render(
      <ShippingRecommendations
        recommendations={null}
        loading={false}
        onAddToCart={mockOnAddToCart}
      />
    );

    expect(container.firstChild).toBeNull();
  });

  it('should not render when qualifies for free shipping', () => {
    const recommendations = createMockRecommendations([], true, 0);
    const { container } = render(
      <ShippingRecommendations
        recommendations={recommendations}
        loading={false}
        onAddToCart={mockOnAddToCart}
      />
    );

    expect(container.firstChild).toBeNull();
  });

  it('should not render when no optimization paths', () => {
    const recommendations = createMockRecommendations([], false, 15.00);
    const { container } = render(
      <ShippingRecommendations
        recommendations={recommendations}
        loading={false}
        onAddToCart={mockOnAddToCart}
      />
    );

    expect(container.firstChild).toBeNull();
  });

  it('should display loading state', () => {
    // When loading, we still need recommendations object (it's just being refreshed)
    const product = createMockProduct('prod1', 'Product 1', 20.00);
    const path = createMockPath([product]);
    const recommendations = createMockRecommendations([path]);

    render(
      <ShippingRecommendations
        recommendations={recommendations}
        loading={true}
        onAddToCart={mockOnAddToCart}
      />
    );

    expect(screen.getByText(/Loading recommendations.../i)).toBeInTheDocument();
  });

  it('should display recommendations header with remaining amount', () => {
    const product = createMockProduct('prod1', 'Product 1', 20.00);
    const path = createMockPath([product]);
    const recommendations = createMockRecommendations([path]);

    render(
      <ShippingRecommendations
        recommendations={recommendations}
        loading={false}
        onAddToCart={mockOnAddToCart}
      />
    );

    expect(screen.getByText(/Get FREE Shipping!/i)).toBeInTheDocument();
    expect(screen.getByText(/Add \$15\.00 more to qualify for free shipping/i)).toBeInTheDocument();
  });

  it('should display single product recommendation', () => {
    const product = createMockProduct('prod1', 'Product 1', 20.00);
    const path = createMockPath([product]);
    const recommendations = createMockRecommendations([path]);

    render(
      <ShippingRecommendations
        recommendations={recommendations}
        loading={false}
        onAddToCart={mockOnAddToCart}
      />
    );

    expect(screen.getByText('Product 1')).toBeInTheDocument();
    expect(screen.getByText(/\$20\.00/)).toBeInTheDocument();
    expect(screen.getByText(/Add this to get FREE shipping and save \$5\.99/i)).toBeInTheDocument();
    expect(screen.getByText(/Add to Cart/i)).toBeInTheDocument();
  });

  it('should display product image when imageUrl is provided', () => {
    const product = createMockProduct('prod1', 'Product 1', 20.00);
    const path = createMockPath([product]);
    const recommendations = createMockRecommendations([path]);

    render(
      <ShippingRecommendations
        recommendations={recommendations}
        loading={false}
        onAddToCart={mockOnAddToCart}
      />
    );

    const image = screen.getByAltText('Product 1');
    expect(image).toBeInTheDocument();
    expect(image.getAttribute('src')).toBe('https://example.com/images/prod1.jpg');
  });

  it('should not display product image when imageUrl is not provided', () => {
    const product: RecommendedProduct = {
      ...createMockProduct('prod1', 'Product 1', 20.00),
      imageUrl: undefined,
    };
    const path = createMockPath([product]);
    const recommendations = createMockRecommendations([path]);

    const { container } = render(
      <ShippingRecommendations
        recommendations={recommendations}
        loading={false}
        onAddToCart={mockOnAddToCart}
      />
    );

    const image = container.querySelector('img');
    expect(image).not.toBeInTheDocument();
  });

  it('should call onAddToCart when Add to Cart button is clicked', () => {
    const product = createMockProduct('prod1', 'Product 1', 20.00);
    const path = createMockPath([product]);
    const recommendations = createMockRecommendations([path]);

    render(
      <ShippingRecommendations
        recommendations={recommendations}
        loading={false}
        onAddToCart={mockOnAddToCart}
      />
    );

    const addButton = screen.getByText(/Add to Cart/i);
    fireEvent.click(addButton);

    expect(mockOnAddToCart).toHaveBeenCalledTimes(1);
    expect(mockOnAddToCart).toHaveBeenCalledWith('prod1');
  });

  it('should display multiple paths with tabs', () => {
    const product1 = createMockProduct('prod1', 'Product 1', 20.00);
    const product2 = createMockProduct('prod2', 'Product 2', 18.00);
    const path1 = createMockPath([product1], 'single');
    const path2 = createMockPath([product2], 'single');
    const recommendations = createMockRecommendations([path1, path2]);

    render(
      <ShippingRecommendations
        recommendations={recommendations}
        loading={false}
        onAddToCart={mockOnAddToCart}
      />
    );

    // Should show tabs (may appear multiple times, so use getAllByText)
    const tabs = screen.getAllByText(/Single Product/i);
    expect(tabs.length).toBeGreaterThan(0);
    // Should show first path by default
    expect(screen.getByText('Product 1')).toBeInTheDocument();
  });

  it('should switch between tabs when clicked', () => {
    const product1 = createMockProduct('prod1', 'Product 1', 20.00);
    const product2 = createMockProduct('prod2', 'Product 2', 18.00);
    const path1 = createMockPath([product1], 'single');
    const path2 = createMockPath([product2], 'single');
    const recommendations = createMockRecommendations([path1, path2]);

    render(
      <ShippingRecommendations
        recommendations={recommendations}
        loading={false}
        onAddToCart={mockOnAddToCart}
      />
    );

    // Initially shows first product
    expect(screen.getByText('Product 1')).toBeInTheDocument();

    // Click second tab
    const tabs = screen.getAllByRole('tab');
    const secondTab = tabs.find(tab => tab.getAttribute('aria-selected') === 'false');
    if (secondTab) {
      fireEvent.click(secondTab);
      // Should now show second product
      expect(screen.getByText('Product 2')).toBeInTheDocument();
    }
  });

  it('should display bundle path correctly', () => {
    const product1 = createMockProduct('prod1', 'Product 1', 10.00);
    const product2 = createMockProduct('prod2', 'Product 2', 8.00);
    const path = createMockPath([product1, product2], 'bundle');
    const recommendations = createMockRecommendations([path]);

    render(
      <ShippingRecommendations
        recommendations={recommendations}
        loading={false}
        onAddToCart={mockOnAddToCart}
      />
    );

    expect(screen.getByText('Product 1')).toBeInTheDocument();
    expect(screen.getByText('Product 2')).toBeInTheDocument();
    expect(screen.getByText(/Add All \(\$18\.00\)/i)).toBeInTheDocument();
  });

  it('should call onAddToCart for all products when Add All is clicked', () => {
    // Reset mock to ensure clean state
    mockOnAddToCart.mockClear();
    
    const product1 = createMockProduct('prod1', 'Product 1', 10.00);
    const product2 = createMockProduct('prod2', 'Product 2', 8.00);
    const path = createMockPath([product1, product2], 'bundle');
    const recommendations = createMockRecommendations([path]);

    render(
      <ShippingRecommendations
        recommendations={recommendations}
        loading={false}
        onAddToCart={mockOnAddToCart}
      />
    );

    const addAllButton = screen.getByText(/Add All/i);
    fireEvent.click(addAllButton);

    // Should be called once for each product in the bundle
    expect(mockOnAddToCart).toHaveBeenCalledTimes(2);
    expect(mockOnAddToCart).toHaveBeenCalledWith('prod1');
    expect(mockOnAddToCart).toHaveBeenCalledWith('prod2');
  });

  it('should display savings message', () => {
    const product = createMockProduct('prod1', 'Product 1', 20.00);
    const path = createMockPath([product]);
    const recommendations = createMockRecommendations([path]);

    render(
      <ShippingRecommendations
        recommendations={recommendations}
        loading={false}
        onAddToCart={mockOnAddToCart}
      />
    );

    expect(screen.getByText(/Save \$5\.99 on shipping/i)).toBeInTheDocument();
  });

  it('should display product description when provided', () => {
    const product = createMockProduct('prod1', 'Product 1', 20.00);
    const path = createMockPath([product]);
    const recommendations = createMockRecommendations([path]);

    render(
      <ShippingRecommendations
        recommendations={recommendations}
        loading={false}
        onAddToCart={mockOnAddToCart}
      />
    );

    expect(screen.getByText(/Description for Product 1/i)).toBeInTheDocument();
  });

  it('should display product category when provided', () => {
    const product = createMockProduct('prod1', 'Product 1', 20.00);
    const path = createMockPath([product]);
    const recommendations = createMockRecommendations([path]);

    render(
      <ShippingRecommendations
        recommendations={recommendations}
        loading={false}
        onAddToCart={mockOnAddToCart}
      />
    );

    expect(screen.getByText(/Electronics/i)).toBeInTheDocument();
  });

  it('should handle empty product list gracefully', () => {
    const path = createMockPath([]);
    const recommendations = createMockRecommendations([path]);

    const { container } = render(
      <ShippingRecommendations
        recommendations={recommendations}
        loading={false}
        onAddToCart={mockOnAddToCart}
      />
    );

    // Should still render the component structure
    expect(container.querySelector('.shipping-recommendations')).toBeInTheDocument();
  });

  it('should display correct remaining amount for different values', () => {
    const product = createMockProduct('prod1', 'Product 1', 20.00);
    const path = createMockPath([product]);
    const recommendations = createMockRecommendations([path], false, 24.50);

    render(
      <ShippingRecommendations
        recommendations={recommendations}
        loading={false}
        onAddToCart={mockOnAddToCart}
      />
    );

    expect(screen.getByText(/Add \$24\.50 more to qualify for free shipping/i)).toBeInTheDocument();
  });
});

