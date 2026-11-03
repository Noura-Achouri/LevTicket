<template>
  <button
    type="button"
    class="watch-btn"
    :class="{ active: watched, busy: busy }"
    :aria-pressed="watched"
    :title="label"
    :aria-label="label"
    @click.stop.prevent="onClick"
  >
    <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
      <path
        d="M12 20.6 4.7 13.3a4.9 4.9 0 0 1 0-7 4.9 4.9 0 0 1 7 0l.3.4.3-.4a4.9 4.9 0 0 1 7 7Z"
        :fill="watched ? 'currentColor' : 'none'"
        stroke="currentColor"
        stroke-width="1.8"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
    </svg>
    <span v-if="showLabel" class="watch-label">{{ watched ? $t('Saved') : $t('Save') }}</span>
  </button>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useWatchlistStore } from '@/stores/watchlist'
import { t } from '@/i18n'

const props = defineProps({
  ticketId: { type: [Number, String], required: true },
  showLabel: { type: Boolean, default: false }
})

const emit = defineEmits(['change'])

const auth = useAuthStore()
const watchlist = useWatchlistStore()
const route = useRoute()
const router = useRouter()

const watched = computed(() => watchlist.isWatched(props.ticketId))
const busy = computed(() => watchlist.pendingId === Number(props.ticketId))
const label = computed(() => {
  if (!auth.token) return t('Sign in to save tickets')
  return watched.value ? t('Remove from saved') : t('Save this ticket')
})

async function onClick() {
  if (!auth.token) {
    ElMessage.info(t('Sign in to save tickets'))
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  if (busy.value) return
  try {
    const res = await watchlist.toggle(props.ticketId)
    ElMessage.success(res.watching ? t('Added to saved tickets') : t('Removed from saved tickets'))
    emit('change', res)
  } catch (err) {
    ElMessage.error(err?.message || t('Could not update saved tickets'))
  }
}
</script>

<style scoped>
.watch-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 42px;
  height: 42px;
  padding: 0 12px;
  border: 1px solid rgba(148, 163, 184, 0.45);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.94);
  color: #64748b;
  font-family: inherit;
  font-size: 0.86rem;
  font-weight: 700;
  cursor: pointer;
  backdrop-filter: blur(6px);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.9), 0 4px 12px rgba(15, 23, 42, 0.12);
  transition: transform 0.18s ease, color 0.18s ease, background 0.18s ease, box-shadow 0.18s ease;
}
.watch-btn:hover {
  color: #e11d48;
  background: #fff;
  transform: translateY(-2px);
  box-shadow: inset 0 1px 0 #fff, 0 10px 22px rgba(225, 29, 72, 0.22);
}
.watch-btn:active { transform: translateY(0) scale(0.96); }
.watch-btn.active {
  color: #fff;
  border-color: transparent;
  background: linear-gradient(135deg, #f43f5e, #e11d48);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.32), 0 8px 20px rgba(225, 29, 72, 0.35);
}
.watch-btn.active:hover { color: #fff; filter: brightness(1.05); }
.watch-btn.busy { opacity: 0.65; cursor: progress; }
.watch-btn svg { display: block; }
.watch-btn.active svg { animation: heartPop 0.35s cubic-bezier(0.34, 1.56, 0.64, 1); }
@keyframes heartPop {
  0% { transform: scale(0.7); }
  60% { transform: scale(1.22); }
  100% { transform: scale(1); }
}
.watch-label { white-space: nowrap; }

:global(html.dark) .watch-btn {
  background: rgba(15, 23, 42, 0.72);
  border-color: var(--border-color);
  color: var(--text-muted);
}
:global(html.dark) .watch-btn:hover {
  background: rgba(15, 23, 42, 0.92);
  color: #fb7185;
}
</style>
