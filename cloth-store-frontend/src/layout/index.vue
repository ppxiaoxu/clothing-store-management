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
        <button class="help-box" type="button" @click="helpDialogVisible = true">
          <div class="help-icon">?</div>
          <div>
            <div class="help-title">使用帮助</div>
            <div class="help-text">第一次用？点这里看操作说明</div>
          </div>
        </button>
        <div class="copyright">
          <div>© 2026 童装店记货系统</div>
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
              <span class="nickname">{{ currentNickname }}</span>
              <span class="role-badge">{{ currentRoleText }}</span>
              <el-icon><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
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
          <span>© 2026 童装店记货系统</span>
          <span>简单 · 实用 · 好用</span>
        </footer>
      </el-main>
    </el-container>
  </el-container>

  <el-dialog title="个人信息" v-model="profileDialogVisible" width="500px" class="profile-dialog" @close="resetProfileForm">
    <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="86px" class="profile-form">
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model.trim="profileForm.nickname" maxlength="50" show-word-limit placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model.trim="profileForm.phone" maxlength="11" placeholder="请输入手机号，可不填" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="profileDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="profileSaving" @click="submitProfile">保存</el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog title="修改密码" v-model="passwordDialogVisible" width="460px" class="password-dialog" @close="resetPasswordForm">
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
      <div class="dialog-footer">
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordSaving" @click="submitChangePassword">保存</el-button>
      </div>
      </template>
  </el-dialog>

  <el-dialog title="使用帮助" v-model="helpDialogVisible" width="720px" class="help-dialog">
    <div class="help-dialog-content">
      <el-tabs v-model="activeHelpTab" class="help-tabs" stretch>
        <el-tab-pane label="快速上手" name="start">
          <div class="help-tip blue">按这个顺序录入，系统会自动帮你把库存加减记录清楚。</div>
          <ol class="help-step-list">
            <li><strong>第一步：</strong>先在“商品分类”里添加衣服、裤子、鞋子等分类</li>
            <li><strong>第二步：</strong>在“进货商”里添加常用拿货店铺</li>
            <li><strong>第三步：</strong>在“商品信息”里添加商品名称、货号、图片等</li>
            <li><strong>第四步：</strong>在“库存管理”里添加颜色、尺码、库存、进价、售价</li>
            <li><strong>第五步：</strong>进货时填写“进货记录”，库存会自动增加</li>
            <li><strong>第六步：</strong>卖货时填写“销售记录”，库存会自动减少</li>
          </ol>
        </el-tab-pane>

        <el-tab-pane label="功能说明" name="features">
          <div class="help-feature-grid">
            <div class="help-feature"><strong>商品信息</strong><span>记录这是什么货，比如防晒服、牛仔裤</span></div>
            <div class="help-feature"><strong>库存管理</strong><span>记录具体颜色、尺码和库存，比如白色 120码 10件</span></div>
            <div class="help-feature"><strong>进货记录</strong><span>每次拿货都记录，系统自动加库存</span></div>
            <div class="help-feature"><strong>销售记录</strong><span>每次卖货都记录，系统自动减库存</span></div>
            <div class="help-feature"><strong>库存流水</strong><span>查看库存为什么增加或减少</span></div>
            <div class="help-feature"><strong>销售统计</strong><span>查看今天或某段时间卖了多少钱</span></div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="常见问题" name="faq">
          <el-collapse class="help-faq" accordion>
            <el-collapse-item title="为什么新增商品后库存还是0？" name="1">
              商品信息只是记录商品基础资料，还需要到库存管理中添加颜色、尺码和库存。
            </el-collapse-item>
            <el-collapse-item title="进货后要不要手动改库存？" name="2">
              不用。填写进货记录后，系统会自动增加库存。
            </el-collapse-item>
            <el-collapse-item title="卖出商品后要不要手动改库存？" name="3">
              不用。填写销售记录后，系统会自动减少库存。
            </el-collapse-item>
            <el-collapse-item title="库存数量不对怎么办？" name="4">
              可以到库存管理中进行库存调整，并在原因里写清楚，比如“盘点修正”。
            </el-collapse-item>
            <el-collapse-item title="图片上传失败怎么办？" name="5">
              检查网络是否正常，图片大小不要超过5MB，格式使用 jpg、png、webp。
            </el-collapse-item>
            <el-collapse-item title="提示库存不足是什么意思？" name="6">
              说明当前库存数量不够，不能继续销售，需要先确认库存或补货。
            </el-collapse-item>
            <el-collapse-item title="为什么不能随便注册账号？" name="7">
              这是门店内部系统，账号由管理员统一创建，避免外人进入系统。
            </el-collapse-item>
          </el-collapse>
        </el-tab-pane>

        <el-tab-pane label="使用建议" name="tips">
          <div class="help-tip yellow">养成及时记录的习惯，月底看账、查库存会轻松很多。</div>
          <ul class="help-suggestion-list">
            <li>每次进货后及时录入进货记录</li>
            <li>每次销售后及时录入销售记录</li>
            <li>定期查看低库存商品</li>
            <li>每周查看一次销售统计</li>
            <li>不要直接乱改库存，优先通过进货、销售、库存调整来记录变化</li>
          </ul>
        </el-tab-pane>
      </el-tabs>

      <div class="help-contact-tip">如仍有问题，请联系管理员处理。</div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="helpDialogVisible = false">知道了</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { changePassword, getCurrentUser, updateProfile } from '@/api/auth'
import { clearAuth, getStoredUser, setStoredUser } from '@/utils/auth'

const route = useRoute()
const router = useRouter()
const menuVisible = ref(false)
const now = ref(new Date())
const user = ref(getStoredUser())
const profileDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const helpDialogVisible = ref(false)
const activeHelpTab = ref('start')
const profileSaving = ref(false)
const passwordSaving = ref(false)
const profileFormRef = ref(null)
const passwordFormRef = ref(null)
let timer = null

const profileForm = reactive({
  nickname: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const phonePattern = /^1[3-9]\d{9}$/

function validatePhone(rule, value, callback) {
  if (value && !phonePattern.test(value)) {
    callback(new Error('手机号格式不正确'))
    return
  }
  callback()
}

function validateConfirmPassword(rule, value, callback) {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
    return
  }
  callback()
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const profileRules = {
  nickname: [{ required: true, message: '昵称不能为空', trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }]
}

const baseMenuItems = [
  { path: '/dashboard', label: '工作台', icon: 'HomeFilled' },
  { path: '/category', label: '商品分类', icon: 'Grid' },
  { path: '/supplier', label: '进货商', icon: 'UserFilled' },
  { path: '/product', label: '商品信息', icon: 'Goods' },
  { path: '/sku', label: '库存管理', icon: 'Box' },
  { path: '/stock-record', label: '库存流水', icon: 'Tickets' },
  { path: '/purchase-record', label: '进货记录', icon: 'DocumentAdd' },
  { path: '/sale-record', label: '销售记录', icon: 'Sell' },
  { path: '/sales-statistics', label: '销售统计', icon: 'DataAnalysis' }
]

const adminMenuItem = { path: '/user-manage', label: '用户管理', icon: 'User' }

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta?.title || '工作台')
const pageSubtitle = computed(() => route.meta?.subtitle || '欢迎回来，记得每天更新库存哦!')
const currentNickname = computed(() => user.value?.nickname || user.value?.username || '店主')
const isAdmin = computed(() => user.value?.role === 'ADMIN')
const currentRoleText = computed(() => isAdmin.value ? '管理员' : '普通用户')
const menuItems = computed(() => isAdmin.value ? [...baseMenuItems, adminMenuItem] : baseMenuItems)

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
  if (command === 'profile') {
    openProfileDialog()
    return
  }
  if (command === 'changePassword') {
    passwordDialogVisible.value = true
    return
  }
  if (command === 'logout') {
    logout('已退出登录')
  }
}

function openProfileDialog() {
  Object.assign(profileForm, {
    nickname: user.value?.nickname || user.value?.username || '',
    phone: user.value?.phone || ''
  })
  profileDialogVisible.value = true
}

function resetProfileForm() {
  profileFormRef.value?.clearValidate()
}

function resetPasswordForm() {
  passwordFormRef.value?.clearValidate()
  Object.assign(passwordForm, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
}

async function submitProfile() {
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return

  profileSaving.value = true
  try {
    const res = await updateProfile(profileForm)
    user.value = res.data
    setStoredUser(res.data)
    profileDialogVisible.value = false
    ElMessage.success('个人信息保存成功')
  } finally {
    profileSaving.value = false
  }
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
  width: 100%;
  gap: 14px;
  padding: 16px;
  color: #ffffff;
  text-align: left;
  background:
    radial-gradient(circle at 12% 10%, rgba(147, 197, 253, 0.28), transparent 34%),
    linear-gradient(135deg, rgba(37, 99, 235, 0.92), rgba(14, 116, 144, 0.84));
  border: 1px solid rgba(191, 219, 254, 0.32);
  border-radius: 8px;
  box-shadow: 0 12px 26px rgba(15, 23, 42, 0.18);
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.help-box:hover {
  transform: translateY(-2px);
  border-color: rgba(219, 234, 254, 0.58);
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.24);
}

.help-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  background: linear-gradient(135deg, #dbeafe, #93c5fd);
  color: #1d4ed8;
  border-radius: 50%;
  font-size: 25px;
  font-weight: 800;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.52);
}

.help-title {
  font-size: 17px;
  font-weight: 800;
}

.help-text {
  margin-top: 4px;
  color: rgba(226, 232, 240, 0.9);
  font-size: 13px;
  line-height: 1.35;
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

.role-badge {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 8px;
  color: #2563eb;
  background: #dbeafe;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}

.user-pill .el-icon {
  flex: 0 0 auto;
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

.profile-form :deep(.el-form-item__label),
.password-form :deep(.el-form-item__label) {
  white-space: nowrap;
}

.help-dialog-content {
  color: #334155;
  font-size: 16px;
  line-height: 1.75;
}

.help-tabs :deep(.el-tabs__item) {
  height: 46px;
  color: #475569;
  font-size: 16px;
  font-weight: 800;
}

.help-tabs :deep(.el-tabs__item.is-active) {
  color: #2563eb;
}

.help-step-list,
.help-suggestion-list {
  margin: 0;
  padding-left: 22px;
}

.help-step-list li,
.help-suggestion-list li {
  margin-top: 10px;
  padding-left: 4px;
}

.help-dialog-content strong {
  color: #0f172a;
}

.help-tip,
.help-contact-tip {
  padding: 12px 14px;
  border-radius: 8px;
  font-weight: 700;
}

.help-tip {
  margin-bottom: 14px;
}

.help-tip.blue,
.help-contact-tip {
  color: #1d4ed8;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
}

.help-tip.yellow {
  color: #92400e;
  background: #fffbeb;
  border: 1px solid #fde68a;
}

.help-feature-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.help-feature {
  padding: 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.help-feature strong,
.help-feature span {
  display: block;
}

.help-feature strong {
  margin-bottom: 8px;
  color: #0f172a;
  font-size: 17px;
}

.help-feature span {
  color: #475569;
  line-height: 1.6;
}

.help-faq {
  border-top: 0;
  border-bottom: 0;
}

.help-faq :deep(.el-collapse-item__header) {
  min-height: 52px;
  color: #0f172a;
  font-size: 16px;
  font-weight: 800;
  line-height: 1.45;
}

.help-faq :deep(.el-collapse-item__content) {
  color: #475569;
  font-size: 16px;
  line-height: 1.7;
}

.help-contact-tip {
  margin-top: 16px;
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
    width: 82vw !important;
    max-width: 300px;
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
    position: sticky;
    top: 0;
    z-index: 80;
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
    width: 100%;
    padding: 10px;
  }

  .page-footer {
    display: none;
  }

  .profile-dialog,
  .password-dialog {
    width: 92vw !important;
    max-width: 92vw;
  }

  .profile-form :deep(.el-form-item),
  .password-form :deep(.el-form-item) {
    display: block;
  }

  .help-dialog {
    width: 92vw !important;
    max-width: 92vw;
  }

  .help-dialog-content {
    font-size: 16px;
  }

  .help-tabs :deep(.el-tabs__nav) {
    white-space: nowrap;
  }

  .help-tabs :deep(.el-tabs__item) {
    padding: 0 10px;
    font-size: 16px;
  }

  .help-feature-grid {
    grid-template-columns: 1fr;
  }

  .help-faq :deep(.el-collapse-item__header) {
    min-height: 58px;
    font-size: 16px;
  }
}

@media screen and (max-width: 420px) {
  .top-left p {
    max-width: 34vw;
  }

  .user-pill .nickname {
    display: none;
  }

  .role-badge {
    display: none;
  }
}
</style>
