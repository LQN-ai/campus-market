import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'home', component: () => import('../views/Home.vue') },
  { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'register', component: () => import('../views/Register.vue') },
  { path: '/product/:id', name: 'productDetail', component: () => import('../views/ProductDetail.vue') },
  { path: '/cart', name: 'cart', component: () => import('../views/Cart.vue') },
  { path: '/orders', name: 'orders', component: () => import('../views/Orders.vue') },
  { path: '/wallet', name: 'wallet', component: () => import('../views/Wallet.vue') },
  { path: '/merchant', name: 'merchant', component: () => import('../views/Merchant.vue') },
  { path: '/admin', name: 'admin', component: () => import('../views/Admin.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 简单的路由守卫：未登录访问受保护页面跳转登录
const publicPages = ['login', 'register', 'home', 'productDetail']
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (!publicPages.includes(to.name) && !token) {
    return { name: 'login' }
  }
  return true
})

export default router
