import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: 'DataBoard' }
      },
      {
        path: '/files',
        name: 'Files',
        component: () => import('@/views/Files.vue'),
        meta: { title: '文件管理', icon: 'Document' }
      },
      {
        path: '/orders',
        name: 'Orders',
        redirect: '/orders/all',
        meta: { title: '订单管理', icon: 'List' },
        children: [
          {
            path: 'all',
            name: 'OrdersAll',
            component: () => import('@/views/orders/OrdersAll.vue'),
            meta: { title: '全部订单', icon: 'List' }
          },
          {
            path: 'pending',
            name: 'OrdersPending',
            component: () => import('@/views/orders/OrdersPending.vue'),
            meta: { title: '待支付', icon: 'Clock' }
          },
          {
            path: 'paid',
            name: 'OrdersPaid',
            component: () => import('@/views/orders/OrdersPaid.vue'),
            meta: { title: '已支付', icon: 'Money' }
          },
          {
            path: 'printing',
            name: 'OrdersPrinting',
            component: () => import('@/views/orders/OrdersPrinting.vue'),
            meta: { title: '打印中', icon: 'Printer' }
          },
          {
            path: 'completed',
            name: 'OrdersCompleted',
            component: () => import('@/views/orders/OrdersCompleted.vue'),
            meta: { title: '已完成', icon: 'Check' }
          },
          {
            path: 'cancelled',
            name: 'OrdersCancelled',
            component: () => import('@/views/orders/OrdersCancelled.vue'),
            meta: { title: '已取消', icon: 'Close' }
          },
          {
            path: 'refunded',
            name: 'OrdersRefunded',
            component: () => import('@/views/orders/OrdersRefunded.vue'),
            meta: { title: '已退款', icon: 'RefreshLeft' }
          }
        ]
      },
      {
        path: '/users',
        name: 'Users',
        component: () => import('@/views/Users.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: '/prices',
        name: 'Prices',
        component: () => import('@/views/Prices.vue'),
        meta: { title: '价格管理', icon: 'Money' }
      },
      {
        path: '/settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: { title: '系统设置', icon: 'Setting' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && authStore.isLoggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router
