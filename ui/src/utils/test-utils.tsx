import { render, RenderResult } from '@testing-library/react';
import { axe, AxeResults } from 'jest-axe';
import React from 'react';

/**
 * Renders a React component and automatically checks for accessibility violations.
 * 
 * This utility function combines React Testing Library's render function with
 * jest-axe accessibility testing, making it easy to add accessibility checks to
 * component tests.
 * 
 * @param component - The React component to render and test
 * @returns The render result from React Testing Library plus axe results
 * 
 * @example
 * ```tsx
 * it('should have no accessibility violations', async () => {
 *   const { container } = await renderAndCheckA11y(<MyComponent />);
 *   // Component is automatically checked for a11y violations
 * });
 * ```
 */
export async function renderAndCheckA11y(
  component: React.ReactElement
): Promise<RenderResult & { axeResults: AxeResults }> {
  const renderResult = render(component);
  const axeResults = await axe(renderResult.container);
  expect(axeResults).toHaveNoViolations();
  return { ...renderResult, axeResults };
}
