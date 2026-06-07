import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'
import { isLoggedIn } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', subtitle: '欢迎回来，记得每天更新库存哦!', requiresAuth: true }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/category/index.vue'),
        meta: { title: '商品分类', subtitle: '维护商品分类，方便后续查找和统计', requiresAuth: true }
      },
      {
        path: 'supplier',
        name: 'Supplier',
        component: () => import('@/views/supplier/index.vue'),
        meta: { title: '进货商', subtitle: '管理供货渠道和联系人信息', requiresAuth: true }
      },
      {
        path: 'product',
        name: 'Product',
        component: () => import('@/views/product/index.vue'),
        meta: { title: '商品信息', subtitle: '维护商品资料、价格和基础信息', requiresAuth: true }
      },
      {
        path: 'sku',
        name: 'Sku',
        component: () => import('@/views/sku/index.vue'),
        meta: { title: '库存管理', subtitle: '查看库存数量，及时发现低库存商品', requiresAuth: true }
      },
      {
        path: 'purchase-record',
        name: 'PurchaseRecord',
        component: () => import('@/views/PurchaseRecord.vue'),
        meta: { title: '进货记录', subtitle: '记录每次进货，自动同步库存数量', requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const loggedIn = isLoggedIn()
  if (to.meta.requiresAuth && !loggedIn) {
    return '/login'
  }
  if (to.meta.guestOnly && loggedIn) {
    return '/dashboard'
  }
  return true
})

export default router
