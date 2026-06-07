import request from '@/utils/request'

export function getPurchaseRecordPage(params) {
  return request.get('/purchase-record/page', { params })
}

export function getPurchaseRecordById(id) {
  return request.get(`/purchase-record/${id}`)
}

export function createPurchaseRecord(data) {
  return request.post('/purchase-record', data)
}

export function updatePurchaseRecord(data) {
  return request.put('/purchase-record', data)
}

export function deletePurchaseRecord(id) {
  return request.delete(`/purchase-record/${id}`)
}
