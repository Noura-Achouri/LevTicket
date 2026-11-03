import request from './request'

export function getRecommendations() {
  return request.post('/ai/recommend', {})
}

export function getPriceInsight(ticketId) {
  return request.get(`/ai/price-insight/${ticketId}`)
}

export function submitComplaint(message, history = [], image = null, userName = '') {
  return request.post('/ai/complaint', {
    message,
    history,
    image,
    userName: userName || undefined
  })
}

export function extractTicketFromImage(imageFile) {
  const formData = new FormData()
  formData.append('ticketImage', imageFile)
  return request.post('/ai/extract-ticket', formData)
}