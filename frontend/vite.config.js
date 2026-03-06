import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        port: 5173,
        proxy: {
            '/api': {
                target: process.env.VITE_API_URL || 'http://localhost:8080',
                changeOrigin: true,
            },
            '/ws-review': {
                target: process.env.VITE_API_URL || 'http://localhost:8080',
                changeOrigin: true,
                ws: true
            }
        }
    },
    build: {
        outDir: 'dist',
        sourcemap: false,
        rollupOptions: {
            output: {
                manualChunks: {
                    'react-vendor': ['react', 'react-dom', 'react-router-dom'],
                    'monaco': ['@monaco-editor/react'],
                    'charts': ['recharts']
                }
            }
        }
    }
})
