<template>
  <el-container class="layout-root">
    <div v-if="menuVisible" class="overlay" @click="menuVisible = false" />

    <el-aside :class="['side-menu', { open: menuVisible }]" width="230px">
      <div class="brand">
        <div class="brand-icon">
          <span class="store-roof"></span>
          <span class="store-body"></span>
        </div>
        <div>
          <div class="brand-title">童装店记货系统</div>
          <div class="brand-subtitle">简单好用的门店管理工具</div>
        </div>
      </div>

      <el-menu
        :default-active="activeMenu"
        router
        class="nav-menu"
        background-color="transparent"
        text-color="#d7e3f4"
        active-text-color="#ffffff"
        @select="menuVisible = false"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>

      <div class="side-footer">
        <div class="help-box">
          <div class="help-icon">?</div>
          <div>
            <div class="help-title">需要帮助?</div>
            <div class="help-text">遇到问题点这里</div>
          </div>
        </div>
        <div class="copyright">
          <div>© 2025 童装店记货系统</div>
          <div>简单 · 实用 · 好用</div>
        </div>
      </div>
    </el-aside>

    <el-container class="main-area">
      <el-header class="top-bar">
        <div class="top-left">
          <el-button class="menu-toggle" @click="menuVisible = !menuVisible" text>
            <el-icon :size="25"><Menu /></el-icon>
          </el-button>
          <div>
            <h1>{{ pageTitle }}</h1>
            <p>{{ pageSubtitle }}</p>
          </div>
        </div>

        <div class="top-right">
          <div class="top-meta">
            <el-icon><Calendar /></el-icon>
            <span>{{ currentDateText }}</span>
          </div>
          <div class="top-meta">
            <el-icon><Clock /></el-icon>
            <span>{{ currentTimeText }}</span>
          </div>
          <el-dropdown trigger="click" @command="handleUserCommand">
            <button class="user-pill" type="button">
              <span class="avatar">
                <el-icon><UserFilled /></el-icon>
              </span>
              <span class="nickname">{{ currentNickname }}</span>
              <el-icon><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="changePassword">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="content">
        <router-view />
        <footer class="page-footer">
          <span>童装店记货系统 · 简单好用的门店管理工具</span>
          <span>建议使用 Chrome 浏览器，获得更好体验</span>
        </footer>
      </el-main>
    </el-container>
  </el-container>

  <el-dialog title="修改密码" v-model="passwordDialogVisible" width="460px" @close="resetPasswordForm">
    <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px" class="password-form">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="passwordDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="passwordSaving" @click="submitChangePassword">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { changePassword, getCurrentUser } from '@/api/auth'
import { clearAuth, getStoredUser, setStoredUser } from '@/utils/auth'

const route = useRoute()
const router = useRouter()
const menuVisible = ref(false)
const now = ref(new Date())
const user = ref(getStoredUser())
const passwordDialogVisible = ref(false)
const passwordSaving = ref(false)
const passwordFormRef = ref(null)
let timer = null

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

function validateConfirmPassword(rule, value, callback) {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
    return
  }
  callback()
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const menuItems = [
  { path: '/dashboard', label: '工作台', icon: 'HomeFilled' },
  { path: '/category', label: '商品分类', icon: 'Grid' },
  { path: '/supplier', label: '进货商', icon: 'UserFilled' },
  { path: '/product', label: '商品信息', icon: 'Goods' },
  { path: '/sku', label: '库存管理', icon: 'Box' },
  { path: '/purchase-record', label: '进货记录', icon: 'DocumentAdd' }
]

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta?.title || '工作台')
const pageSubtitle = computed(() => route.meta?.subtitle || '欢迎回来，记得每天更新库存哦!')
const currentNickname = computed(() => user.value?.nickname || user.value?.username || '店主')

const currentDateText = computed(() => {
  const date = now.value
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const week = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][date.getDay()]
  return `${year}年${month}月${day}日 ${week}`
})

const currentTimeText = computed(() => {
  const date = now.value
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${hour}:${minute}`
})

async function refreshUser() {
  try {
    const res = await getCurrentUser()
    user.value = res.data
    setStoredUser(res.data)
  } catch {
    // 请求拦截器会统一处理失效 token。
  }
}

function handleUserCommand(command) {
  if (command === 'changePassword') {
    passwordDialogVisible.value = true
    return
  }
  if (command === 'logout') {
    logout('已退出登录')
  }
}

function resetPasswordForm() {
  passwordFormRef.value?.clearValidate()
  Object.assign(passwordForm, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
}

async function submitChangePassword() {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  passwordSaving.value = true
  try {
    await changePassword(passwordForm)
    passwordDialogVisible.value = false
    logout('密码修改成功，请重新登录')
  } finally {
    passwordSaving.value = false
  }
}

function logout(message) {
  clearAuth()
  ElMessage.success(message)
  router.push('/login')
}

onMounted(() => {
  refreshUser()
  timer = window.setInterval(() => {
    now.value = new Date()
  }, 1000)
})

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<style scoped>
.layout-root {
  min-height: 100vh;
  background: #f6f8fb;
  color: #152033;
}

.overlay {
  display: none;
  position: fixed;
  inset: 0;
  z-index: 99;
  background: rgba(15, 23, 42, 0.48);
}

.side-menu {
  position: relative;
  z-index: 100;
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(circle at 15% 5%, rgba(59, 130, 246, 0.24), transparent 24%),
    linear-gradient(180deg, #09213f 0%, #061c37 48%, #062646 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 16px 0 35px rgba(15, 23, 42, 0.08);
  overflow-y: auto;
  transition: transform 0.25s ease;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 92px;
  padding: 20px 14px 16px;
}

.brand-icon {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  width: 50px;
  height: 50px;
  background: #fff7f8;
  border-radius: 8px;
  box-shadow: 0 10px 22px rgba(251, 113, 133, 0.28);
}

.store-roof {
  position: absolute;
  top: 9px;
  left: 8px;
  width: 34px;
  height: 12px;
  background: repeating-linear-gradient(90deg, #fb7185 0 8px, #fecdd3 8px 16px);
  border-radius: 5px 5px 3px 3px;
}

.store-roof::before {
  content: '';
  position: absolute;
  left: -2px;
  right: -2px;
  top: -5px;
  height: 7px;
  background: #fb7185;
  border-radius: 4px 4px 1px 1px;
}

.store-body {
  position: absolute;
  left: 12px;
  bottom: 9px;
  width: 26px;
  height: 22px;
  background: #ffffff;
  border: 3px solid #fb7185;
  border-radius: 3px;
}

.store-body::before {
  content: '';
  position: absolute;
  left: 8px;
  bottom: -3px;
  width: 7px;
  height: 12px;
  background: #fecdd3;
  border: 2px solid #fb7185;
  border-bottom: 0;
  border-radius: 3px 3px 0 0;
}

.brand-title {
  color: #ffffff;
  font-size: 19px;
  font-weight: 800;
  line-height: 1.35;
}

.brand-subtitle {
  margin-top: 4px;
  color: rgba(226, 232, 240, 0.9);
  font-size: 13px;
}

.nav-menu {
  flex: 1;
  padding: 10px 14px;
  border-right: 0;
}

.nav-menu :deep(.el-menu-item) {
  height: 52px;
  margin: 8px 0;
  padding: 0 18px !important;
  border-radius: 8px;
  font-size: 16px !important;
  font-weight: 700;
  line-height: 52px;
  transition: background 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
}

.nav-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.09);
  color: #ffffff;
}

.nav-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  box-shadow: 0 12px 26px rgba(37, 99, 235, 0.32);
}

.nav-menu :deep(.el-icon) {
  margin-right: 13px;
  font-size: 21px;
}

.side-footer {
  padding: 14px;
}

.help-box {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  color: #ffffff;
  background: rgba(15, 55, 94, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 8px;
}

.help-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  background: linear-gradient(135deg, #60a5fa, #2563eb);
  border-radius: 50%;
  font-size: 26px;
  font-weight: 800;
}

.help-title {
  font-size: 15px;
  font-weight: 800;
}

.help-text {
  margin-top: 4px;
  color: rgba(226, 232, 240, 0.9);
  font-size: 12px;
}

.copyright {
  margin-top: 24px;
  color: rgba(226, 232, 240, 0.78);
  font-size: 13px;
  line-height: 1.9;
  text-align: center;
}

.main-area {
  min-width: 0;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 82px;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.94);
  border-bottom: 1px solid #e8edf5;
  box-shadow: 0 2px 18px rgba(15, 23, 42, 0.04);
}

.top-left {
  display: flex;
  align-items: center;
  gap: 18px;
  min-width: 0;
}

.menu-toggle {
  display: none;
  width: 42px;
  min-height: 42px;
  padding: 0;
  color: #1f2a44;
}

.top-left h1 {
  margin: 0;
  color: #0f1d37;
  font-size: 24px;
  font-weight: 850;
  line-height: 1.2;
}

.top-left p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 14px;
}

.top-right {
  display: flex;
  align-items: center;
  gap: 18px;
  color: #253247;
  white-space: nowrap;
}

.top-meta {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-size: 14px;
}

.top-meta .el-icon {
  color: #8a97aa;
}

.user-pill {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  height: 48px;
  padding: 0 16px 0 10px;
  color: #1f2a44;
  background: #f1f4f8;
  border: 0;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}

.nickname {
  max-width: 96px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  color: #9aa6b8;
  background: #e2e8f0;
  border-radius: 50%;
}

.content {
  min-height: calc(100vh - 82px);
  padding: 24px;
  background:
    linear-gradient(180deg, #f6f8fb 0%, #f8fafc 42%, #ffffff 100%);
}

.page-footer {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  max-width: 1180px;
  margin: 18px auto 0;
  color: #7b8798;
  font-size: 13px;
}

.password-form :deep(.el-form-item__label) {
  white-space: nowrap;
}

@media screen and (max-width: 1024px) {
  .top-right {
    gap: 12px;
  }

  .top-meta:first-child {
    display: none;
  }
}

@media screen and (max-width: 768px) {
  .side-menu {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    transform: translateX(-100%);
  }

  .side-menu.open {
    transform: translateX(0);
  }

  .overlay {
    display: block;
  }

  .top-bar {
    height: auto;
    min-height: 76px;
    padding: 12px 14px;
  }

  .menu-toggle {
    display: inline-flex;
  }

  .top-left {
    gap: 10px;
  }

  .top-left h1 {
    font-size: 20px;
  }

  .top-left p {
    max-width: 42vw;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .top-right {
    gap: 8px;
  }

  .top-meta {
    display: none;
  }

  .user-pill {
    height: 42px;
    padding-right: 12px;
  }

  .content {
    min-height: calc(100vh - 76px);
    padding: 14px;
  }

  .page-footer {
    display: none;
  }
}

@media screen and (max-width: 420px) {
  .top-left p {
    max-width: 34vw;
  }

  .user-pill .nickname {
    display: none;
  }
}
</style>
