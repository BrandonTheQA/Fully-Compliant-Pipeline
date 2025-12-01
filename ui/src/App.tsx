import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
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
  
  return (
    <nav className="main-nav">
      <div className="nav-container">
        <Link to="/" className="nav-logo">
          E-Commerce <span className="version-badge">v{__APP_VERSION__}</span>
        </Link>
        <div className="nav-links">
          <Link to="/">Home</Link>
          <Link to="/user">User</Link>
          <Link to="/products">Products</Link>
          <Link to="/wishlist">Wishlist</Link>
          <Link to="/orders">
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
