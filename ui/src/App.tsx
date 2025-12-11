import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link, useLocation } from 'react-router-dom';
import { AppProvider } from './context/AppContext';
import { Home } from './pages/Home';
import { UserPage } from './pages/UserPage';
import { CustomerProductsPage } from './pages/CustomerProductsPage';
import { AdminProductsPage } from './pages/AdminProductsPage';
import { OrdersPage } from './pages/OrdersPage';
import { OrderTrackingPage } from './pages/OrderTrackingPage';
import { WishlistPage } from './pages/WishlistPage';
import { GiftCardPurchasePage } from './pages/GiftCardPurchasePage';
import { GiftCardBalancePage } from './pages/GiftCardBalancePage';
import { useAppContext } from './context/AppContext';
import './App.css';

const Navigation: React.FC = () => {
  const { cart } = useAppContext();
  const location = useLocation();
  const isAdminRoute = location.pathname.startsWith('/admin');
  
  return (
    <nav className="main-nav">
      <div className="nav-container">
        <Link to={isAdminRoute ? "/admin/products" : "/"} className="nav-logo">
          E-Commerce <span className="version-badge">v{__APP_VERSION__}</span>
        </Link>
        <div className="nav-links">
          {isAdminRoute ? (
            <>
              <Link to="/admin/products" aria-current={location.pathname === '/admin/products' ? 'page' : undefined}>Admin - Products</Link>
            </>
          ) : (
            <>
              <Link to="/" aria-current={location.pathname === '/' ? 'page' : undefined}>Home</Link>
              <Link to="/user" aria-current={location.pathname === '/user' ? 'page' : undefined}>User</Link>
              <Link to="/products" aria-current={location.pathname === '/products' ? 'page' : undefined}>Products</Link>
              <Link to="/wishlist" aria-current={location.pathname === '/wishlist' ? 'page' : undefined}>Wishlist</Link>
              <Link to="/gift-cards/purchase" aria-current={location.pathname === '/gift-cards/purchase' ? 'page' : undefined}>Gift Cards</Link>
              <Link to="/gift-cards/balance" aria-current={location.pathname === '/gift-cards/balance' ? 'page' : undefined}>Check Balance</Link>
              <Link to="/orders" aria-current={location.pathname.startsWith('/orders') ? 'page' : undefined}>
                Orders {cart.length > 0 && <span className="cart-badge">{cart.length}</span>}
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

function AppContent() {
  return (
    <Router>
      <a href="#main-content" className="skip-link">
        Skip to main content
      </a>
      <Navigation />
      <main id="main-content" className="main-content">
        <Routes>
          {/* Customer routes */}
          <Route path="/" element={<Home />} />
          <Route path="/user" element={<UserPage />} />
          <Route path="/products" element={<CustomerProductsPage />} />
          <Route path="/wishlist" element={<WishlistPage />} />
          <Route path="/gift-cards/purchase" element={<GiftCardPurchasePage />} />
          <Route path="/gift-cards/balance" element={<GiftCardBalancePage />} />
          <Route path="/orders" element={<OrdersPage />} />
          <Route path="/orders/:orderId/tracking" element={<OrderTrackingPage />} />
          
          {/* Admin routes */}
          <Route path="/admin/products" element={<AdminProductsPage />} />
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
