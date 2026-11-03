import request from './request'

export function submitReview(orderId, revieweeId, starRating, comment) {
  return request.post('/reviews', { orderId, revieweeId, starRating, comment })
}