/// <reference types="@testing-library/jest-dom" />
import { describe, it, expect } from '@jest/globals';
import { render, screen } from '@testing-library/react';
import { ShippingBanner } from '../ShippingBanner';

describe('ShippingBanner', () => {
  it('should display message when cart total is below threshold', () => {
    render(
      <ShippingBanner
        cartTotal={35.00}
        region="US"
        threshold={50.00}
      />
    );

    expect(screen.getByText(/Add \$15\.00 more to qualify for FREE shipping!/i)).toBeInTheDocument();
  });

  it('should display success message when cart qualifies for free shipping', () => {
    render(
      <ShippingBanner
        cartTotal={60.00}
        region="US"
        threshold={50.00}
      />
    );

    expect(screen.getByText(/You've qualified for FREE shipping!/i)).toBeInTheDocument();
  });

  it('should display success message when cart total exactly equals threshold', () => {
    render(
      <ShippingBanner
        cartTotal={50.00}
        region="US"
        threshold={50.00}
      />
    );

    expect(screen.getByText(/You've qualified for FREE shipping!/i)).toBeInTheDocument();
  });

  it('should calculate remaining amount correctly', () => {
    render(
      <ShippingBanner
        cartTotal={25.50}
        region="US"
        threshold={50.00}
      />
    );

    expect(screen.getByText(/Add \$24\.50 more to qualify for FREE shipping!/i)).toBeInTheDocument();
  });

  it('should show progress bar when below threshold', () => {
    const { container } = render(
      <ShippingBanner
        cartTotal={30.00}
        region="US"
        threshold={50.00}
      />
    );

    const progressBar = container.querySelector('.shipping-banner-progress-bar');
    expect(progressBar).toBeInTheDocument();
    expect(progressBar?.getAttribute('style')).toContain('width: 60%');
  });

  it('should not show progress bar when qualifies for free shipping', () => {
    const { container } = render(
      <ShippingBanner
        cartTotal={60.00}
        region="US"
        threshold={50.00}
      />
    );

    const progressBar = container.querySelector('.shipping-banner-progress-bar');
    expect(progressBar).not.toBeInTheDocument();
  });

  it('should handle zero cart total', () => {
    render(
      <ShippingBanner
        cartTotal={0}
        region="US"
        threshold={50.00}
      />
    );

    expect(screen.getByText(/Add \$50\.00 more to qualify for FREE shipping!/i)).toBeInTheDocument();
  });

  it('should handle different regions', () => {
    render(
      <ShippingBanner
        cartTotal={40.00}
        region="CA"
        threshold={75.00}
      />
    );

    expect(screen.getByText(/Add \$35\.00 more to qualify for FREE shipping!/i)).toBeInTheDocument();
  });

  it('should have correct accessibility attributes', () => {
    const { container } = render(
      <ShippingBanner
        cartTotal={30.00}
        region="US"
        threshold={50.00}
      />
    );

    const progressBar = container.querySelector('[role="progressbar"]');
    expect(progressBar).toBeInTheDocument();
    expect(progressBar?.getAttribute('aria-valuenow')).toBe('60');
    expect(progressBar?.getAttribute('aria-valuemin')).toBe('0');
    expect(progressBar?.getAttribute('aria-valuemax')).toBe('100');
  });
});

