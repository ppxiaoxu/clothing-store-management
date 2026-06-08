import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { clearAuth, getToken } from '@/utils/auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

const DEFAULT_ERROR_MESSAGE = '操作失败，请检查输入内容'

function getErrorMessage(error, fallback = DEFAULT_ERROR_MESSAGE) {
  if (error?.response) {
    return error.response.data?.message || fallback
  }
  return fallback
}

function redirectToLogin() {
  clearAuth()
  if (router.currentRoute.value.path !== '/login') {
    router.push('/login')
  }
}

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (!res || typeof res.code === 'undefined') {
      return res
    }
    if (res.code === 401) {
      const message = res.message || '登录已过期，请重新登录'
      ElMessage.error(message)
      redirectToLogin()
      return Promise.reject(new Error(message))
    }
    if (res.code === 403) {
      const message = res.message || '无权限访问'
      ElMessage.error(message)
      router.push('/dashboard')
      return Promise.reject(new Error(message))
    }
    if (res.code !== 200) {
      const message = res.message || DEFAULT_ERROR_MESSAGE
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
    return res
  },
  (error) => {
    if (error.response?.status === 401) {
      const message = getErrorMessage(error, '登录已过期，请重新登录')
      ElMessage.error(message)
      redirectToLogin()
      return Promise.reject(new Error(message))
    } else if (error.response?.status === 403) {
      const message = getErrorMessage(error, '无权限访问')
      ElMessage.error(message)
      router.push('/dashboard')
      return Promise.reject(new Error(message))
    } else {
      const message = getErrorMessage(error)
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
  }
)

export default request
