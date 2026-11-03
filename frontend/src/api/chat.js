import request from './request'

/** Inbox: every conversation plus the global unread count. */
export function getInbox() {
  return request.get('/chats')
}

/** Cheap call used by the header badge poller. */
export function getUnreadChatCount() {
  return request.get('/chats/unread-count')
}

/** Full thread with one user — reading it marks their messages as read. */
export function getThread(partnerId) {
  return request.get(`/chats/${partnerId}/messages`)
}

export function sendMessage(partnerId, content, ticketId = null) {
  return request.post(`/chats/${partnerId}/messages`, { content, ticketId })
}

export function markThreadRead(partnerId) {
  return request.put(`/chats/${partnerId}/read`)
}
