import { defineStore } from 'pinia'
import { getUnreadChatCount } from '@/api/chat'
import { useAuthStore } from '@/stores/auth'

/**
 * Owns two things that need to live outside any single page:
 * the unread badge in the header, and the floating chat window state so
 * "Message seller" can be triggered from anywhere.
 */
export const useChatStore = defineStore('chat', {
  state: () => ({
    unreadCount: 0,
    open: false,
    partner: null,
    pollTimer: null
  }),
  getters: {
    isOpen: (state) => state.open && !!state.partner
  },
  actions: {
    async loadUnread() {
      const auth = useAuthStore()
      if (!auth.token) {
        this.unreadCount = 0
        return
      }
      try {
        const res = await getUnreadChatCount()
        this.unreadCount = Number(res.data?.unreadCount) || 0
      } catch {
        /* offline or session expired — keep the previous badge value */
      }
    },
    setUnread(count) {
      this.unreadCount = Math.max(0, Number(count) || 0)
    },
    /** Opens (or retargets) the floating chat window. */
    openWith(partner) {
      if (!partner?.id) return
      this.partner = {
        id: Number(partner.id),
        username: partner.username || '',
        ticketId: partner.ticketId ? Number(partner.ticketId) : null,
        ticketLabel: partner.ticketLabel || ''
      }
      this.open = true
    },
    minimize() {
      this.open = false
    },
    close() {
      this.open = false
      this.partner = null
    },
    startPolling(interval = 15000) {
      if (this.pollTimer) return
      this.pollTimer = setInterval(() => this.loadUnread(), interval)
    },
    stopPolling() {
      if (this.pollTimer) {
        clearInterval(this.pollTimer)
        this.pollTimer = null
      }
    },
    reset() {
      this.stopPolling()
      this.unreadCount = 0
      this.open = false
      this.partner = null
    }
  }
})
