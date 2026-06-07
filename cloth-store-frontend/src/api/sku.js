import request from '@/utils/request'

export function getSkuList(params) {
  return request.get('/skus', { params })
}

export function getSkuById(id) {
  return request.get(`/skus/${id}`)
}

export function createSku(data) {
  return request.post('/skus', data)
}

export function updateSku(id, data) {
  return request.put(`/skus/${id}`, data)
}

export function deleteSku(id) {
  return request.delete(`/skus/${id}`)
}
