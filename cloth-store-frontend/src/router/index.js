import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import Layout from '@/layout/index.vue'
import { getStoredUser, isLoggedIn } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/register',
    redirect: '/login'
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
        path: 'stock-record',
        name: 'StockRecord',
        component: () => import('@/views/StockRecord.vue'),
        meta: { title: '库存流水', subtitle: '查看每一次库存变动的来源和前后数量', requiresAuth: true }
      },
      {
        path: 'purchase-record',
        name: 'PurchaseRecord',
        component: () => import('@/views/PurchaseRecord.vue'),
        meta: { title: '进货记录', subtitle: '记录每次进货，自动同步库存数量', requiresAuth: true }
      },
      {
        path: 'sale-record',
        name: 'SaleRecord',
        component: () => import('@/views/SaleRecord.vue'),
        meta: { title: '销售记录', subtitle: '记录每次销售，自动扣减库存数量', requiresAuth: true }
      },
      {
        path: 'sales-statistics',
        name: 'SalesStatistics',
        component: () => import('@/views/SalesStatistics.vue'),
        meta: { title: '销售统计', subtitle: '查看销售额、收款状态和每日销售表现', requiresAuth: true }
      },
      {
        path: 'user-manage',
        name: 'UserManage',
        component: () => import('@/views/user-manage/index.vue'),
        meta: { title: '用户管理', subtitle: '创建账号、维护角色和启用状态', requiresAuth: true, adminOnly: true }
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
  if (to.meta.adminOnly && getStoredUser()?.role !== 'ADMIN') {
    ElMessage.warning('无权限访问用户管理')
    return '/dashboard'
  }
  if (to.meta.guestOnly && loggedIn) {
    return '/dashboard'
  }
  return true
})

export default router
