import request from './request'
import { USE_MOCKS } from './config'
import { mockTickets, mockTicketDetails, mockTicketDetail } from './mocks/tickets.mock'
import { API_BASE } from './config'

export function getImageUrl(url) {
  if (!url) return ''
  return url.startsWith('http') ? url : `${API_BASE}${url}`
}

export function getTickets(params) {
  if (USE_MOCKS) {
    const normalizedLocation = params?.location?.toLowerCase()
    const tickets = mockTickets.tickets.filter((ticket) => {
      const matchesKeyword = !params?.keyword || ticket.eventName.toLowerCase().includes(params.keyword.toLowerCase())
      const matchesCategory = !params?.categoryId || ticket.categoryId === params.categoryId
      const matchesLocation = !normalizedLocation || ticket.eventLocation.toLowerCase().includes(normalizedLocation)
      return matchesKeyword && matchesCategory && matchesLocation
    })
    return Promise.resolve({ data: { ...mockTickets, tickets, total: tickets.length } })
  }
  return request.get('/tickets', { params })
}

export function getTicketById(id) {
  if (USE_MOCKS) {
    const detail = mockTicketDetails[id] || mockTicketDetail
    return Promise.resolve({ data: detail })
  }
  return request.get(`/tickets/${id}`)
}

export function createTicket(formData) {
  return request.post('/tickets', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getTicketPhoto(id) {
  return request.get(`/tickets/${id}/ticket-photo`)
}

export function getMyTickets(status) {
  if (USE_MOCKS) return Promise.resolve({ data: mockTickets })
  return request.get('/tickets/my', { params: { status } })
}

export function deleteTicket(id) {
  return request.delete(`/tickets/${id}`)
}