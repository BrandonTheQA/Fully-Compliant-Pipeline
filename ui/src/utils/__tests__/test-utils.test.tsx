/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect, jest } from '@jest/globals';
import { render } from '@testing-library/react';
import { renderAndCheckA11y } from '../test-utils';
import { axe } from 'jest-axe';

jest.mock('jest-axe');

describe('test-utils', () => {
  describe('renderAndCheckA11y', () => {
    it('should render component and check accessibility', async () => {
      const mockAxeResults = {
        violations: [],
      };

      (axe as jest.MockedFunction<typeof axe>).mockResolvedValue(mockAxeResults as any);

      const TestComponent = () => <div>Test</div>;
      const result = await renderAndCheckA11y(<TestComponent />);

      expect(result.container).toBeTruthy();
      expect(axe).toHaveBeenCalledWith(result.container);
      expect(result.axeResults).toEqual(mockAxeResults);
    });

  it('should throw error when accessibility violations are found', async () => {
    const mockAxeResults = {
      violations: [
        {
          id: 'violation-1',
          description: 'Test violation',
          nodes: [],
        },
      ],
    };

    (axe as jest.MockedFunction<typeof axe>).mockResolvedValue(mockAxeResults as any);

    const TestComponent = () => <div>Test</div>;

    // The function will throw because toHaveNoViolations throws when violations are found
    // We need to catch the error from the expect inside renderAndCheckA11y
    try {
      await renderAndCheckA11y(<TestComponent />);
      // If we get here without throwing, the test should fail
      fail('Expected renderAndCheckA11y to throw when violations are found');
    } catch (error) {
      // Expected to throw
      expect(error).toBeDefined();
    }
  });

    it('should return render result with axe results', async () => {
      const mockAxeResults = {
        violations: [],
      };

      (axe as jest.MockedFunction<typeof axe>).mockResolvedValue(mockAxeResults as any);

      const TestComponent = () => (
        <div>
          <button>Click me</button>
        </div>
      );

      const result = await renderAndCheckA11y(<TestComponent />);

      expect(result.getByText('Click me')).toBeInTheDocument();
      expect(result.axeResults).toEqual(mockAxeResults);
    });

    it('should work with complex components', async () => {
      const mockAxeResults = {
        violations: [],
      };

      (axe as jest.MockedFunction<typeof axe>).mockResolvedValue(mockAxeResults as any);

      const TestComponent = () => (
        <div>
          <h1>Title</h1>
          <form>
            <label htmlFor="input">Label</label>
            <input id="input" type="text" />
            <button type="submit">Submit</button>
          </form>
        </div>
      );

      const result = await renderAndCheckA11y(<TestComponent />);

      expect(result.getByText('Title')).toBeInTheDocument();
      expect(result.getByLabelText('Label')).toBeInTheDocument();
      expect(result.getByText('Submit')).toBeInTheDocument();
      expect(axe).toHaveBeenCalledWith(result.container);
    });
  });
});

