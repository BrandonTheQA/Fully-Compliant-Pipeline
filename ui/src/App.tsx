import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link, useLocation } from 'react-router-dom';
import { AppProvider } from './context/AppContext';
import { Home } from './pages/Home';
import { UserPage } from './pages/UserPage';
import { ProductsPage } from './pages/ProductsPage';
import { OrdersPage } from './pages/OrdersPage';
import { OrderTrackingPage } from './pages/OrderTrackingPage';
import { WishlistPage } from './pages/WishlistPage';
import { useAppContext } from './context/AppContext';
import './App.css';

const Navigation: React.FC = () => {
  const { cart } = useAppContext();
  const location = useLocation();
  
  return (
    <nav className="main-nav">
      <div className="nav-container">
        <Link to="/" className="nav-logo">
          E-Commerce <span className="version-badge">v{__APP_VERSION__}</span>
        </Link>
        <div className="nav-links">
          <Link to="/" aria-current={location.pathname === '/' ? 'page' : undefined}>Home</Link>
          <Link to="/user" aria-current={location.pathname === '/user' ? 'page' : undefined}>User</Link>
          <Link to="/products" aria-current={location.pathname === '/products' ? 'page' : undefined}>Products</Link>
          <Link to="/wishlist" aria-current={location.pathname === '/wishlist' ? 'page' : undefined}>Wishlist</Link>
          <Link to="/orders" aria-current={location.pathname.startsWith('/orders') ? 'page' : undefined}>
            Orders {cart.length > 0 && <span className="cart-badge">{cart.length}</span>}
          </Link>
        </div>
      </div>
    </nav>
  );
};

function AppContent() {
  return (
    <Router>
      <Navigation />
      <main className="main-content">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/user" element={<UserPage />} />
          <Route path="/products" element={<ProductsPage />} />
          <Route path="/wishlist" element={<WishlistPage />} />
          <Route path="/orders" element={<OrdersPage />} />
          <Route path="/orders/:orderId/tracking" element={<OrderTrackingPage />} />
        </Routes>
      </main>
    </Router>
  );
}

function App() {
  return (
    <AppProvider>
      <AppContent />
    </AppProvider>
  );
}

export default App;
