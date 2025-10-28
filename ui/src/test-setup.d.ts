import '@testing-library/jest-dom';

declare module '@jest/expect' {
  interface Matchers<R = void, T = unknown> {
    toBeInTheDocument(): R;
    toHaveTextContent(text: string | RegExp): R;
  }
}
