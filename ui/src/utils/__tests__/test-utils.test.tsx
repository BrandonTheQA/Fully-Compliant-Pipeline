/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect, jest } from '@jest/globals';
import { renderAndCheckA11y } from '../test-utils';
import { axe } from 'jest-axe';

// Mock jest-axe
jest.mock('jest-axe', () => ({
  axe: jest.fn().mockResolvedValue({ violations: [] }),
  toHaveNoViolations: jest.fn(),
}));

describe('test-utils', () => {
  describe('renderAndCheckA11y', () => {
    it('should render component and check accessibility', async () => {
      const TestComponent = () => <div>Test Content</div>;

      const result = await renderAndCheckA11y(<TestComponent />);

      expect(result.container).toBeInTheDocument();
      expect(result.getByText('Test Content')).toBeInTheDocument();
      expect(axe).toHaveBeenCalledWith(result.container);
    });

    it('should return render result with axe results', async () => {
      const TestComponent = () => <button>Click me</button>;

      const result = await renderAndCheckA11y(<TestComponent />);

      expect(result).toHaveProperty('container');
      expect(result).toHaveProperty('axeResults');
      expect(result.getByText('Click me')).toBeInTheDocument();
    });

    it('should check for accessibility violations', async () => {
      const mockAxeResults = { violations: [] };
      (axe as jest.Mock).mockResolvedValue(mockAxeResults);

      const TestComponent = () => <div>Test</div>;

      const result = await renderAndCheckA11y(<TestComponent />);

      expect(axe).toHaveBeenCalled();
      expect(result.axeResults).toEqual(mockAxeResults);
    });

    it('should handle components with multiple elements', async () => {
      const TestComponent = () => (
        <div>
          <h1>Title</h1>
          <p>Description</p>
          <button>Action</button>
        </div>
      );

      const result = await renderAndCheckA11y(<TestComponent />);

      expect(result.getByText('Title')).toBeInTheDocument();
      expect(result.getByText('Description')).toBeInTheDocument();
      expect(result.getByText('Action')).toBeInTheDocument();
    });

    it('should work with React Testing Library queries', async () => {
      const TestComponent = () => (
        <div>
          <label htmlFor="input">Label</label>
          <input id="input" type="text" />
        </div>
      );

      const result = await renderAndCheckA11y(<TestComponent />);

      expect(result.getByLabelText('Label')).toBeInTheDocument();
      expect(result.getByRole('textbox')).toBeInTheDocument();
    });
  });
});
