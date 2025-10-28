import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  define: {
    // Replace import.meta.env with a runtime-accessible version
    'import.meta.env': 'import.meta.env',
  },
})
