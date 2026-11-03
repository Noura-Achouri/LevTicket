<template>
  <div class="messages-page">
    <header class="msg-hero">
      <div class="hero-copy">
        <span class="section-kicker">{{ $t('Inbox') }}</span>
        <h1>{{ $t('Messages') }}</h1>
        <p>{{ $t('Chat privately with buyers and sellers about any ticket, before or after the auction.') }}</p>
      </div>
      <div v-if="chat.unreadCount" class="hero-badge">
        <strong>{{ chat.unreadCount }}</strong>
        <span>{{ $t('unread') }}</span>
      </div>
    </header>

    <div class="msg-shell">
      <aside v-loading="loading" class="conv-list">
        <button
          v-for="conversation in conversations"
          :key="conversation.partnerId"
          type="button"
          :class="['conv-item', { active: isActive(conversation) }]"
          @click="select(conversation)"
        >
          <span class="conv-avatar">{{ initials(conversation.partnerUsername) }}</span>
          <div class="conv-copy">
            <div class="conv-top">
              <strong>{{ conversation.partnerUsername }}</strong>
              <small>{{ relativeTime(conversation.lastMessageTime) }}</small>
            </div>
            <p class="conv-preview">
              <em v-if="conversation.outgoing">{{ $t('You') }}: </em>{{ conversation.lastMessage }}
            </p>
            <small v-if="conversation.ticketEventName" class="conv-ticket">
              🎟 {{ conversation.ticketEventName }}
            </small>
          </div>
          <b v-if="conversation.unread" class="conv-unread">{{ conversation.unread }}</b>
        </button>

        <el-empty
          v-if="!loading && !conversations.length"
          :description="$t('No conversations yet. Open a listing and message the seller.')"
          :image-size="64"
        />
      </aside>

      <section class="msg-pane">
        <template v-if="active">
          <header class="pane-head">
            <span class="conv-avatar">{{ initials(activeName) }}</span>
            <div class="pane-copy">
              <strong>{{ activeName || $t('Member') }}</strong>
              <small v-if="activeTicketLabel">{{ activeTicketLabel }}</small>
              <small v-else>{{ $t('Private conversation') }}</small>
            </div>
            <router-link
              v-if="active.ticketId"
              class="pane-link"
              :to="`/auction/${active.ticketId}`"
            >
              {{ $t('View auction →') }}
            </router-link>
          </header>

          <MessageThread
            :key="active.partnerId"
            :partner-id="Number(active.partnerId)"
            :partner-name="activeName"
            :ticket-id="active.ticketId ? Number(active.ticketId) : null"
            :ticket-label="activeTicketLabel"
            @loaded="onThreadLoaded"
            @new-messages="loadConversations"
          />
        </template>

        <div v-else class="pane-empty">
          <span class="pane-empty-ico">💬</span>
          <h3>{{ $t('Select a conversation') }}</h3>
          <p>{{ $t('Pick someone on the left, or open a listing and tap “Message seller”.') }}</p>
          <router-link to="/search" class="pane-cta">{{ $t('Browse tickets') }}</router-link>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MessageThread from '@/components/MessageThread.vue'
import { getInbox } from '@/api/chat'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'
import { t } from '@/i18n'

const route = useRoute()
const router = useRouter()
const chat = useChatStore()
const auth = useAuthStore()

const conversations = ref([])
const loading = ref(true)
const active = ref(null)

const activeName = computed(() => active.value?.partnerUsername || '')
const activeTicketLabel = computed(() => active.value?.ticketEventName || '')

function isActive(conversation) {
  return Number(conversation.partnerId) === Number(active.value?.partnerId)
}

function initials(value) {
  const text = String(value || '').trim()
  if (!text) return '?'
  return text.replace(/[^A-Za-z0-9]/g, '').slice(0, 2).toUpperCase() || text.charAt(0).toUpperCase()
}

function relativeTime(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const diff = Date.now() - date.getTime()
  if (diff < 60000) return t('now')
  if (diff < 3600000) return `${Math.floor(diff / 60000)}m`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}h`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}d`
  return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })
}

async function loadConversations() {
  try {
    const res = await getInbox()
    conversations.value = res.data?.conversations || []
    chat.setUnread(res.data?.unreadCount)

    // Keep the open thread in sync with the refreshed row.
    if (active.value) {
      const fresh = conversations.value.find((c) => Number(c.partnerId) === Number(active.value.partnerId))
      if (fresh) active.value = { ...active.value, ...fresh }
    }
  } catch {
    conversations.value = []
  } finally {
    loading.value = false
  }
}

function select(conversation) {
  active.value = { ...conversation }
  if (route.query.with) router.replace({ path: '/messages' })
}

/** Deep link: /messages?with=<userId> opens (or creates) that thread. */
function applyDeepLink() {
  const id = Number(route.query.with)
  if (!id || Number.isNaN(id)) return
  const found = conversations.value.find((c) => Number(c.partnerId) === id)
  active.value = found ? { ...found } : { partnerId: id, partnerUsername: '', ticketId: null, ticketEventName: '' }
}

function onThreadLoaded(partner) {
  if (!active.value || !partner) return
  if (!active.value.partnerUsername && partner.username) {
    active.value = { ...active.value, partnerUsername: partner.username }
  }
}

onMounted(async () => {
  // The thread itself also needs a valid session for the header badge.
  if (auth.token) chat.loadUnread()
  await loadConversations()
  applyDeepLink()
})
</script>

<style scoped>
.messages-page {
  display: grid;
  gap: 20px;
}

.msg-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 26px 30px;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 22px;
  background:
    radial-gradient(120% 180% at 12% -30%, rgba(59, 130, 246, 0.22), transparent 60%),
    linear-gradient(150deg, #ffffff 0%, #eff6ff 100%);
  box-shadow: inset 0 1px 0 #fff, 0 16px 38px rgba(30, 64, 175, 0.12);
}
.hero-copy { display: grid; gap: 8px; }
.section-kicker {
  justify-self: start;
  padding: 5px 12px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.12);
  color: #1d4ed8;
  font-size: 0.7rem;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}
.msg-hero h1 {
  margin: 0;
  color: #0f172a;
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: -0.03em;
}
.msg-hero p {
  max-width: 560px;
  margin: 0;
  color: #475467;
  font-size: 0.92rem;
  line-height: 1.6;
}
.hero-badge {
  display: grid;
  place-items: center;
  gap: 1px;
  flex-shrink: 0;
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: linear-gradient(150deg, #f43f5e, #be123c);
  color: #fff;
  box-shadow: inset 0 2px 6px rgba(255, 255, 255, 0.35), 0 14px 30px rgba(225, 29, 72, 0.35);
}
.hero-badge strong { font-size: 1.7rem; font-weight: 800; line-height: 1; }
.hero-badge span { font-size: 0.66rem; font-weight: 700; letter-spacing: 0.08em; text-transform: uppercase; opacity: 0.9; }

.msg-shell {
  display: grid;
  grid-template-columns: minmax(260px, 340px) minmax(0, 1fr);
  gap: 18px;
  align-items: stretch;
}

.conv-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 320px;
  max-height: 620px;
  overflow-y: auto;
  padding: 12px;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 10px 26px rgba(15, 23, 42, 0.07);
}

.conv-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 11px;
  width: 100%;
  padding: 11px;
  border: 1px solid transparent;
  border-radius: 14px;
  background: transparent;
  text-align: left;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease, transform 0.2s ease;
}
.conv-item:hover { background: #f1f5f9; transform: translateY(-1px); }
.conv-item.active {
  border-color: rgba(37, 99, 235, 0.35);
  background: linear-gradient(135deg, #eff6ff, #e0edff);
}

.conv-avatar {
  flex-shrink: 0;
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: linear-gradient(140deg, #3b82f6, #1d4ed8);
  color: #fff;
  font-size: 0.76rem;
  font-weight: 800;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.conv-copy { display: grid; gap: 3px; min-width: 0; flex: 1; }
.conv-top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
}
.conv-top strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-strong, #111827);
  font-size: 0.9rem;
  font-weight: 800;
}
.conv-top small { flex-shrink: 0; color: #98a2b3; font-size: 0.7rem; font-weight: 700; }

.conv-preview {
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #667085;
  font-size: 0.8rem;
}
.conv-preview em { font-style: normal; font-weight: 700; color: #98a2b3; }

.conv-ticket {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #2563eb;
  font-size: 0.7rem;
  font-weight: 600;
}

.conv-unread {
  align-self: center;
  flex-shrink: 0;
  min-width: 20px;
  padding: 3px 6px;
  border-radius: 999px;
  background: linear-gradient(135deg, #f43f5e, #e11d48);
  color: #fff;
  font-size: 0.66rem;
  font-weight: 800;
  text-align: center;
  box-shadow: 0 2px 6px rgba(225, 29, 72, 0.4);
}

.msg-pane {
  display: flex;
  flex-direction: column;
  min-height: 420px;
  max-height: 620px;
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 10px 26px rgba(15, 23, 42, 0.07);
}

.pane-head {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 13px 18px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.22);
}
.pane-copy { display: grid; gap: 1px; min-width: 0; flex: 1; }
.pane-copy strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-strong, #111827);
  font-size: 0.98rem;
  font-weight: 800;
}
.pane-copy small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #98a2b3;
  font-size: 0.74rem;
  font-weight: 600;
}
.pane-link {
  flex-shrink: 0;
  color: #2563eb;
  font-size: 0.8rem;
  font-weight: 700;
  text-decoration: none;
}
.pane-link:hover { text-decoration: underline; }

.pane-empty {
  display: grid;
  gap: 8px;
  justify-items: center;
  align-content: center;
  flex: 1;
  padding: 40px 24px;
  text-align: center;
}
.pane-empty-ico { font-size: 2.2rem; }
.pane-empty h3 { margin: 0; color: var(--text-strong, #111827); font-size: 1.05rem; font-weight: 800; }
.pane-empty p { max-width: 340px; margin: 0; color: var(--text-muted, #667085); font-size: 0.86rem; line-height: 1.6; }
.pane-cta {
  margin-top: 8px;
  padding: 10px 22px;
  border-radius: 999px;
  background: linear-gradient(135deg, #1d4ed8, #3b82f6);
  color: #fff;
  font-size: 0.85rem;
  font-weight: 700;
  text-decoration: none;
  box-shadow: 0 12px 24px rgba(37, 99, 235, 0.3);
}
.pane-cta:hover { transform: translateY(-2px); }

@media (max-width: 860px) {
  .msg-shell { grid-template-columns: 1fr; }
  .conv-list { max-height: 300px; }
  .msg-pane { max-height: 560px; }
  .msg-hero { flex-direction: column; align-items: flex-start; padding: 22px; }
}

/* ---- dark mode ---- */
:global(html.dark) .msg-hero {
  background:
    radial-gradient(120% 180% at 12% -30%, rgba(59, 130, 246, 0.24), transparent 60%),
    linear-gradient(150deg, var(--surface-2) 0%, var(--surface-muted) 100%);
  border-color: var(--border-color);
}
:global(html.dark) .msg-hero h1,
:global(html.dark) .conv-top strong { color: var(--text-strong); }
:global(html.dark) .msg-hero p,
:global(html.dark) .conv-preview { color: var(--text-muted); }
:global(html.dark) .section-kicker { background: rgba(59, 130, 246, 0.2); color: #93c5fd; }
:global(html.dark) .conv-list,
:global(html.dark) .msg-pane { background: var(--surface-2); border-color: var(--border-color); }
:global(html.dark) .conv-item:hover { background: var(--surface-muted); }
:global(html.dark) .conv-item.active { background: linear-gradient(135deg, rgba(37, 99, 235, 0.24), rgba(59, 130, 246, 0.12)); }
:global(html.dark) .pane-head { border-color: var(--border-color); }
</style>
