import express from 'express';
import { createProxyMiddleware } from 'http-proxy-middleware';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';
import { readFileSync, existsSync } from 'fs';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const app = express();
const PORT = process.env.PORT || process.env.WEBSITES_PORT || 8080;
const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

const distPath = join(__dirname, 'dist');

// Log startup information
console.log('=== Server Startup ===');
console.log('PORT:', PORT);
console.log('WEBSITES_PORT:', process.env.WEBSITES_PORT);
console.log('BACKEND_URL:', BACKEND_URL);
console.log('distPath:', distPath);
console.log('distPath exists:', existsSync(distPath));
console.log('server.js location:', __filename);
console.log('Current directory:', __dirname);

// Proxy API requests to backend (must be before static files)
app.use('/api', (req, res, next) => {
  console.log(`[API Proxy] ${req.method} ${req.path} -> ${BACKEND_URL}${req.path}`);
  next();
}, createProxyMiddleware({
  target: BACKEND_URL,
  changeOrigin: true,
  pathRewrite: {
    '^/api': '/api', // Keep /api prefix
  },
  onProxyReq: (proxyReq, req, res) => {
    // Preserve original host header for proper routing
    proxyReq.setHeader('X-Forwarded-Host', req.headers.host);
    proxyReq.setHeader('X-Forwarded-Proto', req.protocol);
    console.log(`[Proxy Request] ${req.method} ${req.path} -> ${BACKEND_URL}${req.path}`);
  },
  onError: (err, req, res) => {
    console.error('[Proxy Error]', err);
    if (!res.headersSent) {
      res.status(500).json({ error: 'Proxy error', message: err.message });
    }
  },
  onProxyRes: (proxyRes, req, res) => {
    console.log(`[Proxy Response] ${req.method} ${req.path} -> ${proxyRes.statusCode}`);
  },
}));

// Health check endpoint
app.get('/health', (req, res) => {
  res.status(200).send('healthy');
});

// Serve static files from dist directory (after API proxy)
app.use(express.static(distPath));

// Handle SPA routing - serve index.html for all non-API routes
app.get('*', (req, res) => {
  // Don't serve index.html for API routes
  if (req.path.startsWith('/api')) {
    return res.status(404).json({ error: 'Not found' });
  }
  
  const indexPath = join(distPath, 'index.html');
  try {
    const indexHtml = readFileSync(indexPath, 'utf-8');
    res.send(indexHtml);
  } catch (error) {
    console.error('Error reading index.html:', error);
    res.status(500).send('Internal server error');
  }
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`✅ Server running on port ${PORT}`);
  console.log(`✅ Backend URL: ${BACKEND_URL}`);
  console.log(`✅ Serving static files from: ${distPath}`);
  console.log(`✅ Server is ready to accept connections`);
}).on('error', (err) => {
  console.error('❌ Server failed to start:', err);
  process.exit(1);
});

