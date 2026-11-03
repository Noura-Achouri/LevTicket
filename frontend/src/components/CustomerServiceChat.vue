<template>
  <div class="cs-chat">
    <el-drawer v-model="open" :title="$t('Customer Service')" size="360px">

      <!-- AI header with 3D dragon -->
      <div class="ai-header">
        <div
          class="ai-avatar"
          :style="avatarStyle"
          @mousemove="onAvatarMove"
          @mouseleave="onAvatarLeave"
        >
          <div class="avatar-ring" />
          <canvas ref="avatarCanvas" class="avatar-canvas" />
        </div>

        <div class="ai-meta">
          <div class="ai-name">LevTicket AI</div>
          <div class="ai-status">
            <span class="status-dot" />
            <span>{{ $t('Online · Auto reply') }}</span>
          </div>
        </div>

        <div class="ai-actions">
          <button class="icon-btn" :title="$t('New chat')" @click="resetChat">
            <el-icon><Refresh /></el-icon>
          </button>
        </div>
      </div>

      <!-- Welcome message -->
      <div v-if="messages.length === 0" class="welcome">
        <h3>{{ greetingTitle }}</h3>
        <p>
          {{ greetingSub }}
        </p>

        <div class="quick-questions">
          <button
            v-for="(q, i) in quickList"
            :key="i"
            class="quick-btn"
            @click="input = $t(q)"
          >
            <el-icon><ChatLineRound /></el-icon>
            <span>{{ $t(q) }}</span>
          </button>
        </div>
      </div>

      <!-- Messages -->
      <div ref="messagesEl" class="messages">
        <div
          v-for="(msg, i) in messages"
          :key="i"
          :class="['msg', msg.from]"
        >
          <div v-if="msg.from === 'ai'" class="message-avatar">
            <img src="/ai-avatar.png" alt="AI" />
          </div>

          <div class="message-content">
            <div v-if="msg.attachment" class="msg-attach">
              <el-icon><component :is="attachIcon(msg.attachment)" /></el-icon>
              <span class="msg-attach-name">{{ msg.attachment.name }}</span>
            </div>
            <div v-if="msg.text" class="msg-row">
              <div class="msg-text">{{ msg.text }}</div>
              <button
                type="button"
                class="msg-copy"
                :class="{ copied: copiedIndex === i }"
                :title="copiedIndex === i ? $t('Copied') : $t('Copy message')"
                @click="copyMessage(msg.text, i)"
              >
                <el-icon v-if="copiedIndex === i"><Check /></el-icon>
                <el-icon v-else><DocumentCopy /></el-icon>
              </button>
            </div>
          </div>
        </div>

        <div v-if="loading" class="msg ai typing">
          <div class="message-avatar">
            <img src="/ai-avatar.png" alt="AI" />
          </div>
          <div class="message-content">
            <span class="dot" />
            <span class="dot" />
            <span class="dot" />
          </div>
        </div>
      </div>

      <!-- Attachment preview -->
      <transition name="pill">
        <div v-if="attachment" class="attachment-preview">
          <div class="attachment-pill">
            <el-icon class="pill-icon"><component :is="attachIcon(attachment)" /></el-icon>
            <span class="pill-name">{{ attachment.name }}</span>
            <button
              type="button"
              class="attachment-remove"
              @click="removeAttachment"
            >×</button>
          </div>
        </div>
      </transition>

      <!-- Chat input bar -->
      <div class="chat-input-bar" :class="{ 'is-focused': inputFocused }">
        <!-- Attachment wrapper with popover menu -->
        <div class="attach-wrapper" v-click-outside="() => (showAttachMenu = false)">
          <button
            type="button"
            class="input-icon attach"
            :class="{ active: showAttachMenu }"
            :title="$t('Attach')"
            @click="showAttachMenu = !showAttachMenu"
          >
            <el-icon><Plus /></el-icon>
          </button>

          <transition name="menu">
            <div v-if="showAttachMenu" class="attach-menu">
              <button class="menu-item" @click="pick('img')">
                <span class="menu-ico ico-img">
                  <el-icon><Picture /></el-icon>
                </span>
                <div>
                  <div class="menu-title">{{ $t('Image') }}</div>
                  <div class="menu-sub">JPG, PNG, GIF…</div>
                </div>
              </button>

              <button class="menu-item" @click="pick('file')">
                <span class="menu-ico ico-file">
                  <el-icon><Document /></el-icon>
                </span>
                <div>
                  <div class="menu-title">{{ $t('File') }}</div>
                  <div class="menu-sub">PDF, DOC, ZIP…</div>
                </div>
              </button>

              <button class="menu-item" @click="pick('folder')">
                <span class="menu-ico ico-folder">
                  <el-icon><Folder /></el-icon>
                </span>
                <div>
                  <div class="menu-title">{{ $t('Folder') }}</div>
                  <div class="menu-sub">{{ $t('All files inside') }}</div>
                </div>
              </button>
            </div>
          </transition>
        </div>

        <!-- Hidden file inputs -->
        <input
          ref="imgInput"
          type="file"
          accept="image/*"
          class="hidden-file-input"
          @change="onPick($event, 'image')"
        />
        <input
          ref="fileInput"
          type="file"
          class="hidden-file-input"
          @change="onPick($event, 'file')"
        />
        <input
          ref="folderInput"
          type="file"
          webkitdirectory
          directory
          multiple
          class="hidden-file-input"
          @change="onPick($event, 'folder')"
        />

        <input
          v-model="input"
          type="text"
          class="chat-input"
          :placeholder="$t('Ask anything')"
          @focus="inputFocused = true"
          @blur="inputFocused = false"
          @keyup.enter="send"
          @paste="onPaste"
        />

        <button
          type="button"
          class="send-btn"
          :disabled="loading || (!input.trim() && !attachment)"
          @click="send"
        >
          <el-icon v-if="!loading"><Promotion /></el-icon>
          <el-icon v-else class="is-loading"><Loading /></el-icon>
        </button>
      </div>

      <div class="ai-footer">
        {{ $t('Powered by') }} <span class="brand">LevTicket AI</span>
      </div>

    </el-drawer>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, computed, watch } from 'vue'
import {
  ChatLineRound,
  Check,
  Document,
  DocumentCopy,
  Folder,
  Loading,
  Picture,
  Plus,
  Promotion,
  Refresh
} from '@element-plus/icons-vue'
import { submitComplaint } from '@/api/ai'
import { useAuthStore } from '@/stores/auth'
import { t } from '@/i18n'

// ---- State ----
const auth = useAuthStore()
const userName = computed(() => auth.user?.username?.trim() || '')
const greetingTitle = computed(() =>
  userName.value ? t('Hi {name}! 👋', { name: userName.value }) : t("Hi! I'm LevTicket AI")
)
const greetingSub = computed(() =>
  userName.value
    ? t("I'm LevTicket AI, your personal assistant. Ask me anything about tickets, auctions, orders, or customer service, {name}!", { name: userName.value })
    : t('I can help you with tickets, auctions, orders, and customer service questions.')
)
const open = ref(false)
const input = ref('')
const messages = ref([])
const loading = ref(false)
const messagesEl = ref(null)
const attachment = ref(null)
const inputFocused = ref(false)
const showAttachMenu = ref(false)
const copiedIndex = ref(-1)
let copiedTimer = null

const imgInput = ref(null)
const fileInput = ref(null)
const folderInput = ref(null)

const avatarCanvas = ref(null)

const quickList = [
  'How do I buy a ticket?',
  'How do I place a bid?',
  'How do I sell a ticket?'
]

const emit = defineEmits(['open-change'])
defineExpose({ openChat: () => (open.value = true) })
watch(open, (value) => emit('open-change', value))

function resetChat() {
  messages.value = []
  input.value = ''
  attachment.value = null
}

// ---- Auto-scroll ----
function scrollToBottom() {
  nextTick(() => {
    const el = messagesEl.value
    if (el) el.scrollTop = el.scrollHeight
  })
}
watch(messages, scrollToBottom, { deep: true })
watch(loading, scrollToBottom)

// ---- Click outside directive (local) ----
const vClickOutside = {
  mounted(el, binding) {
    el._handler = (e) => {
      if (!el.contains(e.target)) binding.value?.()
    }
    setTimeout(() => document.addEventListener('click', el._handler), 0)
  },
  unmounted(el) {
    document.removeEventListener('click', el._handler)
  }
}

// ---- File / folder attach ----
function pick(kind) {
  showAttachMenu.value = false
  if (kind === 'img') imgInput.value?.click()
  else if (kind === 'file') fileInput.value?.click()
  else if (kind === 'folder') folderInput.value?.click()
}

function onPick(event, kind) {
  const files = Array.from(event.target.files || [])
  if (!files.length) {
    event.target.value = ''
    return
  }

  if (kind === 'folder') {
    const names = [...new Set(files.map((f) => f.webkitRelativePath.split('/')[0]))]
    const first = files[0]
    attachment.value = {
      type: 'folder',
      name: names[0] || first.name,
      count: files.length,
      file: first
    }
  } else if (kind === 'image') {
    const file = files[0]
    attachment.value = { type: 'image', name: file.name, file }
  } else {
    const file = files[0]
    attachment.value = { type: 'file', name: file.name, file }
  }
  event.target.value = ''
}

function attachIcon(att) {
  if (att.type === 'image') return Picture
  if (att.type === 'folder') return Folder
  return Document
}

function onPaste(event) {
  const items = event.clipboardData?.items
  if (!items) return
  for (const item of items) {
    if (item.type.startsWith('image/')) {
      event.preventDefault()
      const file = item.getAsFile()
      if (file) {
        attachment.value = { type: 'image', name: file.name || t('Pasted image'), file }
      }
      return
    }
  }
}

function removeAttachment() {
  attachment.value = null
}

async function copyMessage(text, index) {
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
  } catch (err) {
    const ta = document.createElement('textarea')
    ta.value = text
    ta.style.position = 'fixed'
    ta.style.opacity = '0'
    document.body.appendChild(ta)
    ta.select()
    document.execCommand('copy')
    document.body.removeChild(ta)
  }
  copiedIndex.value = index
  if (copiedTimer) clearTimeout(copiedTimer)
  copiedTimer = setTimeout(() => (copiedIndex.value = -1), 1800)
}

function fileToDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

// ---- Send ----
function buildHistory() {
  return messages.value.map((msg) => ({
    role: msg.from === 'user' ? 'user' : 'assistant',
    content: msg.text
  }))
}

async function send() {
  const text = input.value.trim()
  if ((!text && !attachment.value) || loading.value) return

  const attachSnapshot = attachment.value
  let displayText = text

  if (attachSnapshot) {
    const tag =
      attachSnapshot.type === 'image'
        ? `📷 ${attachSnapshot.name}`
        : attachSnapshot.type === 'folder'
          ? `📁 ${attachSnapshot.name} (${t('{count} files', { count: attachSnapshot.count })})`
          : `📄 ${attachSnapshot.name}`
    displayText = text ? `${text}\n${tag}` : tag
  }

  messages.value.push({
    from: 'user',
    text: displayText,
    attachment: attachSnapshot
  })

  const userMsg = text
  input.value = ''
  let imageDataUrl = null
  if (attachSnapshot?.type === 'image' && attachSnapshot.file) {
    try {
      imageDataUrl = await fileToDataUrl(attachSnapshot.file)
    } catch (err) {
      console.error('Failed to read image:', err)
    }
  }
  attachment.value = null
  loading.value = true

  try {
    const res = await submitComplaint(userMsg, buildHistory(), imageDataUrl, userName.value)
    const reply = res?.reply
    messages.value.push({
      from: 'ai',
      text: reply || t('I received a response, but there was no reply from the AI.')
    })
  } catch (err) {
    console.error('AI complaint error:', err)
    let detail = err?.message
      ? String(err.message)
      : t('Sorry, I could not connect to LevTicket AI.')
    if (detail.toLowerCase().includes('timeout')) {
      detail = t('LevTicket AI is taking longer than expected. Please try again in a moment.')
    } else if (detail.includes('Network Error') || detail.includes('ERR_CONNECTION_REFUSED')) {
      detail = t('Unable to reach the server. Please make sure the backend is running.')
    }
    messages.value.push({ from: 'ai', text: detail })
  } finally {
    loading.value = false
  }
}

// ---- 3D Dragon avatar ----
let avatarRaf = null
let avatarCtx = null
let avatarState = { nx: 0, ny: 0, t: 0, hovering: false }
let dragonImg = null

const avatarStyle = ref({
  transform: 'perspective(240px) rotateX(0deg) rotateY(0deg)'
})

function onAvatarMove(e) {
  const el = e.currentTarget
  const rect = el.getBoundingClientRect()
  const nx = ((e.clientX - rect.left) / rect.width) * 2 - 1
  const ny = ((e.clientY - rect.top) / rect.height) * 2 - 1
  avatarState.nx = Math.max(-1, Math.min(1, nx))
  avatarState.ny = Math.max(-1, Math.min(1, ny))
  avatarState.hovering = true
  avatarStyle.value = {
    transform: `perspective(260px) rotateX(${(-avatarState.ny * 18).toFixed(1)}deg) rotateY(${(avatarState.nx * 18).toFixed(1)}deg) scale(1.05)`
  }
}

function onAvatarLeave() {
  avatarState.nx = 0
  avatarState.ny = 0
  avatarState.hovering = false
}

function initAvatar() {
  const canvas = avatarCanvas.value
  if (!canvas) return
  const dpr = window.devicePixelRatio || 1
  const size = Math.round(canvas.clientWidth)
  if (!size) return

  canvas.width = size * dpr
  canvas.height = size * dpr
  avatarCtx = canvas.getContext('2d')
  avatarCtx.setTransform(dpr, 0, 0, dpr, 0, 0)

  // load dragon emblem once
  if (!dragonImg) {
    dragonImg = new Image()
    dragonImg.src = '/ai-avatar.png'
  }

  if (avatarRaf) cancelAnimationFrame(avatarRaf)
  avatarRaf = requestAnimationFrame(avatarTick)
}

function avatarTick() {
  const canvas = avatarCanvas.value
  if (!canvas) return
  const size = Math.round(canvas.clientWidth)
  if (!size) return

  const ctx = avatarCtx
  const t = (avatarState.t += 0.012)
  const cx = size / 2
  const cy = size / 2
  const R = size / 2

  // Gentle continuous 3D float + tilt when idle (mouse parallax takes over on hover)
  if (!avatarState.hovering) {
    const bobY = Math.sin(t * 1.1) * 3.5
    const rotY = Math.sin(t * 0.7) * 10
    const rotX = Math.cos(t * 0.9) * 8
    avatarStyle.value = {
      transform: `perspective(260px) rotateX(${rotX.toFixed(2)}deg) rotateY(${rotY.toFixed(2)}deg) translateY(${bobY.toFixed(2)}px)`
    }
  }

  ctx.clearRect(0, 0, size, size)
  ctx.save()
  ctx.beginPath()
  ctx.arc(cx, cy, R - 1, 0, Math.PI * 2)
  ctx.clip()

  // glowing ball base (website navy/blue)
  const bg = ctx.createRadialGradient(cx - R * 0.35, cy - R * 0.45, R * 0.05, cx, cy, R * 1.2)
  bg.addColorStop(0, '#60a5fa')
  bg.addColorStop(0.4, '#2563eb')
  bg.addColorStop(0.85, '#0f172a')
  ctx.fillStyle = bg
  ctx.fillRect(0, 0, size, size)

  // dragon emblem
  if (dragonImg && dragonImg.complete && dragonImg.naturalWidth) {
    const iw = dragonImg.naturalWidth
    const ih = dragonImg.naturalHeight
    const sc = Math.max(size / iw, size / ih)
    const dw = iw * sc
    const dh = ih * sc
    ctx.drawImage(dragonImg, cx - dw / 2, cy - dh / 2, dw, dh)
  } else {
    // fallback emblem while the image loads
    ctx.fillStyle = 'rgba(255,255,255,0.92)'
    ctx.font = `bold ${R * 0.85}px Arial, sans-serif`
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText('L', cx, cy + R * 0.06)
  }

  // rotating light sweep (3D movement)
  if (ctx.createConicGradient) {
    try {
      const sweep = ctx.createConicGradient(-t * 1.3, cx, cy)
      sweep.addColorStop(0, 'rgba(255,255,255,0)')
      sweep.addColorStop(0.1, 'rgba(255,255,255,0.16)')
      sweep.addColorStop(0.2, 'rgba(255,255,255,0.02)')
      sweep.addColorStop(1, 'rgba(255,255,255,0)')
      ctx.fillStyle = sweep
      ctx.fillRect(0, 0, size, size)
    } catch (err) {
      /* ignore */
    }
  }

  // mouse-tracked specular highlight
  const hx = cx + avatarState.nx * R * 0.6
  const hy = cy + avatarState.ny * R * 0.6
  const sg = ctx.createRadialGradient(hx, hy, 0, hx, hy, R * 1.05)
  sg.addColorStop(0, 'rgba(255,255,255,0.3)')
  sg.addColorStop(0.5, 'rgba(255,255,255,0.07)')
  sg.addColorStop(1, 'rgba(255,255,255,0)')
  ctx.fillStyle = sg
  ctx.fillRect(0, 0, size, size)

  // sphere depth shading (bottom-right darker)
  const dg = ctx.createRadialGradient(
    cx + R * 0.45,
    cy + R * 0.45,
    R * 0.1,
    cx + R * 0.1,
    cy + R * 0.1,
    R * 1.55
  )
  dg.addColorStop(0, 'rgba(2, 6, 23, 0)')
  dg.addColorStop(0.6, 'rgba(2, 6, 23, 0.22)')
  dg.addColorStop(1, 'rgba(2, 6, 23, 0.55)')
  ctx.fillStyle = dg
  ctx.fillRect(0, 0, size, size)

  ctx.restore()

  // glossy rim
  ctx.beginPath()
  ctx.arc(cx, cy, R - 0.8, 0, Math.PI * 2)
  ctx.lineWidth = 1.5
  const rim = ctx.createLinearGradient(0, 0, size, size)
  rim.addColorStop(0, '#93c5fd')
  rim.addColorStop(1, '#1d4ed8')
  ctx.strokeStyle = rim
  ctx.stroke()

  // orbiting glint dot
  const gx = cx + Math.cos(t * 1.3) * R * 0.6
  const gy = cy + Math.sin(t * 1.3) * R * 0.6
  ctx.beginPath()
  ctx.arc(gx, gy, 1.6, 0, Math.PI * 2)
  ctx.fillStyle = 'rgba(255,255,255,0.85)'
  ctx.fill()

  avatarRaf = requestAnimationFrame(avatarTick)
}

function destroyAvatar() {
  if (avatarRaf) cancelAnimationFrame(avatarRaf)
  avatarRaf = null
}

onMounted(() => {
  nextTick(initAvatar)
})
onBeforeUnmount(destroyAvatar)

watch(open, (v) => {
  if (v) nextTick(initAvatar)
  else destroyAvatar()
})
</script>

<style scoped>
/* ---------- Header ---------- */
.ai-header {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;

  padding: 16px 16px;
  margin-bottom: 14px;

  border-radius: 18px;
  overflow: hidden;

  background:
    radial-gradient(130% 150% at 0% 0%, rgba(37, 99, 235, 0.22), transparent 55%),
    radial-gradient(100% 120% at 100% 100%, rgba(96, 165, 250, 0.18), transparent 60%),
    linear-gradient(135deg, #eef4ff, #ffffff 68%);

  border: 1px solid rgba(37, 99, 235, 0.22);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 10px 28px -14px rgba(37, 99, 235, 0.4),
    0 3px 8px rgba(15, 23, 42, 0.04);
  transition: box-shadow 0.25s ease, transform 0.25s ease;
}

/* soft accent glow ring */
.ai-header::after {
  content: '';
  position: absolute;
  right: -18px;
  top: -22px;
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(96, 165, 250, 0.28), transparent 70%);
  pointer-events: none;
}

.ai-header:hover {
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 16px 36px -14px rgba(37, 99, 235, 0.5),
    0 4px 10px rgba(15, 23, 42, 0.06);
  transform: translateY(-1px);
}

.ai-avatar {
  position: relative;
  width: 56px;
  height: 56px;
  flex-shrink: 0;
  border-radius: 50%;
  overflow: visible;
  cursor: grab;
  transform-style: preserve-3d;
  transition: transform 0.18s ease-out;
}

.avatar-canvas {
  position: relative;
  z-index: 1;
  width: 56px;
  height: 56px;
  display: block;
  border-radius: 50%;
  background: transparent;
  box-shadow:
    0 10px 22px -8px rgba(15, 23, 42, 0.55),
    0 4px 10px -4px rgba(37, 99, 235, 0.45);
}

.avatar-ring {
  position: absolute;
  inset: -4px;
  z-index: 2;
  border-radius: 50%;
  background: conic-gradient(
    from 0deg,
    #0f172a,
    #2563eb,
    #60a5fa,
    #0ea5e9,
    #1d4ed8,
    #0f172a
  );
  filter: blur(0.2px);
  -webkit-mask: radial-gradient(farthest-side, transparent calc(100% - 3px), #000 calc(100% - 3px));
  mask: radial-gradient(farthest-side, transparent calc(100% - 3px), #000 calc(100% - 3px));
  animation: ringSpin 5s linear infinite;
  pointer-events: none;
}

@keyframes ringSpin {
  to { transform: rotate(360deg); }
}

@keyframes statusPulse {
  0%, 100% {
    box-shadow: 0 0 0 2px rgba(34, 197, 94, 0.2);
    opacity: 1;
  }
  50% {
    box-shadow: 0 0 0 5px rgba(34, 197, 94, 0.08);
    opacity: 0.85;
  }
}

.ai-meta {
  flex: 1;
  min-width: 0;
}

.ai-name {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: 0.1px;
}

.ai-status {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  margin-top: 5px;
  font-size: 11.5px;
  font-weight: 600;
  color: #2563eb;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 2px rgba(34, 197, 94, 0.2);
  animation: statusPulse 1.8s ease-in-out infinite;
}

.ai-actions {
  display: flex;
  gap: 4px;
}

.icon-btn {
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: 0.2s;
}

.icon-btn:hover {
  background: #e0edff;
  color: #2563eb;
  transform: translateY(-1px);
}

/* ---------- Welcome ---------- */
:deep(.el-drawer__body) {
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(62% 48% at 100% 0%, rgba(37, 99, 235, 0.1), transparent 65%),
    radial-gradient(48% 42% at 0% 20%, rgba(96, 165, 250, 0.12), transparent 70%),
    radial-gradient(30% 22% at 10% 80%, rgba(37, 99, 235, 0.07), transparent 70%),
    radial-gradient(26% 20% at 92% 70%, rgba(59, 130, 246, 0.09), transparent 70%),
    linear-gradient(180deg, #f7fbff 0%, #ffffff 48%, #eef4ff 100%);
}

.welcome {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 14px 6px 6px;
  text-align: center;
}

.welcome h3 {
  margin: 0 0 6px;
  color: #1f2937;
  font-size: 17px;
  font-weight: 700;
}

.welcome p {
  margin: 0 auto 16px;
  max-width: 280px;
  color: #6b7280;
  font-size: 12.5px;
  line-height: 1.55;
}

.quick-questions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: auto;
  padding-top: 24px;
}

.quick-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
  color: #4b5563;
  font-size: 12.5px;
  text-align: left;
  cursor: pointer;
  transition: 0.2s;
  box-shadow: 0 2px 6px rgba(15, 23, 42, 0.04);
}

.quick-btn .el-icon {
  color: #2563eb;
  font-size: 14px;
}

.quick-btn:hover {
  border-color: #2563eb;
  background: #eff6ff;
  color: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -8px rgba(37, 99, 235, 0.45);
}

/* ---------- Messages ---------- */
.messages {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 12px;
  max-height: 380px;
  overflow-y: auto;
  padding: 4px;
}

.msg {
  padding: 8px 12px;
  border-radius: 12px;
  max-width: 80%;
  display: flex;
  align-items: flex-start;
  gap: 7px;
  font-size: 13px;
  line-height: 1.5;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.msg.user {
  align-self: flex-end;
  background: linear-gradient(135deg, #2563eb, #60a5fa);
  color: white;
  border-radius: 12px 12px 3px 12px;
  box-shadow: 0 6px 16px -8px rgba(37, 99, 235, 0.55);
}

.msg.ai {
  align-self: flex-start;
  background: #ffffff;
  color: #374151;
  border: 1px solid #eef0f3;
  border-radius: 12px 12px 12px 3px;
}

.message-avatar {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #e0edff;
  color: #2563eb;
  font-size: 11px;
  overflow: hidden;
  border: 1px solid rgba(37, 99, 235, 0.25);
}

.message-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.message-content {
  position: relative;
  white-space: pre-line;
  min-width: 0;
}

.msg-attach {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 8px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.18);
  font-size: 11.5px;
  margin-bottom: 4px;
}

.msg.ai .msg-attach {
  background: #e0edff;
  color: #1d4ed8;
}

.msg-attach-name {
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.msg-text {
  word-break: break-word;
}

.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.msg-copy {
  flex: none;
  width: 22px;
  height: 22px;
  margin-top: 1px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: opacity 0.15s ease, background 0.15s ease, transform 0.15s ease, color 0.15s ease;
}

.msg.ai .msg-copy {
  background: #eef2f7;
  color: #2563eb;
  opacity: 0.85;
}

.msg.user .msg-copy {
  background: rgba(255, 255, 255, 0.25);
  color: #ffffff;
  opacity: 0.9;
}

.msg:hover .msg-copy,
.msg-copy:focus-visible {
  opacity: 1;
  transform: translateY(-1px);
}

.msg.ai .msg-copy:hover {
  background: #e0edff;
  color: #1d4ed8;
}

.msg.user .msg-copy:hover {
  background: rgba(255, 255, 255, 0.35);
}

.msg-copy.copied {
  opacity: 1 !important;
}

.typing .message-content {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 40px;
  min-height: 20px;
}

.typing .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #9ca3af;
  animation: bounce 1.2s infinite ease-in-out;
}

.typing .dot:nth-child(2) { animation-delay: 0.15s; }
.typing .dot:nth-child(3) { animation-delay: 0.3s; }

@keyframes bounce {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-5px); }
}

/* ---------- Attachment preview ---------- */
.attachment-preview {
  margin-bottom: 8px;
}

.attachment-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  background: #eff6ff;
  border: 1px solid rgba(37, 99, 235, 0.3);
  border-radius: 16px;
  font-size: 12px;
  color: #1d4ed8;
  box-shadow: 0 4px 10px -6px rgba(37, 99, 235, 0.45);
}

.pill-icon {
  color: #2563eb;
  font-size: 13px;
}

.pill-name {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attachment-remove {
  background: transparent;
  border: none;
  color: #1d4ed8;
  font-size: 15px;
  cursor: pointer;
  line-height: 1;
  padding: 0 2px;
  border-radius: 50%;
  transition: 0.15s;
}

.attachment-remove:hover {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.08);
}

.pill-enter-active, .pill-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}
.pill-enter-from, .pill-leave-to {
  opacity: 0;
  transform: translateY(4px);
}

/* ---------- Input bar ---------- */
.chat-input-bar {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  padding: 6px 8px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 999px;
  box-shadow:
    0 6px 18px -10px rgba(15, 23, 42, 0.18),
    0 2px 6px rgba(15, 23, 42, 0.04);
  transition: 0.2s;
}

.chat-input-bar.is-focused,
.chat-input-bar:focus-within {
  border-color: #2563eb;
  box-shadow:
    0 0 0 3px rgba(37, 99, 235, 0.15),
    0 6px 18px -10px rgba(37, 99, 235, 0.4);
}

.hidden-file-input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
  width: 0;
  height: 0;
}

.input-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  transition: 0.2s;
}

.input-icon:hover {
  background: #f3f4f6;
  color: #374151;
  transform: translateY(-1px);
}

.input-icon.attach:hover,
.input-icon.attach.active {
  background: #e0edff;
  color: #2563eb;
  box-shadow: 0 4px 10px -6px rgba(37, 99, 235, 0.5);
}

.chat-input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  color: #1f2937;
  font-size: 14px;
  padding: 0 4px;
}

.chat-input::placeholder {
  color: #9ca3af;
}

.send-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb, #60a5fa);
  color: #fff;
  cursor: pointer;
  transition: 0.2s;
  flex-shrink: 0;
  box-shadow: 0 6px 14px -6px rgba(37, 99, 235, 0.65);
}

.send-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
  box-shadow: none;
}

.send-btn:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px -8px rgba(37, 99, 235, 0.7);
}

.is-loading {
  animation: spin 0.9s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ---------- Attach menu (popover) ---------- */
.attach-wrapper {
  position: relative;
}

.attach-menu {
  position: absolute;
  bottom: calc(100% + 8px);
  left: 0;
  z-index: 20;
  min-width: 220px;
  padding: 6px;
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 12px;
  box-shadow:
    0 16px 40px -10px rgba(15, 23, 42, 0.25),
    0 4px 10px rgba(15, 23, 42, 0.06);
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.attach-menu::after {
  content: '';
  position: absolute;
  left: 14px;
  bottom: -6px;
  width: 12px;
  height: 12px;
  background: #fff;
  border-right: 1px solid rgba(15, 23, 42, 0.06);
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
  transform: rotate(45deg);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  text-align: left;
  transition: 0.15s;
}

.menu-item:hover {
  background: #eff6ff;
}

.menu-ico {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 15px;
  flex-shrink: 0;
}

.ico-img    { background: linear-gradient(135deg, #f97316, #fb923c); }
.ico-file   { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
.ico-folder { background: linear-gradient(135deg, #f59e0b, #fbbf24); }

.menu-title {
  font-size: 12.5px;
  font-weight: 600;
  color: #1f2937;
}

.menu-sub {
  font-size: 11px;
  color: #6b7280;
  margin-top: 1px;
}

.menu-enter-active, .menu-leave-active {
  transition: opacity 0.15s, transform 0.15s;
}
.menu-enter-from, .menu-leave-to {
  opacity: 0;
  transform: translateY(4px);
}

/* ---------- Footer ---------- */
.ai-footer {
  margin-top: 10px;
  text-align: center;
  font-size: 10px;
  color: #9ca3af;
}

.brand {
  color: #2563eb;
  font-weight: 600;
}

/* ---- Dark theme surfaces ---- */
:global(html.dark .cs-chat .el-drawer),
:global(html.dark .cs-chat .el-drawer__body) {
  background: var(--surface);
  color: var(--text-body);
}
:global(html.dark .cs-chat .el-drawer__header) { color: var(--text-strong); }
:global(html.dark) .welcome h3 { color: var(--text-strong); }
:global(html.dark) .welcome p { color: var(--text-muted); }
:global(html.dark) .quick-btn {
  background: var(--surface-2);
  border-color: var(--border-color);
  color: var(--text-body);
}
:global(html.dark) .quick-btn:hover {
  background: rgba(37, 99, 235, 0.18);
  border-color: rgba(96, 165, 250, 0.5);
  color: #93c5fd;
}
:global(html.dark) .msg.ai .msg-text {
  background: var(--surface-2);
  color: var(--text-body);
  border-color: var(--border-color);
}
:global(html.dark) .msg.user .msg-text {
  color: #ffffff;
}
:global(html.dark) .chat-input-bar {
  background: var(--surface-2);
  border-color: var(--border-color);
}
:global(html.dark) .chat-input-bar.is-focused {
  background: var(--surface);
  border-color: rgba(96, 165, 250, 0.6);
}
:global(html.dark) .chat-input { color: var(--text-strong); }
:global(html.dark) .attach-menu {
  background: var(--surface);
  border-color: var(--border-color);
  box-shadow: var(--shadow-strong);
}
:global(html.dark) .menu-item:hover { background: var(--surface-muted); }
:global(html.dark) .menu-title { color: var(--text-strong); }
:global(html.dark) .menu-sub { color: var(--text-soft); }
:global(html.dark) .attachment-pill {
  background: var(--surface-2);
  border-color: var(--border-color);
  color: var(--text-body);
}
:global(html.dark) .ai-footer { color: var(--text-soft); }
</style>
