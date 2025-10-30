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

The app uses environment variables for API endpoints. Configure them in `.env` files:

- `.env.development` - Dev environment
- `.env.test` - Test environment
- `.env.staging` - Staging environment
- `.env.production` - Production environment

Example (AKS internal services or your ingress/gateway URLs):
```
VITE_USER_API_URL=http://user-service.app-services.svc.cluster.local:8080/api
VITE_PRODUCT_API_URL=http://product-service.app-services.svc.cluster.local:8080/api
VITE_ORDER_API_URL=http://order-service.app-services.svc.cluster.local:8080/api
```

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

The backend APIs are Spring Boot services deployed to Azure Kubernetes Service (AKS). Point the UI environment variables to your AKS ingress or service endpoints as appropriate.

## API Endpoints

The UI communicates with three Spring Boot services running on AKS:

- User API: `/api/users`
- Product API: `/api/products`
- Order API: `/api/orders`

Base URLs are configured per environment via environment variables.
