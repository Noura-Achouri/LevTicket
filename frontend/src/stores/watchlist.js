import { defineStore } from 'pinia'
import { getWatchedIds, addToWatchlist, removeFromWatchlist } from '@/api/watchlist'
import { useAuthStore } from '@/stores/auth'

/**
 * Holds the set of saved ticket ids in one place, so every heart on the page
 * reflects the same state and we only fetch once per session.
 */
export const useWatchlistStore = defineStore('watchlist', {
  state: () => ({
    ids: [],
    loaded: false,
    pendingId: null
  }),
  getters: {
    count: (state) => state.ids.length,
    isWatched: (state) => (ticketId) => state.ids.includes(Number(ticketId))
  },
  actions: {
    async load(force = false) {
      const auth = useAuthStore()
      if (!auth.token) {
        this.ids = []
        this.loaded = false
        return
      }
      if (this.loaded && !force) return
      try {
        const res = await getWatchedIds()
        this.ids = (res.data || []).map(Number)
        this.loaded = true
      } catch {
        this.ids = []
      }
    },
    /** Returns { needsAuth: true } when nobody is signed in. */
    async toggle(ticketId) {
      const auth = useAuthStore()
      if (!auth.token) return { needsAuth: true }

      const id = Number(ticketId)
      this.pendingId = id
      try {
        if (this.ids.includes(id)) {
          await removeFromWatchlist(id)
          this.ids = this.ids.filter((x) => x !== id)
          return { watching: false }
        }
        await addToWatchlist(id)
        this.ids = [...this.ids, id]
        return { watching: true }
      } finally {
        this.pendingId = null
      }
    },
    reset() {
      this.ids = []
      this.loaded = false
      this.pendingId = null
    }
  }
})
