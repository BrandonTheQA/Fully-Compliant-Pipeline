import express from 'express';
import { createProxyMiddleware } from 'http-proxy-middleware';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';
import { readFileSync } from 'fs';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const app = express();
const PORT = process.env.PORT || process.env.WEBSITES_PORT || 8080;
const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

const distPath = join(__dirname, 'dist');

// Proxy API requests to backend (must be before static files)
app.use('/api', createProxyMiddleware({
  target: BACKEND_URL,
  changeOrigin: true,
  pathRewrite: {
    '^/api': '/api', // Keep /api prefix
  },
  onProxyReq: (proxyReq, req, res) => {
    // Preserve original host header for proper routing
    proxyReq.setHeader('X-Forwarded-Host', req.headers.host);
    proxyReq.setHeader('X-Forwarded-Proto', req.protocol);
  },
  onError: (err, req, res) => {
    console.error('Proxy error:', err);
    res.status(500).json({ error: 'Proxy error', message: err.message });
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

app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
  console.log(`Backend URL: ${BACKEND_URL}`);
  console.log(`Serving static files from: ${distPath}`);
});

