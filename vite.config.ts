import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Base path must match the GitHub Pages repository subpath, e.g.
// https://<org>.github.io/resideo-nextgen-dashboard/
// Override at build time with: VITE_BASE_PATH=/my-repo/ npm run build
// https://vite.dev/config/
export default defineConfig({
  base: process.env.VITE_BASE_PATH || '/',
  plugins: [react()],
  build: {
    outDir: 'dist',
  },
})
