<template>
  <div class="auth-page">
    <section class="auth-card">
      <div class="auth-brand">
        <div class="brand-mark">
          <span class="store-roof"></span>
          <span class="store-body"></span>
        </div>
        <div>
          <h1>创建账号</h1>
          <p>注册后即可开始记货和管理库存</p>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model.trim="form.nickname" placeholder="例如：店主" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" placeholder="请输入密码" type="password" show-password size="large" />
        </el-form-item>
        <el-button class="submit-button" type="primary" size="large" :loading="loading" @click="submit">
          注册
        </el-button>
      </el-form>

      <div class="auth-switch">
        <span>已有账号?</span>
        <RouterLink to="/login">去登录</RouterLink>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(circle at 20% 15%, rgba(59, 130, 246, 0.14), transparent 28%),
    linear-gradient(135deg, #f8fafc 0%, #eef4ff 52%, #fff7f8 100%);
}

.auth-card {
  width: min(420px, 100%);
  padding: 32px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid #e8edf5;
  border-radius: 8px;
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.12);
}

.auth-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
}

.auth-brand h1 {
  margin: 0;
  color: #0f1d37;
  font-size: 25px;
  font-weight: 850;
}

.auth-brand p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 14px;
}

.brand-mark {
  position: relative;
  flex: 0 0 auto;
  width: 54px;
  height: 54px;
  background: #fff7f8;
  border-radius: 8px;
  box-shadow: 0 10px 22px rgba(251, 113, 133, 0.22);
}

.store-roof {
  position: absolute;
  top: 10px;
  left: 9px;
  width: 36px;
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
  left: 13px;
  bottom: 9px;
  width: 28px;
  height: 23px;
  background: #ffffff;
  border: 3px solid #fb7185;
  border-radius: 3px;
}

.store-body::before {
  content: '';
  position: absolute;
  left: 9px;
  bottom: -3px;
  width: 7px;
  height: 13px;
  background: #fecdd3;
  border: 2px solid #fb7185;
  border-bottom: 0;
  border-radius: 3px 3px 0 0;
}

.submit-button {
  width: 100%;
  margin-top: 8px;
}

.auth-switch {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 20px;
  color: #64748b;
  font-size: 14px;
}

.auth-switch a {
  color: #2563eb;
  font-weight: 700;
  text-decoration: none;
}

@media screen and (max-width: 480px) {
  .auth-page {
    padding: 16px;
  }

  .auth-card {
    padding: 24px;
  }
}
</style>
