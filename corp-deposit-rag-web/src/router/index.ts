import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import MainLayout from '@/layout/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/assistant' },
    { path: '/login', component: () => import('@/views/login/index.vue') },
    {
      path: '/', component: MainLayout, children: [
        { path: 'assistant', component: () => import('@/views/assistant/index.vue') },
        { path: 'intentions', component: () => import('@/views/intention/index.vue') },
        { path: 'intentions/:id', component: () => import('@/views/intention/detail.vue') },
        { path: 'products', component: () => import('@/views/product/index.vue') },
        { path: 'products/:id', component: () => import('@/views/product/detail.vue') }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const user = useUserStore()
  if (to.path !== '/login' && !user.token) return '/login'
  if (to.path === '/login' && user.token) return '/assistant'
})

export default router
