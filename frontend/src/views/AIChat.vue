<template>
  <div class="ai-page">
    <div class="chat-container">

      <!-- Header -->
      <div class="chat-header">
        <div class="header-left">
          <div class="header-avatar">
            <img src="/ai-avatar.png" alt="LevTicket AI" />
            <span class="header-status-dot"></span>
          </div>
          <div class="header-text">
            <h1>LevTicket AI</h1>
            <p>Your expert assistant for tickets, bids & orders</p>
          </div>
        </div>
      </div>

      <!-- Messages -->
      <div class="messages" ref="messagesContainer">
        <div
          v-for="(message, index) in messages"
          :key="index"
          :class="['message-row', message.role]"
        >
          <div class="bubble-avatar" v-if="message.role === 'assistant'">
            <img src="/ai-avatar.png" alt="AI" />
          </div>

          <div class="message-bubble">
            <div class="bubble-tail" v-if="message.role === 'assistant'"></div>
            <div class="bubble-tail user-tail" v-if="message.role === 'user'"></div>
            <div class="message-content">
              <span class="message-text">{{ message.text }}</span>
              <button
                type="button"
                class="copy-msg-btn"
                :title="copiedIndex === index ? 'Copied!' : 'Copy message'"
                @click.stop="copyMessage(index, message.text)"
              >
                <svg v-if="copiedIndex !== index" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                  <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                </svg>
                <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
              </button>
            </div>
          </div>

          <div class="bubble-avatar user-avatar" v-if="message.role === 'user'">
            <span>U</span>
          </div>
        </div>

        <!-- Typing indicator -->
        <div v-if="loading" class="message-row assistant">
          <div class="bubble-avatar">
            <img src="/ai-avatar.png" alt="AI" />
          </div>
          <div class="message-bubble typing-bubble">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
        </div>
      </div>

      <!-- Suggestion chips -->
      <div class="suggestions" v-if="messages.length <= 1">
        <button
          v-for="q in quickQuestions"
          :key="q"
          class="chip"
          @click="sendMessage(q)"
          :disabled="loading"
        >
          {{ q }}
        </button>
      </div>

      <!-- Attachment preview -->
      <div class="attachment-preview" v-if="attachment">
        <div class="attachment-pill">
          <span class="attachment-icon">{{ attachment.type === 'audio' ? '🎙️' : attachment.type === 'camera' ? '📷' : '📎' }}</span>
          <span class="attachment-name">{{ attachment.name }}</span>
          <button type="button" class="attachment-remove" @click="removeAttachment" title="Remove">
            ×
          </button>
        </div>
      </div>

      <!-- Audio recording bar -->
      <div class="recording-bar" v-if="isRecording">
        <span class="recording-dot"></span>
        <span>Recording... {{ recordingTime }}s</span>
        <button type="button" @click="stopRecording" class="stop-record-btn">Stop</button>
      </div>

      <!-- Camera modal -->
      <div class="camera-modal" v-if="cameraOpen">
        <div class="camera-backdrop" @click="closeCamera"></div>
        <div class="camera-panel">
          <video ref="cameraVideo" autoplay playsinline muted class="camera-video"></video>
          <div class="camera-actions">
            <button type="button" @click="capturePhoto" class="camera-capture">📸 Take Photo</button>
            <button type="button" @click="closeCamera" class="camera-cancel">Cancel</button>
          </div>
        </div>
      </div>

      <!-- ChatGPT-style Input -->
      <form class="chat-input-wrap" @submit.prevent="sendMessage(input)">
        <div class="chat-input-bar">
          <button
            type="button"
            class="input-tool attach"
            title="Attach file"
            @click="openFilePicker"
            :disabled="loading || isRecording"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/>
            </svg>
          </button>

          <input
            v-model="input"
            type="text"
            placeholder="Ask anything"
            :disabled="loading || isRecording"
            @keydown.enter.prevent="sendMessage(input)"
            @paste="onPaste"
          />

          <button
            type="button"
            class="input-tool think"
            title="Think deeper (placeholder)"
            :disabled="loading || isRecording"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <path d="M12 16v-4M12 8h.01"/>
            </svg>
            <span>Think</span>
          </button>

          <button
            type="button"
            class="input-tool mic"
            title="Record audio"
            :class="{ recording: isRecording }"
            @click="toggleRecording"
            :disabled="loading"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 2a3 3 0 0 1 3 3v7a3 3 0 0 1-6 0V5a3 3 0 0 1 3-3Z"/>
              <path d="M19 10v2a7 7 0 0 1-14 0v-2"/>
              <line x1="12" y1="19" x2="12" y2="22"/>
            </svg>
          </button>

          <button
            type="button"
            class="input-tool camera"
            title="Take a picture"
            @click="openCamera"
            :disabled="loading || isRecording"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
              <circle cx="12" cy="13" r="4"/>
            </svg>
          </button>

          <button
            type="submit"
            class="input-submit"
            :disabled="loading || isRecording || (!input.trim() && !attachment)"
          >
            <svg v-if="!attachment || input.trim()" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <line x1="22" y1="2" x2="11" y2="13"/>
              <polygon points="22 2 15 22 11 13 2 9 22 2"/>
            </svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 2a3 3 0 0 1 3 3v7a3 3 0 0 1-6 0V5a3 3 0 0 1 3-3Z"/>
              <path d="M19 10v2a7 7 0 0 1-14 0v-2"/>
            </svg>
          </button>
        </div>

        <p class="input-footer">LevTicket AI can make mistakes. Check important info.</p>
      </form>

      <!-- Hidden file inputs -->
      <input ref="fileInput" type="file" class="hidden-input" @change="onFileSelected" />
    </div>
  </div>
</template>

<script setup>
import { nextTick, ref } from 'vue'
import { submitComplaint } from '@/api/ai'

const input = ref('')
const loading = ref(false)
const messagesContainer = ref(null)
const copiedIndex = ref(-1)

const quickQuestions = [
  'How do I sell a ticket?',
  'How do I buy a ticket?',
  'How do I place a bid?',
  'How do refunds work?'
]

const messages = ref([
  {
    role: 'assistant',
    text: 'Hello! I am LevTicket AI, your ticket marketplace assistant. I can help you buy, sell, bid on tickets, and manage your orders. What would you like to know?',
    time: Date.now()
  }
])

async function scrollToBottom() {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTo({
      top: messagesContainer.value.scrollHeight,
      behavior: 'smooth'
    })
  }
}

async function copyMessage(index, text) {
  try {
    await navigator.clipboard.writeText(text)
    copiedIndex.value = index
    setTimeout(() => (copiedIndex.value = -1), 1800)
  } catch (err) {
    console.error('Copy failed:', err)
  }
}

function buildHistory() {
  return messages.value.map((m) => ({
    role: m.role === 'user' ? 'user' : 'assistant',
    content: m.text
  }))
}

const fileInput = ref(null)
const attachment = ref(null)
const isRecording = ref(false)
const recordingTime = ref(0)
const cameraOpen = ref(false)
const cameraVideo = ref(null)

let mediaRecorder = null
let audioChunks = []
let recordingTimer = null
let cameraStream = null

function openFilePicker() {
  fileInput.value?.click()
}

function onFileSelected(event) {
  const file = event.target.files?.[0]
  if (!file) return

  const type = file.type.startsWith('image/') ? 'image' : 'file'
  attachment.value = { type, name: file.name, file }
  event.target.value = ''
}

function removeAttachment() {
  attachment.value = null
}

function onPaste(event) {
  const items = event.clipboardData?.items
  if (!items) return

  for (const item of items) {
    if (item.type.startsWith('image/')) {
      event.preventDefault()
      const file = item.getAsFile()
      if (file) {
        attachment.value = { type: 'image', name: file.name || 'Pasted image', file }
      }
      return
    }
  }
}

async function toggleRecording() {
  if (isRecording.value) {
    stopRecording()
  } else {
    await startRecording()
  }
}

async function startRecording() {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder = new MediaRecorder(stream)
    audioChunks = []

    mediaRecorder.ondataavailable = (e) => {
      if (e.data.size > 0) audioChunks.push(e.data)
    }

    mediaRecorder.onstop = () => {
      const blob = new Blob(audioChunks, { type: 'audio/webm' })
      const url = URL.createObjectURL(blob)
      attachment.value = { type: 'audio', name: `Voice message (${recordingTime.value}s)`, url, blob }
      stream.getTracks().forEach((t) => t.stop())
    }

    mediaRecorder.start()
    isRecording.value = true
    recordingTime.value = 0
    recordingTimer = setInterval(() => recordingTime.value++, 1000)
  } catch (err) {
    console.error('Microphone access denied:', err)
    alert('Microphone access is needed to record audio.')
  }
}

function stopRecording() {
  if (recordingTimer) clearInterval(recordingTimer)
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
  }
  isRecording.value = false
}

async function openCamera() {
  try {
    cameraStream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })
    cameraOpen.value = true
    await nextTick()
    if (cameraVideo.value) {
      cameraVideo.value.srcObject = cameraStream
    }
  } catch (err) {
    console.error('Camera access denied:', err)
    alert('Camera access is needed to take pictures.')
  }
}

function closeCamera() {
  cameraOpen.value = false
  if (cameraStream) {
    cameraStream.getTracks().forEach((t) => t.stop())
    cameraStream = null
  }
}

function capturePhoto() {
  if (!cameraVideo.value) return
  const video = cameraVideo.value
  const canvas = document.createElement('canvas')
  canvas.width = video.videoWidth || 640
  canvas.height = video.videoHeight || 480
  const ctx = canvas.getContext('2d')
  ctx.drawImage(video, 0, 0, canvas.width, canvas.height)

  canvas.toBlob((blob) => {
    if (!blob) return
    const url = URL.createObjectURL(blob)
    attachment.value = { type: 'camera', name: 'Camera photo', url, blob }
    closeCamera()
  }, 'image/png')
}

async function sendMessage(text) {
  let message = (text ?? input.value ?? '').trim()
  if (loading.value) return
  if (!message && !attachment.value) return

  let displayText = message
  let aiText = message

  if (attachment.value) {
    const label =
      attachment.value.type === 'audio'
        ? '🎙️ Audio attachment'
        : attachment.value.type === 'camera' || attachment.value.type === 'image'
          ? '📷 Image attachment'
          : '📎 File attachment'
    displayText = displayText ? `${label}: ${attachment.value.name}\n${message}` : `${label}: ${attachment.value.name}`
    aiText = aiText
      ? `[User uploaded ${attachment.value.type === 'audio' ? 'an audio recording' : 'a file'}: ${attachment.value.name}]\n${message}`
      : `[User uploaded ${attachment.value.type === 'audio' ? 'an audio recording' : 'a file'}: ${attachment.value.name}]`
  }

  messages.value.push({ role: 'user', text: displayText, time: Date.now() })
  input.value = ''
  attachment.value = null
  loading.value = true
  await scrollToBottom()

  try {
    const res = await submitComplaint(aiText, buildHistory())
    const reply = res?.reply || 'Sorry, I could not generate a response.'
    messages.value.push({ role: 'assistant', text: reply, time: Date.now() })
  } catch (error) {
    console.error('AI request failed:', error)
    let detail = error?.message ? String(error.message) : 'Sorry, LevTicket AI is unavailable.'
    if (detail.toLowerCase().includes('timeout')) {
      detail = 'LevTicket AI is taking longer than expected. Please try again in a moment.'
    }
    messages.value.push({ role: 'assistant', text: detail, time: Date.now() })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}
</script>

<style scoped>
.ai-page {
  position: relative;
  min-height: calc(100vh - 116px);
  padding: 28px 16px;
  background: linear-gradient(160deg, #0b1120 0%, #060a13 50%, #0e1629 100%);
  overflow: hidden;
}

.ai-page::before {
  content: '';
  position: absolute;
  inset: -2px;
  z-index: 0;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='600' height='600' viewBox='0 0 600 600'%3E%3Cdefs%3E%3Cfilter id='soft'%3E%3CfeGaussianBlur stdDeviation='0.7'/%3E%3C/filter%3E%3C/defs%3E%3Cg filter='url(%23soft)' stroke='%2338bdf8' stroke-width='0.7' fill='none'%3E%3Cline x1='90' y1='70' x2='190' y2='160'/%3E%3Cline x1='190' y1='160' x2='310' y2='130'/%3E%3Cline x1='310' y1='130' x2='400' y2='220'/%3E%3Cline x1='90' y1='70' x2='210' y2='45'/%3E%3Cline x1='210' y1='45' x2='310' y2='130'/%3E%3Cline x1='400' y1='220' x2='510' y2='185'/%3E%3Cline x1='210' y1='45' x2='250' y2='290'/%3E%3Cline x1='250' y1='290' x2='125' y2='360'/%3E%3Cline x1='125' y1='360' x2='190' y2='160'/%3E%3Cline x1='310' y1='130' x2='430' y2='50'/%3E%3Cline x1='430' y1='50' x2='510' y2='185'/%3E%3Cline x1='190' y1='160' x2='125' y2='360'/%3E%3Cline x1='250' y1='290' x2='380' y2='340'/%3E%3Cline x1='380' y1='340' x2='510' y2='185'/%3E%3Cline x1='380' y1='340' x2='400' y2='220'/%3E%3C/g%3E%3Cg fill='%2338bdf8'%3E%3Ccircle cx='90' cy='70' r='2.4'/%3E%3Ccircle cx='190' cy='160' r='2.4'/%3E%3Ccircle cx='310' cy='130' r='2.4'/%3E%3Ccircle cx='400' cy='220' r='2.4'/%3E%3Ccircle cx='210' cy='45' r='2.4'/%3E%3Ccircle cx='510' cy='185' r='2.4'/%3E%3Ccircle cx='250' cy='290' r='2.4'/%3E%3Ccircle cx='125' cy='360' r='2.4'/%3E%3Ccircle cx='430' cy='50' r='2.4'/%3E%3Ccircle cx='380' cy='340' r='2.4'/%3E%3C/g%3E%3C/svg%3E");
  background-repeat: repeat;
  background-size: 600px 600px;
  opacity: 0.26;
  filter: blur(0.8px);
  pointer-events: none;
}

.ai-page::after {
  content: '';
  position: absolute;
  top: -140px;
  left: 50%;
  transform: translateX(-50%);
  width: 900px;
  height: 500px;
  background: radial-gradient(ellipse at center, rgba(59, 130, 246, 0.13) 0%, transparent 65%);
  pointer-events: none;
  z-index: 0;
}

.chat-container {
  position: relative;
  z-index: 1;
  max-width: 860px;
  height: calc(100vh - 172px);
  min-height: 520px;
  margin: 0 auto;
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.45);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

/* Header */
.chat-header {
  padding: 20px 26px;
  background: linear-gradient(120deg, #1f2937 0%, #374151 100%);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.header-avatar {
  position: relative;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: visible;
  border: 2px solid rgba(255, 255, 255, 0.25);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.25);
}

.header-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.header-status-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 13px;
  height: 13px;
  border-radius: 50%;
  background: #22c55e;
  border: 2px solid #1f2937;
}

.header-text h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0.2px;
}

.header-text p {
  margin: 4px 0 0;
  font-size: 12.5px;
  opacity: 0.75;
}

/* Messages */
.messages {
  flex: 1;
  padding: 22px 26px;
  overflow-y: auto;
  background: #fafbfd;
}

.message-row {
  display: flex;
  align-items: flex-end;
  gap: 9px;
  margin-bottom: 18px;
}

.message-row.user {
  justify-content: flex-end;
}

.message-row.assistant {
  justify-content: flex-start;
}

.bubble-avatar {
  flex-shrink: 0;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  border: 2px solid #ffffff;
}

.bubble-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar {
  background: linear-gradient(135deg, #ff6b35, #ff9a5c);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: 700;
}

.message-bubble {
  position: relative;
  max-width: 72%;
  padding: 11px 15px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.55;
  white-space: pre-line;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.2s ease;
}

.message-bubble:hover {
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.09);
}

.assistant .message-bubble {
  background: #ffffff;
  color: #1f2937;
  border: 1px solid #eceef4;
  border-bottom-left-radius: 4px;
}

.user .message-bubble {
  background: linear-gradient(135deg, #ff6b35, #ff8f5a);
  color: white;
  border-bottom-right-radius: 4px;
  box-shadow: 0 4px 14px rgba(255, 107, 53, 0.25);
}

.bubble-tail {
  position: absolute;
  bottom: 8px;
  left: -7px;
  width: 0;
  height: 0;
  border-top: 7px solid transparent;
  border-bottom: 7px solid transparent;
  border-right: 7px solid #ffffff;
  filter: drop-shadow(-1px 1px 0 #eceef4);
}

.user-tail {
  left: auto;
  right: -7px;
  border-top: 7px solid transparent;
  border-bottom: 7px solid transparent;
  border-left: 7px solid #ff8f5a;
  border-right: none;
}

.message-content {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
}

.message-text {
  word-break: break-word;
}

.copy-msg-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: #9ca3af;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s ease, background 0.2s ease, color 0.2s ease;
}

.message-bubble:hover .copy-msg-btn {
  opacity: 1;
}

.copy-msg-btn:hover {
  background: rgba(0, 0, 0, 0.08);
  color: #4b5563;
}

.user .message-bubble .copy-msg-btn {
  color: rgba(255, 255, 255, 0.8);
}

.user .message-bubble .copy-msg-btn:hover {
  background: rgba(255, 255, 255, 0.18);
  color: #ffffff;
}

@media (max-width: 600px) {
  .copy-msg-btn {
    opacity: 1;
    width: 18px;
    height: 18px;
  }
}

/* Typing */
.typing-bubble {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 14px 17px;
  min-width: 58px;
}

.typing-bubble .dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #9ca3af;
  animation: bounce 1.2s infinite ease-in-out;
}

.typing-bubble .dot:nth-child(2) {
  animation-delay: 0.15s;
}

.typing-bubble .dot:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes bounce {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-5px); }
}

/* Suggestions */
.suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 26px 12px;
  background: #fafbfd;
}

.chip {
  padding: 8px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  background: #ffffff;
  color: #4a4e5c;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.chip:hover:not(:disabled) {
  border-color: #ff6b35;
  color: #ff6b35;
  background: #fff7f3;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 107, 53, 0.18);
}

.chip:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Attachment preview */
.attachment-preview {
  padding: 10px 20px 0;
  background: #fafbfd;
}

.attachment-pill {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 6px 12px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 18px;
  font-size: 12px;
  color: #4a4e5c;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.attachment-remove {
  background: transparent;
  border: none;
  color: #9ca3af;
  font-size: 16px;
  cursor: pointer;
  line-height: 1;
  padding: 0 0 2px;
}

.attachment-remove:hover {
  color: #ef4444;
}

/* Recording bar */
.recording-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 10px 20px;
  background: #fafbfd;
  color: #ef4444;
  font-size: 13px;
  font-weight: 500;
}

.recording-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ef4444;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.stop-record-btn {
  margin-left: 8px;
  padding: 4px 10px;
  border: 1px solid #ef4444;
  background: #ffffff;
  color: #ef4444;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
}

.stop-record-btn:hover {
  background: #ef4444;
  color: #ffffff;
}

/* Camera modal */
.camera-modal {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3000;
}

.camera-backdrop {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.65);
}

.camera-panel {
  position: relative;
  background: #1f2937;
  border-radius: 16px;
  padding: 16px;
  width: 90%;
  max-width: 520px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.4);
}

.camera-video {
  width: 100%;
  border-radius: 12px;
  background: #000;
}

.camera-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 14px;
}

.camera-capture {
  padding: 10px 18px;
  border: none;
  border-radius: 10px;
  background: #ff6b35;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s;
}

.camera-capture:hover {
  transform: translateY(-2px);
}

.camera-cancel {
  padding: 10px 18px;
  border: 1px solid #4b5563;
  border-radius: 10px;
  background: transparent;
  color: #d1d5db;
  cursor: pointer;
}

.camera-cancel:hover {
  background: #374151;
}

/* ChatGPT-style Input */
.chat-input-wrap {
  padding: 14px 18px 16px;
  background: #fafbfd;
}

.chat-input-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  background: #1f2937;
  border-radius: 28px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.18);
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.chat-input-bar:focus-within {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25);
}

.chat-input-bar input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: #f3f4f6;
  font-size: 15px;
  padding: 8px 4px;
}

.chat-input-bar input::placeholder {
  color: #9ca3af;
}

.input-tool {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: #9ca3af;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease, transform 0.2s ease;
  flex-shrink: 0;
}

.input-tool:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
  transform: scale(1.08);
}

.input-tool:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.input-tool.think {
  width: auto;
  gap: 5px;
  padding: 0 10px;
  border-radius: 16px;
  font-size: 13px;
  color: #9ca3af;
}

.input-tool.think span {
  font-size: 13px;
}

.input-tool.recording {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.15);
}

.input-submit {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: none;
  background: #2563eb;
  color: #ffffff;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.35);
  flex-shrink: 0;
}

.input-submit:hover:not(:disabled) {
  background: #1d4ed8;
  transform: scale(1.08);
  box-shadow: 0 6px 18px rgba(37, 99, 235, 0.45);
}

.input-submit:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  background: #4b5563;
  box-shadow: none;
}

.input-footer {
  text-align: center;
  margin: 8px 0 0;
  font-size: 11px;
  color: #9ca3af;
}

.hidden-input {
  display: none;
}

@media (max-width: 600px) {
  .ai-page {
    padding: 12px;
  }

  .chat-container {
    height: calc(100vh - 120px);
  }

  .message-bubble {
    max-width: 82%;
  }

  .chat-header {
    padding: 16px 18px;
  }
}
</style>
