import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
    // 强制所有包共享同一个 yjs 实例，解决 "Yjs was already imported" 错误
    dedupe: ['yjs', 'lib0'],
  },
  define: {
    global: 'globalThis',
  },
  optimizeDeps: {
    include: ['yjs', 'y-websocket', 'lib0'],
    force: true,
  },
})