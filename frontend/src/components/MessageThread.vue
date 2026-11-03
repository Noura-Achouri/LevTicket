<template>
  <div class="thread" :class="{ compact }">
    <!-- Listing context. Only in the floating window: the inbox pane header
         already names the ticket, so repeating it there would be noise. -->
    <router-link v-if="compact && ticketId" class="thread-context" :to="`/auction/${ticketId}`">
      <span class="context-ico" aria-hidden="true">🎟</span>
      <span class="context-copy">
        <small>{{ $t('About') }}</small>
        <b>{{ contextLabel || $t('View listing') }}</b>
      </span>
      <span class="context-go" aria-hidden="true">→</span>
    </router-link>

    <div ref="scroller" v-loading="loading" class="thread-body" @scroll.passive="onScroll">
      <div v-if="!loading && !messages.length" class="thread-empty">
        <span class="thread-empty-ico" aria-hidden="true">💬</span>
        <strong>{{ $t('No messages yet') }}</strong>
        <p>{{ $t('Say hello to start the conversation.') }}</p>
        <div v-if="quickReplies.length" class="quick-replies">
          <span class="quick-label">{{ $t('Quick replies') }}</span>
          <button
            v-for="reply in quickReplies"
            :key="reply"
            type="button"
            class="quick-chip"
            @click="useQuickReply(reply)"
          >
            {{ $t(reply) }}
          </button>
        </div>
      </div>

      <template v-for="item in renderItems" :key="item.key">
        <div v-if="item.kind === 'day'" class="day-sep">
          <span>{{ item.label }}</span>
        </div>

        <div v-else class="row" :class="{ mine: item.mine }">
          <!-- Only the last bubble of a run carries the avatar, so short
               exchanges stay visually quiet instead of repeating faces. -->
          <span class="row-avatar" :class="{ ghosted: !item.lastInGroup }" aria-hidden="true">
            {{ initials(item.mine ? myName : partnerName) }}
          </span>
          <div class="row-body">
            <div
              class="bubble"
              :class="{ grouped: !item.firstInGroup }"
              :title="fullStamp(item.message.createTime)"
            >
              {{ item.message.content }}
            </div>
            <span v-if="item.lastInGroup" class="stamp">
              <button
                type="button"
                class="copy-btn"
                :title="$t('Copy')"
                :aria-label="$t('Copy')"
                @click="copyMessage(item.message.content)"
              >
                ⧉
              </button>
              {{ formatStamp(item.message.createTime) }}
              <i v-if="item.mine" class="tick" :class="{ read: item.message.read }">
                {{ item.message.read ? '✓✓' : '✓' }}
              </i>
            </span>
          </div>
        </div>
      </template>
    </div>

    <button
      v-if="messages.length && !atBottom"
      type="button"
      class="jump-btn"
      @click="scrollToEnd(true)"
    >
      <span class="jump-ico" aria-hidden="true">↓</span>
      <b v-if="unseen" class="jump-count">{{ unseen }}</b>
      <span class="jump-label">{{ $t('Jump to latest') }}</span>
    </button>

    <div class="composer">
      <div class="composer-field">
        <el-input
          ref="inputRef"
          v-model="draft"
          type="textarea"
          resize="none"
          :autosize="{ minRows: 1, maxRows: 5 }"
          maxlength="1000"
          :placeholder="$t('Write a message…')"
          @keydown="onKeydown"
        />
        <span v-if="draft.length > MAX_LENGTH - 200" class="counter">
          {{ draft.length }}/{{ MAX_LENGTH }}
        </span>
      </div>
      <button
        type="button"
        class="send-btn"
        :disabled="!canSend"
        :title="$t('Send')"
        :aria-label="$t('Send')"
        @click="submit"
      >
        <span class="send-ico" aria-hidden="true">➤</span>
      </button>
    </div>

    <p v-if="!compact" class="composer-hint">{{ $t('Send hint') }}</p>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { getThread, sendMessage } from '@/api/chat'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { t, locale } from '@/i18n'

const POLL_INTERVAL = 5000
const MAX_LENGTH = 1000

/** Canned openers — the three things every ticket conversation starts with. */
const QUICK_REPLIES = [
  'Hi! Is this ticket still available?',
  'How would you transfer the ticket?',
  'Would you accept a different price?'
]

const props = defineProps({
  partnerId: { type: Number, required: true },
  partnerName: { type: String, default: '' },
  ticketId: { type: Number, default: null },
  ticketLabel: { type: String, default: '' },
  compact: { type: Boolean, default: false }
})

const emit = defineEmits(['loaded', 'new-messages'])

const auth = useAuthStore()
const chat = useChatStore()

const messages = ref([])
const draft = ref('')
const loading = ref(true)
const sending = ref(false)
const scroller = ref(null)
const inputRef = ref(null)
const resolvedLabel = ref('')
const atBottom = ref(true)
const unseen = ref(0)
const quickReplies = QUICK_REPLIES

/** Ids we have already rendered - lets a silent poll tell "new" from "same". */
const seenIds = new Set()
let poller = null

const myName = computed(() => auth.user?.username || t('You'))
const contextLabel = computed(() => props.ticketLabel || resolvedLabel.value)
const canSend = computed(() => draft.value.trim().length > 0 && !sending.value)

function isMine(message) {
  return Number(message.senderId) === Number(auth.user?.id)
}

function initials(value) {
  const text = String(value || '').trim()
  if (!text) return '?'
  return text.replace(/[^A-Za-z0-9]/g, '').slice(0, 2).toUpperCase() || text.charAt(0).toUpperCase()
}

function dayKey(value) {
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? 'unknown' : date.toDateString()
}

function dayLabel(value) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(today.getDate() - 1)
  if (date.toDateString() === today.toDateString()) return t('Today')
  if (date.toDateString() === yesterday.toDateString()) return t('Yesterday')
  return new Intl.DateTimeFormat(locale.value, { month: 'short', day: 'numeric', year: 'numeric' }).format(date)
}

function formatStamp(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const time = date.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' })
  const sameDay = date.toDateString() === new Date().toDateString()
  return sameDay ? time : `${date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })} ${time}`
}

function fullStamp(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return new Intl.DateTimeFormat(locale.value, {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(date)
}

/**
 * Flattens the raw list into renderable items: day separators plus messages
 * annotated with their position inside a run from the same sender.
 */
const renderItems = computed(() => {
  const list = messages.value
  const items = []
  let prevDay = null
  let prevSender = null

  list.forEach((message, index) => {
    const day = dayKey(message.createTime)
    if (day !== prevDay) {
      items.push({ kind: 'day', key: `day-${day}-${index}`, label: dayLabel(message.createTime) })
      prevDay = day
      prevSender = null
    }

    const next = list[index + 1]
    const sameAsPrevious = prevSender !== null && Number(prevSender) === Number(message.senderId)
    const sameAsNext =
      !!next &&
      Number(next.senderId) === Number(message.senderId) &&
      dayKey(next.createTime) === day

    items.push({
      kind: 'message',
      key: message.id ?? `msg-${index}`,
      message,
      mine: isMine(message),
      firstInGroup: !sameAsPrevious,
      lastInGroup: !sameAsNext
    })

    prevSender = message.senderId
  })

  return items
})

function onScroll() {
  const el = scroller.value
  if (!el) return
  atBottom.value = el.scrollHeight - el.scrollTop - el.clientHeight < 32
  if (atBottom.value) unseen.value = 0
}

function scrollToEnd(smooth = false) {
  nextTick(() => {
    const el = scroller.value
    if (!el) return
    if (smooth) el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' })
    else el.scrollTop = el.scrollHeight
    atBottom.value = true
    unseen.value = 0
  })
}

async function load({ silent = false } = {}) {
  if (!silent) loading.value = true
  try {
    const res = await getThread(props.partnerId)
    const next = res.data?.messages || []
    const fresh = next.filter((message) => !seenIds.has(message.id))
    // A first load is not "new mail", it is just the backlog.
    const isBacklog = seenIds.size === 0

    next.forEach((message) => seenIds.add(message.id))
    messages.value = next
    resolvedLabel.value = next.find((message) => message.ticketEventName)?.ticketEventName || ''
    chat.setUnread(res.data?.unreadCount)

    if (res.data?.partner) emit('loaded', res.data.partner)

    if (fresh.length && !isBacklog) {
      if (atBottom.value) scrollToEnd()
      else unseen.value += fresh.filter((message) => !isMine(message)).length
      if (silent) emit('new-messages')
    }
  } catch {
    if (!silent) messages.value = []
  } finally {
    loading.value = false
    if (!silent) scrollToEnd()
  }
}

function onKeydown(event) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    submit()
  }
}

function useQuickReply(reply) {
  draft.value = t(reply)
  nextTick(() => inputRef.value?.focus())
}

async function copyMessage(text) {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(t('Message copied'))
  } catch {
    ElMessage.error(t('Could not copy message'))
  }
}

async function submit() {
  const text = draft.value.trim().slice(0, MAX_LENGTH)
  if (!text || sending.value) return

  sending.value = true
  try {
    const res = await sendMessage(props.partnerId, text, props.ticketId)
    if (res.data?.message) {
      messages.value.push(res.data.message)
      seenIds.add(res.data.message.id)
    }
    draft.value = ''
    scrollToEnd()
  } catch (error) {
    ElMessage.error(error?.message || t('Could not send your message'))
  } finally {
    sending.value = false
  }
}

function startPolling() {
  if (poller) return
  poller = setInterval(() => load({ silent: true }), POLL_INTERVAL)
}

function stopPolling() {
  if (poller) {
    clearInterval(poller)
    poller = null
  }
}

watch(() => props.partnerId, () => {
  messages.value = []
  seenIds.clear()
  unseen.value = 0
  atBottom.value = true
  loading.value = true
  load()
})

onMounted(() => {
  load()
  startPolling()
})

onBeforeUnmount(stopPolling)
</script>

<style scoped>
.thread {
  position: relative;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  background: #fff;
}

.thread-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 18px 16px 14px;
  background:
    radial-gradient(120% 55% at 50% 0%, rgba(59, 130, 246, 0.08), transparent 62%),
    linear-gradient(180deg, #f8fbff, #f4f7fb);
  scroll-behavior: smooth;
  overscroll-behavior: contain;
}

/* ---- empty state ---- */
.thread-empty {
  margin: auto;
  display: grid;
  gap: 6px;
  justify-items: center;
  text-align: center;
  padding: 10px 6px 18px;
  color: var(--text-muted, #667085);
}
.thread-empty-ico {
  display: grid;
  place-items: center;
  width: 54px;
  height: 54px;
  margin-bottom: 4px;
  border-radius: 50%;
  background: linear-gradient(140deg, #e0edff, #cfe0fb);
  font-size: 1.5rem;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.9), 0 8px 18px rgba(37, 99, 235, 0.14);
}
.thread-empty strong { color: var(--text-strong, #111827); font-size: 0.95rem; font-weight: 800; }
.thread-empty p { margin: 0; font-size: 0.84rem; line-height: 1.5; }

.quick-replies {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
  margin-top: 14px;
}
.quick-label {
  font-size: 0.66rem;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--text-soft, #98a2b3);
}
.quick-chip {
  max-width: 100%;
  padding: 8px 14px;
  border: 1px solid rgba(37, 99, 235, 0.24);
  border-radius: 999px;
  background: #fff;
  color: #1d4ed8;
  font-family: inherit;
  font-size: 0.8rem;
  font-weight: 600;
  text-align: left;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.05);
  transition: background 0.18s ease, transform 0.18s ease, border-color 0.18s ease;
}
.quick-chip:hover {
  background: #eff6ff;
  border-color: rgba(37, 99, 235, 0.45);
  transform: translateY(-1px);
}

/* ---- day separator ---- */
.day-sep {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 12px 0 8px;
}
.day-sep::before,
.day-sep::after {
  content: '';
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(148, 163, 184, 0.42), transparent);
}
.day-sep span {
  flex: none;
  padding: 3px 11px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.26);
  color: var(--text-muted, #667085);
  font-size: 0.68rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  white-space: nowrap;
}

/* ---- message rows ---- */
.row {
  display: flex;
  align-items: flex-end;
  gap: 9px;
  max-width: 100%;
  margin-top: 10px;
}
.row.mine { flex-direction: row-reverse; }

.row-avatar {
  flex-shrink: 0;
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(140deg, #94a3b8, #64748b);
  color: #fff;
  font-size: 0.68rem;
  font-weight: 800;
  letter-spacing: 0.02em;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.45), 0 3px 8px rgba(15, 23, 42, 0.12);
}
.row.mine .row-avatar { background: linear-gradient(140deg, #3b82f6, #1d4ed8); }
/* keeps the gutter aligned without repeating the face on every bubble */
.row-avatar.ghosted { visibility: hidden; }

.row-body {
  display: grid;
  gap: 3px;
  min-width: 0;
  max-width: min(76%, 520px);
}
.row.mine .row-body { justify-items: end; }

.bubble {
  position: relative;
  padding: 10px 14px;
  border-radius: 18px 18px 18px 5px;
  background: #fff;
  border: 1px solid rgba(148, 163, 184, 0.32);
  color: var(--text-strong, #1f2937);
  font-size: 0.885rem;
  line-height: 1.52;
  overflow-wrap: anywhere;
  white-space: pre-wrap;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
}
/* tightened corners for the middle bubbles of a run */
.bubble.grouped { border-top-left-radius: 6px; }
.row.mine .bubble {
  border-radius: 18px 18px 5px 18px;
  border-color: transparent;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  color: #fff;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.26);
}
.row.mine .bubble.grouped { border-top-right-radius: 6px; }

.stamp {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 0 4px;
  color: #98a2b3;
  font-size: 0.68rem;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
.tick { font-style: normal; color: #cbd5e1; }
.tick.read { color: #2563eb; }
.row.mine .stamp .tick.read { color: #1d4ed8; }

.copy-btn {
  display: grid;
  place-items: center;
  width: 18px;
  height: 18px;
  padding: 0;
  border: 0;
  border-radius: 5px;
  background: transparent;
  color: #98a2b3;
  font-family: inherit;
  font-size: 0.8rem;
  line-height: 1;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.15s ease, background 0.15s ease, color 0.15s ease;
}
.row:hover .copy-btn { opacity: 1; }
.copy-btn:hover { background: rgba(37, 99, 235, 0.12); color: #2563eb; }
.copy-btn:focus-visible { opacity: 1; outline: 2px solid #93c5fd; }

/* ---- jump to latest ---- */
.jump-btn {
  position: absolute;
  left: 50%;
  bottom: 92px;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 7px 14px;
  transform: translateX(-50%);
  border: 1px solid rgba(148, 163, 184, 0.34);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.96);
  color: #1d4ed8;
  font-family: inherit;
  font-size: 0.74rem;
  font-weight: 700;
  white-space: nowrap;
  cursor: pointer;
  backdrop-filter: blur(8px);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.16);
  animation: jumpIn 0.2s ease;
}
.jump-btn:hover { background: #fff; }
.jump-ico { font-size: 0.85rem; }
.jump-count {
  display: grid;
  place-items: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: #2563eb;
  color: #fff;
  font-size: 0.66rem;
}
@keyframes jumpIn {
  from { opacity: 0; transform: translate(-50%, 8px); }
  to   { opacity: 1; transform: translate(-50%, 0); }
}

/* ---- listing context strip (floating window only) ---- */
.thread-context {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 10px 14px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.22);
  background: linear-gradient(180deg, #f7faff, #fff);
  text-decoration: none;
  transition: background 0.18s ease;
}
.thread-context:hover { background: #eef5ff; }
.context-ico {
  display: grid;
  place-items: center;
  flex: none;
  width: 34px;
  height: 34px;
  border-radius: 11px;
  background: linear-gradient(140deg, #dbeafe, #bfdbfe);
  font-size: 1rem;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.85);
}
.context-copy { display: grid; gap: 1px; min-width: 0; flex: 1; }
.context-copy small {
  color: var(--text-soft, #98a2b3);
  font-size: 0.63rem;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}
.context-copy b {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-strong, #1f2937);
  font-size: 0.8rem;
  font-weight: 700;
}
.context-go {
  flex: none;
  color: #2563eb;
  font-size: 0.9rem;
  font-weight: 800;
  transition: transform 0.18s ease;
}
.thread-context:hover .context-go { transform: translateX(3px); }

/* ---- composer ---- */
.composer {
  display: flex;
  align-items: flex-end;
  gap: 9px;
  padding: 12px 14px 10px;
  border-top: 1px solid rgba(148, 163, 184, 0.22);
  background: #fff;
}

.composer-field {
  position: relative;
  flex: 1;
  min-width: 0;
}

.composer :deep(.el-textarea__inner) {
  border-radius: 18px;
  box-shadow: none;
  border: 1px solid rgba(148, 163, 184, 0.34);
  background: #f5f8fc;
  color: var(--text-strong, #1f2937);
  font-family: inherit;
  font-size: 0.885rem;
  line-height: 1.5;
  padding: 10px 14px;
  transition: border-color 0.18s ease, background 0.18s ease, box-shadow 0.18s ease;
}
.composer :deep(.el-textarea__inner::placeholder) { color: #98a2b3; }
.composer :deep(.el-textarea__inner:hover) { border-color: rgba(37, 99, 235, 0.34); }
.composer :deep(.el-textarea__inner:focus) {
  background: #fff;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
}

.counter {
  position: absolute;
  right: 12px;
  bottom: 6px;
  color: #b45309;
  font-size: 0.66rem;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  pointer-events: none;
}

.send-btn {
  display: inline-grid;
  place-items: center;
  flex-shrink: 0;
  width: 42px;
  height: 42px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: linear-gradient(135deg, #1d4ed8, #3b82f6);
  color: #fff;
  font-family: inherit;
  cursor: pointer;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.32), inset 0 1px 0 rgba(255, 255, 255, 0.3);
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}
.send-btn:hover:not(:disabled) {
  transform: translateY(-2px) scale(1.04);
  box-shadow: 0 14px 26px rgba(37, 99, 235, 0.42), inset 0 1px 0 rgba(255, 255, 255, 0.34);
}
.send-btn:active:not(:disabled) { transform: translateY(0) scale(0.98); }
.send-btn:disabled { opacity: 0.45; cursor: not-allowed; box-shadow: none; }
.send-ico { font-size: 0.95rem; line-height: 1; transform: translateX(1px); }

.composer-hint {
  margin: 0;
  padding: 0 16px 10px;
  background: #fff;
  color: var(--text-soft, #98a2b3);
  font-size: 0.7rem;
}

/* ---- compact (floating window) ---- */
.thread.compact .thread-body { padding: 12px 12px 10px; }
.thread.compact .row-body { max-width: 82%; }
.thread.compact .bubble { font-size: 0.85rem; padding: 9px 12px; }
.thread.compact .row { gap: 8px; margin-top: 9px; }
.thread.compact .row-avatar { width: 28px; height: 28px; font-size: 0.62rem; }
.thread.compact .day-sep { margin: 10px 0 6px; }
.thread.compact .composer { padding: 10px 12px; }
.thread.compact .send-btn { width: 38px; height: 38px; }
.thread.compact .jump-btn { bottom: 78px; }
.thread.compact .quick-chip { font-size: 0.76rem; padding: 7px 12px; }

/* ---- dark mode ---- */
:global(html.dark) .thread,
:global(html.dark) .composer,
:global(html.dark) .composer-hint { background: var(--surface-2); }
:global(html.dark) .thread-body {
  background:
    radial-gradient(120% 55% at 50% 0%, rgba(59, 130, 246, 0.16), transparent 62%),
    var(--surface-muted);
}
:global(html.dark) .thread-context,
:global(html.dark) .composer { border-color: var(--border-color); }
:global(html.dark) .thread-context { background: var(--surface-2); }
:global(html.dark) .thread-context:hover { background: rgba(37, 99, 235, 0.16); }
:global(html.dark) .context-copy b { color: var(--text-strong); }
:global(html.dark) .context-ico { background: rgba(59, 130, 246, 0.22); }

:global(html.dark) .bubble {
  background: var(--surface-2);
  border-color: var(--border-color);
  color: var(--text-strong);
}
:global(html.dark) .row.mine .bubble { background: linear-gradient(135deg, #1d4ed8, #2563eb); color: #fff; }

:global(html.dark) .thread-empty-ico { background: linear-gradient(140deg, #1e3a8a, #1d4ed8); }
:global(html.dark) .thread-empty strong { color: var(--text-strong); }
:global(html.dark) .quick-chip {
  background: var(--surface-2);
  border-color: rgba(59, 130, 246, 0.4);
  color: #93c5fd;
}
:global(html.dark) .quick-chip:hover { background: rgba(37, 99, 235, 0.2); }

:global(html.dark) .day-sep span {
  background: var(--surface-2);
  border-color: var(--border-color);
  color: var(--text-muted);
}
:global(html.dark) .stamp { color: var(--text-soft); }

:global(html.dark) .jump-btn {
  background: var(--surface-2);
  border-color: var(--border-color);
  color: #93c5fd;
}
:global(html.dark) .jump-btn:hover { background: var(--surface-3, #1f3050); }

:global(html.dark) .composer :deep(.el-textarea__inner) {
  background: var(--surface-muted);
  border-color: var(--border-color);
  color: var(--text-strong);
}
:global(html.dark) .composer :deep(.el-textarea__inner::placeholder) { color: var(--text-soft); }
:global(html.dark) .composer :deep(.el-textarea__inner:focus) {
  background: var(--surface-2);
  border-color: #3b82f6;
}
</style>
