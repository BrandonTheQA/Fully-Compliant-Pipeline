/// <reference types="@testing-library/jest-dom" />
import { describe, it, expect } from '@jest/globals';
import { render, screen } from '@testing-library/react';
import { ShippingCostCalculator } from '../ShippingCostCalculator';

describe('ShippingCostCalculator', () => {
  it('should display shipping cost when cart is below threshold', () => {
    render(
      <ShippingCostCalculator
        cartTotal={35.00}
        region="US"
        shippingCost={5.99}
        freeShippingThreshold={50.00}
        remainingAmount={15.00}
        qualifiesForFreeShipping={false}
      />
    );

    expect(screen.getByText(/Estimated Shipping:/i)).toBeInTheDocument();
    // Shipping cost appears in both estimated shipping and cost breakdown
    const shippingCostElements = screen.getAllByText(/\$5\.99/);
    expect(shippingCostElements.length).toBeGreaterThan(0);
    expect(screen.getByText(/Subtotal:/i)).toBeInTheDocument();
    expect(screen.getByText(/\$35\.00/)).toBeInTheDocument();
    // "Total:" appears in cost breakdown
    const totalLabels = screen.getAllByText(/Total:/i);
    expect(totalLabels.length).toBeGreaterThan(0);
    expect(screen.getByText(/\$40\.99/)).toBeInTheDocument();
  });

  it('should display FREE shipping when cart qualifies', () => {
    render(
      <ShippingCostCalculator
        cartTotal={60.00}
        region="US"
        shippingCost={0}
        freeShippingThreshold={50.00}
        remainingAmount={0}
        qualifiesForFreeShipping={true}
      />
    );

    expect(screen.getByText(/Estimated Shipping:/i)).toBeInTheDocument();
    // "FREE" appears in both estimated shipping and cost breakdown
    const freeTexts = screen.getAllByText(/FREE/);
    expect(freeTexts.length).toBeGreaterThan(0);
    expect(screen.getByText(/Subtotal:/i)).toBeInTheDocument();
    // $60.00 appears as both subtotal and total (when shipping is free)
    const subtotalTotalElements = screen.getAllByText(/\$60\.00/);
    expect(subtotalTotalElements.length).toBeGreaterThanOrEqual(2);
    // "Total:" appears in cost breakdown
    const totalLabels = screen.getAllByText(/Total:/i);
    expect(totalLabels.length).toBeGreaterThan(0);
  });

  it('should display shipping cost in order summary when below threshold', () => {
    render(
      <ShippingCostCalculator
        cartTotal={35.00}
        region="US"
        shippingCost={5.99}
        freeShippingThreshold={50.00}
        remainingAmount={15.00}
        qualifiesForFreeShipping={false}
      />
    );

    const shippingRows = screen.getAllByText(/Shipping:/i);
    expect(shippingRows.length).toBeGreaterThan(0);
    // Shipping cost appears in both estimated shipping and cost breakdown
    const shippingCostElements = screen.getAllByText(/\$5\.99/);
    expect(shippingCostElements.length).toBeGreaterThan(0);
  });

  it('should display FREE in order summary when qualifies for free shipping', () => {
    render(
      <ShippingCostCalculator
        cartTotal={60.00}
        region="US"
        shippingCost={0}
        freeShippingThreshold={50.00}
        remainingAmount={0}
        qualifiesForFreeShipping={true}
      />
    );

    const freeTexts = screen.getAllByText(/FREE/);
    expect(freeTexts.length).toBeGreaterThan(0);
  });

  it('should calculate total correctly with shipping cost', () => {
    render(
      <ShippingCostCalculator
        cartTotal={30.00}
        region="US"
        shippingCost={5.99}
        freeShippingThreshold={50.00}
        remainingAmount={20.00}
        qualifiesForFreeShipping={false}
      />
    );

    expect(screen.getByText(/\$35\.99/)).toBeInTheDocument(); // 30.00 + 5.99
  });

  it('should calculate total correctly when shipping is free', () => {
    render(
      <ShippingCostCalculator
        cartTotal={55.00}
        region="US"
        shippingCost={0}
        freeShippingThreshold={50.00}
        remainingAmount={0}
        qualifiesForFreeShipping={true}
      />
    );

    // $55.00 appears as both subtotal and total (when shipping is free)
    const subtotalTotalElements = screen.getAllByText(/\$55\.00/);
    expect(subtotalTotalElements.length).toBeGreaterThanOrEqual(2); // 55.00 + 0
  });

  it('should display shipping banner when below threshold', () => {
    const { container } = render(
      <ShippingCostCalculator
        cartTotal={35.00}
        region="US"
        shippingCost={5.99}
        freeShippingThreshold={50.00}
        remainingAmount={15.00}
        qualifiesForFreeShipping={false}
      />
    );

    const shippingBanner = container.querySelector('.shipping-banner');
    expect(shippingBanner).toBeInTheDocument();
  });

  it('should not display shipping banner when qualifies for free shipping', () => {
    const { container } = render(
      <ShippingCostCalculator
        cartTotal={60.00}
        region="US"
        shippingCost={0}
        freeShippingThreshold={50.00}
        remainingAmount={0}
        qualifiesForFreeShipping={true}
      />
    );

    // When shipping is free, the progress wrapper should not be rendered at all
    const shippingProgress = container.querySelector('.shipping-progress-wrapper');
    expect(shippingProgress).toBeNull();
  });

  it('should display cost breakdown with all required fields', () => {
    render(
      <ShippingCostCalculator
        cartTotal={25.50}
        region="US"
        shippingCost={5.99}
        freeShippingThreshold={50.00}
        remainingAmount={24.50}
        qualifiesForFreeShipping={false}
      />
    );

    expect(screen.getByText(/Subtotal:/i)).toBeInTheDocument();
    expect(screen.getByText(/\$25\.50/)).toBeInTheDocument();
    // "Shipping:" appears in cost breakdown (and possibly in "Estimated Shipping:" label)
    const shippingLabels = screen.getAllByText(/Shipping:/i);
    expect(shippingLabels.length).toBeGreaterThan(0);
    // Shipping cost appears in both estimated shipping and cost breakdown
    const shippingCostElements = screen.getAllByText(/\$5\.99/);
    expect(shippingCostElements.length).toBeGreaterThan(0);
    // "Total:" appears in cost breakdown
    const totalLabels = screen.getAllByText(/Total:/i);
    expect(totalLabels.length).toBeGreaterThan(0);
    expect(screen.getByText(/\$31\.49/)).toBeInTheDocument();
  });

  it('should handle zero cart total', () => {
    render(
      <ShippingCostCalculator
        cartTotal={0}
        region="US"
        shippingCost={5.99}
        freeShippingThreshold={50.00}
        remainingAmount={50.00}
        qualifiesForFreeShipping={false}
      />
    );

    expect(screen.getByText(/\$0\.00/)).toBeInTheDocument(); // Subtotal
    // Shipping cost appears in both estimated shipping and cost breakdown
    const shippingCostElements = screen.getAllByText(/\$5\.99/);
    expect(shippingCostElements.length).toBeGreaterThan(0); // Shipping appears at least once
    // Total should also be $5.99 (0 + 5.99)
    const totalElements = screen.getAllByText(/\$5\.99/);
    expect(totalElements.length).toBeGreaterThanOrEqual(2); // Shipping + Total
  });

  it('should handle different regions', () => {
    render(
      <ShippingCostCalculator
        cartTotal={40.00}
        region="CA"
        shippingCost={9.99}
        freeShippingThreshold={75.00}
        remainingAmount={35.00}
        qualifiesForFreeShipping={false}
      />
    );

    expect(screen.getByText(/\$40\.00/)).toBeInTheDocument();
    // Shipping cost appears in both estimated shipping and cost breakdown
    const shippingCostElements = screen.getAllByText(/\$9\.99/);
    expect(shippingCostElements.length).toBeGreaterThan(0);
    expect(screen.getByText(/\$49\.99/)).toBeInTheDocument();
  });

  it('should display success icon when qualifies for free shipping', () => {
    render(
      <ShippingCostCalculator
        cartTotal={60.00}
        region="US"
        shippingCost={0}
        freeShippingThreshold={50.00}
        remainingAmount={0}
        qualifiesForFreeShipping={true}
      />
    );

    expect(screen.getByText(/🎉/)).toBeInTheDocument();
  });
});





