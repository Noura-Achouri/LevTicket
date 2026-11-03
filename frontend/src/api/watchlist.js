import request from './request'

export function getWatchlist() {
  return request.get('/watchlist')
}

/** Just the saved ticket ids - used to render heart state everywhere at once. */
export function getWatchedIds() {
  return request.get('/watchlist/ids')
}

export function addToWatchlist(ticketId) {
  return request.post(`/watchlist/${ticketId}`)
}

export function removeFromWatchlist(ticketId) {
  return request.delete(`/watchlist/${ticketId}`)
}

/** Public: how many people saved this ticket. */
export function getWatcherCount(ticketId) {
  return request.get(`/tickets/${ticketId}/watchers`)
}
