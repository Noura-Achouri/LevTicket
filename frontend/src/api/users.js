import request from './request'

export function getUserProfile(id) {
  return request.get(`/users/${id}`)
}

export function getUserReviews(id) {
  return request.get(`/reviews/user/${id}`)  // changed from /users/${id}/reviews
}

export function getMyProfile() {
  return request.get('/users/me')
}

export function updateMyProfile(bio, phone) {
  return request.put('/users/me', { bio, phone })
}

export function uploadAvatar(formData) {
  return request.post('/users/me/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function deleteAvatar() {
  return request.delete('/users/me/avatar')
}

export function getBidStats() {
  return request.get('/users/me/bid-stats')
}

export function getTrustScore(id) {
  return request.get(`/users/${id}/trust-score`)
}