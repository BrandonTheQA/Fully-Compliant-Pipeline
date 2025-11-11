import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

type AppGlobal = typeof globalThis & {
  __APP_ENV__?: Record<string, string | undefined>
}

if (typeof globalThis !== 'undefined') {
  const appGlobal = globalThis as AppGlobal
  appGlobal.__APP_ENV__ = {
    ...(appGlobal.__APP_ENV__ ?? {}),
    VITE_API_BASE_URL: import.meta.env.VITE_API_BASE_URL,
  }
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
