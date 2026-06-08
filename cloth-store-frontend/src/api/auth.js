import request from '@/utils/request'

export function register(data) {
  return request.post('/auth/register', data)
}

export function login(data) {
  return request.post('/auth/login', data)
}

export function getCurrentUser() {
  return request.get('/auth/me')
}

export function updateProfile(data) {
  return request.put('/auth/profile', data)
}

export function changePassword(data) {
  return request.put('/auth/change-password', data)
}
