import request from './request'

export function getOrders(type = 'bought') {
  return request.get('/orders', { params: { type } })
}

export function getOrderById(id) {
  return request.get(`/orders/${id}`)
}

export function updateOrderStatus(id, status) {
  return request.put(`/orders/${id}/status`, { status })
}