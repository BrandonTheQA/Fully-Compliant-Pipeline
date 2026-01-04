/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect } from '@jest/globals';
import { render, screen } from '@testing-library/react';
import { ReturnLabelDownload } from '../ReturnLabelDownload';

describe('ReturnLabelDownload', () => {
  it('should not render when labelUrl is not provided', () => {
    const { container } = render(<ReturnLabelDownload />);
    expect(container.firstChild).toBeNull();
  });

  it('should not render when labelUrl is empty string', () => {
    const { container } = render(<ReturnLabelDownload labelUrl="" />);
    expect(container.firstChild).toBeNull();
  });

  it('should render return label download with labelUrl', () => {
    render(<ReturnLabelDownload labelUrl="https://example.com/label.pdf" />);

    expect(screen.getByText('Return Shipping Label')).toBeInTheDocument();
    expect(screen.getByText('Download Return Label')).toBeInTheDocument();
    expect(screen.getByText(/Print this label and attach it to your return package/i)).toBeInTheDocument();
  });

  it('should render download link with correct href', () => {
    const labelUrl = 'https://example.com/label.pdf';
    render(<ReturnLabelDownload labelUrl={labelUrl} />);

    const link = screen.getByText('Download Return Label') as HTMLAnchorElement;
    expect(link.href).toBe(labelUrl);
    expect(link.target).toBe('_blank');
    expect(link.rel).toBe('noopener noreferrer');
  });

  it('should render tracking number when provided', () => {
    render(
      <ReturnLabelDownload
        labelUrl="https://example.com/label.pdf"
        trackingNumber="TRACK123456"
      />
    );

    expect(screen.getByText(/Tracking Number:/i)).toBeInTheDocument();
    expect(screen.getByText('TRACK123456')).toBeInTheDocument();
  });

  it('should render carrier when provided', () => {
    render(
      <ReturnLabelDownload
        labelUrl="https://example.com/label.pdf"
        carrier="FedEx"
      />
    );

    expect(screen.getByText(/Carrier:/i)).toBeInTheDocument();
    expect(screen.getByText('FedEx')).toBeInTheDocument();
  });

  it('should render all optional fields when provided', () => {
    render(
      <ReturnLabelDownload
        labelUrl="https://example.com/label.pdf"
        trackingNumber="TRACK123456"
        carrier="FedEx"
      />
    );

    expect(screen.getByText('Return Shipping Label')).toBeInTheDocument();
    expect(screen.getByText(/Tracking Number:/i)).toBeInTheDocument();
    expect(screen.getByText('TRACK123456')).toBeInTheDocument();
    expect(screen.getByText(/Carrier:/i)).toBeInTheDocument();
    expect(screen.getByText('FedEx')).toBeInTheDocument();
    expect(screen.getByText('Download Return Label')).toBeInTheDocument();
  });

  it('should not render tracking number when not provided', () => {
    render(
      <ReturnLabelDownload
        labelUrl="https://example.com/label.pdf"
        carrier="FedEx"
      />
    );

    expect(screen.queryByText(/Tracking Number:/i)).not.toBeInTheDocument();
  });

  it('should not render carrier when not provided', () => {
    render(
      <ReturnLabelDownload
        labelUrl="https://example.com/label.pdf"
        trackingNumber="TRACK123456"
      />
    );

    expect(screen.queryByText(/Carrier:/i)).not.toBeInTheDocument();
  });

  it('should have correct button class', () => {
    render(<ReturnLabelDownload labelUrl="https://example.com/label.pdf" />);

    const link = screen.getByText('Download Return Label');
    expect(link.classList.contains('btn')).toBe(true);
    expect(link.classList.contains('btn-primary')).toBe(true);
  });
});

