<template>
  <transition name="chat-pop">
    <section
      v-if="chat.isOpen"
      class="chat-window"
      role="dialog"
      :aria-label="$t('Chat')"
      @keydown.esc="chat.minimize()"
    >
      <header class="chat-head">
        <span class="chat-avatar">
          <img v-if="displayAvatar" :src="displayAvatar" :alt="displayName" @error="avatarBroken = true">
          <template v-else>{{ initials(displayName) }}</template>
          <i class="chat-avatar-dot" aria-hidden="true"></i>
        </span>

        <div class="chat-peer">
          <strong>{{ displayName }}</strong>
          <small v-if="chat.partner.ticketLabel">{{ chat.partner.ticketLabel }}</small>
          <small v-else>{{ $t('Private conversation') }}</small>
        </div>

        <div class="chat-head-actions">
          <el-dropdown trigger="click" placement="bottom-end" @command="onCommand">
            <button type="button" class="head-btn" :title="$t('More')" :aria-label="$t('More')">⋯</button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">{{ $t('View profile') }}</el-dropdown-item>
                <el-dropdown-item command="inbox">{{ $t('Open in inbox') }}</el-dropdown-item>
                <el-dropdown-item v-if="chat.partner.ticketId" command="listing" divided>
                  {{ $t('View listing') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <button type="button" class="head-btn" :title="$t('Minimize')" :aria-label="$t('Minimize')" @click="chat.minimize()">—</button>
          <button type="button" class="head-btn" :title="$t('Close')" :aria-label="$t('Close')" @click="chat.close()">✕</button>
        </div>
      </header>

      <MessageThread
        :key="chat.partner.id"
        compact
        :partner-id="chat.partner.id"
        :partner-name="displayName"
        :ticket-id="chat.partner.ticketId"
        :ticket-label="chat.partner.ticketLabel"
        @loaded="onLoaded"
      />
    </section>
  </transition>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import MessageThread from '@/components/MessageThread.vue'
import { useChatStore } from '@/stores/chat'
import { getImageUrl } from '@/api/tickets'

const router = useRouter()
const chat = useChatStore()
const resolvedName = ref('')
const resolvedAvatar = ref('')
const avatarBroken = ref(false)

const displayName = computed(() => chat.partner?.username || resolvedName.value || '')
const displayAvatar = computed(() => {
  if (avatarBroken.value || !resolvedAvatar.value) return ''
  return getImageUrl(resolvedAvatar.value)
})

function initials(value) {
  const text = String(value || '').trim()
  if (!text) return '?'
  return text.replace(/[^A-Za-z0-9]/g, '').slice(0, 2).toUpperCase() || text.charAt(0).toUpperCase()
}

/** The thread endpoint returns the peer's profile, which fills in name + photo. */
function onLoaded(partner) {
  if (partner?.username) resolvedName.value = partner.username
  if (partner?.avatarUrl) resolvedAvatar.value = partner.avatarUrl
}

// A new partner means the cached peer details are stale.
watch(() => chat.partner?.id, () => {
  resolvedName.value = ''
  resolvedAvatar.value = ''
  avatarBroken.value = false
})

/** Shortcuts that would otherwise mean leaving the page you are bidding on. */
function onCommand(command) {
  const partner = chat.partner
  if (!partner) return
  chat.minimize()
  if (command === 'profile') router.push(`/profile/${partner.id}`)
  else if (command === 'inbox') router.push({ path: '/messages', query: { with: partner.id } })
  else if (command === 'listing' && partner.ticketId) router.push(`/auction/${partner.ticketId}`)
}
</script>

<style scoped>
.chat-window {
  position: fixed;
  right: 24px;
  bottom: 104px;
  z-index: 3100;
  display: flex;
  flex-direction: column;
  width: 360px;
  height: 496px;
  max-height: calc(100vh - 140px);
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.32);
  border-radius: 20px;
  background: #fff;
  box-shadow:
    0 32px 68px rgba(15, 23, 42, 0.28),
    0 6px 16px rgba(15, 23, 42, 0.12);
}

/* ---- header ---- */
.chat-head {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 13px 14px;
  background:
    radial-gradient(110% 160% at 18% -10%, rgba(255, 255, 255, 0.3), transparent 62%),
    linear-gradient(135deg, #1d4ed8, #3b82f6);
  color: #fff;
}

.chat-avatar {
  position: relative;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  overflow: visible;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.22);
  border: 1px solid rgba(255, 255, 255, 0.44);
  font-size: 0.8rem;
  font-weight: 800;
  letter-spacing: 0.02em;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.35);
}
.chat-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}
/* "private / present" marker on the avatar rim */
.chat-avatar-dot {
  position: absolute;
  right: -1px;
  bottom: -1px;
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: #34d399;
  border: 2px solid #2563eb;
  box-shadow: 0 2px 4px rgba(2, 6, 23, 0.3);
}

.chat-peer {
  display: grid;
  gap: 1px;
  min-width: 0;
  flex: 1;
}
.chat-peer strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.96rem;
  font-weight: 800;
  letter-spacing: -0.01em;
}
.chat-peer small {
  display: flex;
  align-items: center;
  gap: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.72rem;
  opacity: 0.88;
}
.chat-peer small::before {
  content: '🔒';
  font-size: 0.62rem;
  opacity: 0.85;
}

.chat-head-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}
.chat-head-actions :deep(.el-dropdown) { display: inline-flex; }

.head-btn {
  display: grid;
  place-items: center;
  width: 28px;
  height: 28px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  font-family: inherit;
  font-size: 0.82rem;
  line-height: 1;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
}
.head-btn:hover { background: rgba(255, 255, 255, 0.32); transform: translateY(-1px); }
.head-btn:focus-visible { outline: 2px solid #fff; outline-offset: 2px; }

/* ---- entrance ---- */
.chat-pop-enter-active,
.chat-pop-leave-active {
  transition: opacity 0.22s ease, transform 0.28s cubic-bezier(0.22, 1, 0.36, 1);
}
.chat-pop-enter-from,
.chat-pop-leave-to { opacity: 0; transform: translateY(18px) scale(0.97); }

/* ---- dark mode ---- */
:global(html.dark) .chat-window {
  background: var(--surface-2);
  border-color: var(--border-color);
}

@media (max-width: 520px) {
  .chat-window {
    right: 12px;
    left: 12px;
    bottom: 88px;
    width: auto;
    height: min(74vh, 500px);
  }
}

@media (prefers-reduced-motion: reduce) {
  .chat-pop-enter-active,
  .chat-pop-leave-active,
  .head-btn { transition: none; }
}
</style>
