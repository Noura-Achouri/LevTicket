import request from './request'

export function getNotifications() {
  return request.get('/notifications')
}

export function markNotificationRead(id) {
  return request.put(`/notifications/${id}/read`)
}

export function markAllNotificationsRead() {
  return request.put('/notifications/read-all')
}
