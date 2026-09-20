import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  return {
    plugins: [vue()],
    resolve: {
      alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) }
    },
    server: {
      host: '127.0.0.1',
      port: 5173,
      strictPort: true,
      proxy: {
        '/customer-api': {
          target: env.VITE_CUSTOMER_API || 'http://127.0.0.1:8081',
          changeOrigin: true,
          rewrite: path => path.replace(/^\/customer-api/, '/api')
        },
        '/product-api': {
          target: env.VITE_PRODUCT_API || 'http://127.0.0.1:8082',
          changeOrigin: true,
          rewrite: path => path.replace(/^\/product-api/, '/api')
        },
        '/business-api': {
          target: env.VITE_BUSINESS_API || 'http://127.0.0.1:8083',
          changeOrigin: true,
          rewrite: path => path.replace(/^\/business-api/, '/api')
        },
        '/api': {
          target: env.VITE_ASSISTANT_API || 'http://127.0.0.1:8084',
          changeOrigin: true
        }
      }
    }
  }
})
