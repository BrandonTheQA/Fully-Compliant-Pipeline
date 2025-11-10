# React Frontend UI

A modern React + TypeScript frontend for the E-Commerce platform that allows users to complete the happy path workflow: create user accounts, browse/create products, and place orders.

## Features

- **User Management**: Create and manage user accounts
- **Product Catalog**: Browse all products and create new ones
- **Shopping Cart**: Add products to cart with quantity management
- **Order Management**: Create orders and view order history
- **Responsive Design**: Modern, mobile-friendly UI
- **TypeScript**: Full type safety
- **Unit Tests**: Comprehensive test coverage with Jest and React Testing Library

## Tech Stack

- **React 19** with TypeScript
- **Vite** for fast development and building
- **React Router** for navigation
- **Axios** for API calls
- **Jest + React Testing Library** for testing
- **CSS Modules** for styling

## Getting Started

### Prerequisites

- Node.js 18 or higher
- npm or yarn

### Installation

```bash
npm install --legacy-peer-deps
```

### Development

Start the development server:

```bash
npm run dev
```

The app will be available at `http://localhost:5173`

### Environment Variables

The app now relies on a single backend API exposed at `/api`. For local development the Vite dev server proxies `/api` requests to the monolith running on `http://localhost:8080`, so no additional environment variables are required. In production the Nginx configuration included in the Docker image forwards `/api` to the monolith service automatically.

### Testing

Run unit tests:

```bash
npm test
```

Run tests with coverage:

```bash
npm run test:coverage
```

### Building

Build for production:

```bash
npm run build
```

The build output will be in the `dist` folder.

## Project Structure

```
ui/
├── src/
│   ├── components/     # Reusable UI components
│   │   ├── UserForm.tsx
│   │   ├── ProductList.tsx
│   │   ├── ProductForm.tsx
│   │   ├── OrderForm.tsx
│   │   └── OrderDetails.tsx
│   ├── pages/          # Page components
│   │   ├── Home.tsx
│   │   ├── UserPage.tsx
│   │   ├── ProductsPage.tsx
│   │   └── OrdersPage.tsx
│   ├── services/       # API service layer
│   │   ├── api.ts
│   │   ├── userService.ts
│   │   ├── productService.ts
│   │   └── orderService.ts
│   ├── context/        # React context for state management
│   │   └── AppContext.tsx
│   ├── types/          # TypeScript type definitions
│   │   └── index.ts
│   └── App.tsx         # Main app component
├── package.json
├── vite.config.ts
├── jest.config.js
└── web.config          # Azure App Service configuration
```

## Happy Path Workflow

The UI implements the complete integration test workflow:

1. **Create User**: Navigate to User page and create a new account
2. **Browse Products**: Go to Products page to view available products
3. **Add to Cart**: Click "Add to Cart" on desired products
4. **Create Order**: Navigate to Orders page and place the order
5. **View Order**: View order details and confirmation

## Deployment

The UI is automatically built and deployed to Azure App Service via GitHub Actions for each environment:

- **Dev**: `joaz-ui-9021-dev`
- **Test**: `joaz-ui-9021-test`
- **Stage**: `joaz-ui-9021-stage`
- **Production**: `joaz-ui-9021-prod`

Deployment happens in parallel with function app deployments for each environment.

## API Endpoints

The UI communicates with three Azure Function backends:

- User API: `/api/users`
- Product API: `/api/products`
- Order API: `/api/orders`

Base URLs are configured per environment via environment variables.
