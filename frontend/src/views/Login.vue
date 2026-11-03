<template>
  <div class="auth-shell">
    <div class="auth-panel brand-panel">
      <div class="brand-scene" aria-hidden="true">
        <span class="blob blob-1"></span>
        <span class="blob blob-2"></span>
        <span class="blob blob-3"></span>
      </div>

      <canvas ref="sceneCanvas" class="brand-canvas" aria-hidden="true"></canvas>

      <div class="brand-copy">
        <div class="brand-mark">
          <img src="/ai-dragon.png" alt="LevTicket" />
        </div>
        <span class="eyebrow">{{ $t('LevTicket access') }}</span>
        <h1>{{ $t('Welcome back to your trusted ticket marketplace.') }}</h1>
        <p>
          {{ $t('Manage orders, monitor live bids, and keep your event plans in one place.') }}
        </p>
        <ul>
          <li><el-icon><Check /></el-icon><span>{{ $t('Secure buyer and seller protection') }}</span></li>
          <li><el-icon><Check /></el-icon><span>{{ $t('Instant event updates and alerts') }}</span></li>
          <li><el-icon><Check /></el-icon><span>{{ $t('24/7 support and AI concierge') }}</span></li>
        </ul>
      </div>
    </div>

    <div class="auth-panel form-panel">
      <el-card class="auth-card">
        <div class="card-header">
          <span class="card-kicker">{{ $t('Sign in') }}</span>
          <h2>{{ $t('Login to LevTicket') }}</h2>
        </div>

        <el-form :model="form" label-position="top" @submit.prevent="handleLogin">
          <el-form-item :label="$t('Email')">
            <el-input v-model="form.email" :placeholder="$t('Enter email')" />
          </el-form-item>
          <el-form-item :label="$t('Password')">
            <el-input v-model="form.password" type="password" :placeholder="$t('Enter password')" />
          </el-form-item>
          <el-button type="primary" native-type="submit" class="auth-submit" :loading="loading">{{ $t('Login') }}</el-button>
        </el-form>

        <p class="switch-copy">
          {{ $t('No account yet?') }}
          <router-link to="/register">{{ $t('Create one') }}</router-link>
        </p>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Check } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { login } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { t } from '@/i18n'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ email: '', password: '' })

// ---- drifting dot-mesh behind the copy ----
const sceneCanvas = ref(null)
let sceneCtx = null
let sceneRaf = null
let sceneDots = []
let sceneW = 0
let sceneH = 0

const LINK_DISTANCE = 132
const reduceMotion =
  typeof window !== 'undefined' &&
  window.matchMedia?.('(prefers-reduced-motion: reduce)').matches

function initScene() {
  const canvas = sceneCanvas.value
  if (!canvas) return
  const w = Math.round(canvas.clientWidth)
  const h = Math.round(canvas.clientHeight)
  if (!w || !h) return

  const dpr = window.devicePixelRatio || 1
  canvas.width = w * dpr
  canvas.height = h * dpr
  sceneCtx = canvas.getContext('2d')
  sceneCtx.setTransform(dpr, 0, 0, dpr, 0, 0)

  sceneW = w
  sceneH = h

  // density follows the panel area so it never reads as sparse or busy
  const count = Math.min(64, Math.max(18, Math.round((w * h) / 15000)))
  sceneDots = Array.from({ length: count }, () => ({
    x: Math.random() * w,
    y: Math.random() * h,
    vx: (Math.random() - 0.5) * 0.24,
    vy: (Math.random() - 0.5) * 0.24,
    r: Math.random() * 1.4 + 0.9
  }))

  if (sceneRaf) cancelAnimationFrame(sceneRaf)
  sceneRaf = null
  if (reduceMotion) drawScene()
  else sceneRaf = requestAnimationFrame(tickScene)
}

function drawScene() {
  const ctx = sceneCtx
  if (!ctx) return
  ctx.clearRect(0, 0, sceneW, sceneH)

  // faint mesh linking nearby dots
  ctx.lineWidth = 1
  for (let i = 0; i < sceneDots.length; i++) {
    const a = sceneDots[i]
    for (let j = i + 1; j < sceneDots.length; j++) {
      const b = sceneDots[j]
      const dist = Math.hypot(a.x - b.x, a.y - b.y)
      if (dist > LINK_DISTANCE) continue
      const alpha = (1 - dist / LINK_DISTANCE) * 0.32
      ctx.strokeStyle = `rgba(147, 197, 253, ${alpha.toFixed(3)})`
      ctx.beginPath()
      ctx.moveTo(a.x, a.y)
      ctx.lineTo(b.x, b.y)
      ctx.stroke()
    }
  }

  ctx.fillStyle = 'rgba(219, 234, 254, 0.7)'
  for (const d of sceneDots) {
    ctx.beginPath()
    ctx.arc(d.x, d.y, d.r, 0, Math.PI * 2)
    ctx.fill()
  }
}

function tickScene() {
  for (const d of sceneDots) {
    d.x += d.vx
    d.y += d.vy
    if (d.x < 0 || d.x > sceneW) d.vx *= -1
    if (d.y < 0 || d.y > sceneH) d.vy *= -1
  }
  drawScene()
  sceneRaf = requestAnimationFrame(tickScene)
}

function onSceneResize() {
  const canvas = sceneCanvas.value
  if (!canvas) return
  if (Math.round(canvas.clientWidth) === sceneW && Math.round(canvas.clientHeight) === sceneH) return
  initScene()
}

onMounted(() => {
  nextTick(initScene)
  // the panel is a fluid grid column, so the backing store has to refit on resize
  window.addEventListener('resize', onSceneResize)
})

onBeforeUnmount(() => {
  if (sceneRaf) cancelAnimationFrame(sceneRaf)
  sceneRaf = null
  window.removeEventListener('resize', onSceneResize)
})

async function handleLogin() {
  const email = form.email.trim()
  if (!email || !form.password) {
    ElMessage.warning(t('Please enter your email and password'))
    return
  }
  loading.value = true
  try {
    const res = await login(email, form.password)
    if (res?.code !== 200 || !res?.data?.token) {
      ElMessage.error(res?.message || t('Login failed. Please check your credentials.'))
      return
    }
    const { token, userId, username } = res.data
    auth.setToken(token)
    auth.setUser({ id: userId, username, email })
    ElMessage.success(t('Welcome back, {name}! 👋', { name: username }))
    router.push(router.currentRoute.value.query.redirect || '/search')
  } catch (err) {
    ElMessage.error(err?.message || t('Unable to reach the server. Please try again.'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-panel {
  min-height: 640px;
  border-radius: 30px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  box-shadow: var(--shadow-soft);
}

/* the same deep navy stage the marketplace hero sits on, so the two
   pages read as one product */
.brand-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 34px;
  overflow: hidden;
  isolation: isolate;
  color: #fff;
  background:
    radial-gradient(1100px 640px at 84% -14%, rgba(96, 165, 250, 0.34), transparent 60%),
    radial-gradient(760px 560px at -10% 118%, rgba(59, 130, 246, 0.3), transparent 62%),
    radial-gradient(560px 440px at 112% 96%, rgba(37, 99, 235, 0.38), transparent 66%),
    linear-gradient(142deg, #040a1d 0%, #0a1a3e 46%, #0d2a70 100%);
  box-shadow: var(--shadow-strong);
}

/* ---- drifting blobs (background layer, mirrors .hero-scene) ---- */
.brand-scene {
  position: absolute;
  inset: -12%;
  z-index: 0;
  pointer-events: none;
}

.brand-scene .blob {
  position: absolute;
  border-radius: 50%;
  animation-timing-function: ease-in-out;
  animation-iteration-count: infinite;
  animation-direction: alternate;
  background: radial-gradient(
    circle at 32% 28%,
    rgba(219, 234, 254, 0.9) 0%,
    rgba(96, 165, 250, 0.45) 40%,
    rgba(37, 99, 235, 0.18) 62%,
    rgba(30, 58, 138, 0) 76%
  );
}

.brand-scene .blob-1 { top: -24%; right: -13%; width: 420px; height: 420px; animation: authBlob1 18s infinite; }
.brand-scene .blob-2 { bottom: -28%; right: -5%; width: 380px; height: 380px; animation: authBlob2 15s infinite; }
.brand-scene .blob-3 { bottom: -22%; left: 6%; width: 360px; height: 360px; opacity: 0.85; animation: authBlob3 21s infinite; }

@keyframes authBlob1 {
  0%   { transform: translate(0, 0) scale(1); }
  100% { transform: translate(70px, 50px) scale(1.2); }
}
@keyframes authBlob2 {
  0%   { transform: translate(0, 0) scale(1.1); }
  100% { transform: translate(-60px, -55px) scale(0.92); }
}
@keyframes authBlob3 {
  0%   { transform: translate(0, 0) scale(1); }
  100% { transform: translate(55px, -40px) scale(1.18); }
}

/* ---- dot mesh canvas, layered between the blobs and the glass card ---- */
.brand-canvas {
  position: absolute;
  inset: 0;
  z-index: 1;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

/* ---- foreground: the glass copy card (mirrors .hero-copy) ---- */
.brand-copy {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  border-radius: 22px;
  padding: 34px;
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.12) 0%, rgba(255, 255, 255, 0.05) 100%);
  border: 1px solid rgba(255, 255, 255, 0.16);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.14),
    0 26px 60px rgba(2, 6, 23, 0.38);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
}

.brand-copy h1,
.brand-copy p,
.brand-copy ul {
  align-self: stretch;
}

/* ---- the dragon crest, floated on the same 3D blue orb as the topbar brand ---- */
.brand-mark {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: none;
  overflow: hidden;
  width: 78px;
  height: 78px;
  margin-bottom: 24px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.45);
  background:
    radial-gradient(circle at 30% 22%, rgba(255, 255, 255, 0.65), rgba(255, 255, 255, 0.16) 34%, transparent 52%),
    radial-gradient(circle at 70% 100%, rgba(59, 130, 246, 0.5), transparent 55%),
    linear-gradient(150deg, #60a5fa 0%, #2563eb 55%, #1e40af 100%);
  box-shadow:
    inset 0 2px 4px rgba(255, 255, 255, 0.4),
    inset 0 -9px 16px rgba(15, 23, 42, 0.35),
    0 10px 22px rgba(37, 99, 235, 0.45),
    0 3px 6px rgba(15, 23, 42, 0.2);
  animation: authMarkFloat 7s ease-in-out infinite;
}

/* the dragon reads as a separate layer: offset white edge, then cast shadow */
.brand-mark img {
  display: block;
  width: 88%;
  height: 88%;
  object-fit: contain;
  filter:
    drop-shadow(0 1px 0 rgba(255, 255, 255, 0.35))
    drop-shadow(0 3px 5px rgba(15, 23, 42, 0.55))
    drop-shadow(0 6px 12px rgba(30, 64, 175, 0.4));
}

/* gloss sweeping across the orb, on top of the crest */
.brand-mark::after {
  content: '';
  position: absolute;
  top: -60%;
  left: -140%;
  width: 55%;
  height: 220%;
  transform: rotate(22deg);
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.6), transparent);
  animation: authMarkShine 6s ease-in-out infinite;
}

@keyframes authMarkFloat {
  0%, 100% { transform: translateY(0); }
  50%      { transform: translateY(-7px); }
}

@keyframes authMarkShine {
  0%        { left: -140%; }
  45%, 100% { left: 170%; }
}

/* matches .eyebrow on the marketplace hero and on Register, now embossed */
.eyebrow {
  display: inline-flex;
  padding: 7px 12px;
  border-radius: 999px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.26) 0%, rgba(255, 255, 255, 0.1) 100%);
  border: 1px solid rgba(255, 255, 255, 0.22);
  color: #eaf2ff;
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.22), 0 2px 5px rgba(2, 6, 23, 0.5);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.38),
    inset 0 -3px 8px rgba(2, 6, 23, 0.28),
    0 8px 18px rgba(2, 6, 23, 0.32);
}

/* 3D headline: a top-lit gradient face over chained drop-shadows that stack
   into a blue bevel. drop-shadow (not text-shadow) is required here —
   text-shadow paints above the background-clip:text gradient and floods it.
   The chain is static so the filter result is cached and costs nothing per
   frame, and the bevel is a mid blue: a white bevel reads as a blur against
   the white face, a navy one disappears into the panel. */
.brand-copy h1 {
  margin-top: 20px;
  font-size: clamp(2rem, 3.4vw, 2.9rem);
  line-height: 1.05;
  letter-spacing: -0.06em;
  background: linear-gradient(180deg, #ffffff 0%, #eaf3ff 34%, #cfe3ff 68%, #a8ccfb 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: #eaf3ff;
  filter:
    drop-shadow(0 1px 0 #93c5fd)
    drop-shadow(0 1px 0 #60a5fa)
    drop-shadow(0 1px 0 #3b82f6)
    drop-shadow(0 1px 0 #2563eb)
    drop-shadow(0 6px 10px rgba(2, 6, 23, 0.6))
    drop-shadow(0 14px 32px rgba(37, 99, 235, 0.45));
}

.brand-copy p {
  max-width: 34rem;
  margin-top: 18px;
  font-size: 1.02rem;
  line-height: 1.7;
  color: rgba(232, 240, 254, 0.86);
  text-shadow: 0 1px 0 rgba(2, 6, 23, 0.45), 0 2px 8px rgba(2, 6, 23, 0.3);
}

/* divided rail, same treatment as .hero-metrics */
.brand-copy ul {
  display: grid;
  gap: 12px;
  margin-top: 30px;
  padding: 26px 0 0;
  list-style: none;
  border-top: 1px solid rgba(255, 255, 255, 0.16);
  color: rgba(232, 240, 254, 0.94);
  font-size: 0.95rem;
  line-height: 1.5;
  text-shadow: 0 1px 0 rgba(2, 6, 23, 0.4);
}

.brand-copy li {
  display: flex;
  align-items: flex-start;
  gap: 11px;
}

/* ticked badge instead of a default disc bullet */
.brand-copy li .el-icon {
  flex: none;
  width: 20px;
  height: 20px;
  margin-top: 1px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.14);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.24);
  color: #bfdbfe;
  font-size: 12px;
}

/* ---- the form half: the same blue stage the brand panel sits on, lit from
   behind the card so the two halves read as one composition ---- */
.form-panel {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 34px 24px;
  overflow: hidden;
  isolation: isolate;
  background:
    radial-gradient(760px 520px at 112% -16%, rgba(96, 165, 250, 0.2), transparent 62%),
    radial-gradient(620px 460px at -14% 114%, rgba(37, 99, 235, 0.14), transparent 64%),
    linear-gradient(180deg, #f8fbff 0%, #eef4fb 100%);
}

/* ambient bloom behind the card, the light-surface echo of the brand blobs */
.form-panel::before {
  content: '';
  position: absolute;
  z-index: 0;
  width: min(120%, 560px);
  aspect-ratio: 1;
  border-radius: 50%;
  background: radial-gradient(
    circle at 50% 50%,
    rgba(96, 165, 250, 0.26) 0%,
    rgba(59, 130, 246, 0.1) 46%,
    rgba(37, 99, 235, 0) 70%
  );
  pointer-events: none;
}

/* ---- the card: a top-lit glass surface with a hairline highlight and a
   layered lift, so it floats above the panel instead of sitting in it ---- */
.auth-card {
  position: relative;
  z-index: 1;
  width: min(100%, 460px);
  padding: 12px;
  border-radius: 26px;
  border: 1px solid rgba(255, 255, 255, 0.9);
  background: linear-gradient(168deg, #ffffff 0%, #fdfeff 54%, #f1f7ff 100%);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.95),
    0 1px 2px rgba(15, 23, 42, 0.04),
    0 14px 28px rgba(15, 23, 42, 0.08),
    0 34px 70px rgba(37, 99, 235, 0.16);
}

/* specular streak along the top edge — the same light source as the hero glass */
.auth-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 12%;
  right: 12%;
  height: 1px;
  pointer-events: none;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.98) 22%,
    rgba(191, 219, 254, 0.95) 50%,
    rgba(255, 255, 255, 0.98) 78%,
    transparent
  );
}

.auth-card :deep(.el-card__body) {
  padding: 26px;
}

.card-header {
  margin-bottom: 20px;
}

/* embossed kicker: the light-surface twin of .eyebrow */
.card-kicker {
  display: inline-flex;
  padding: 7px 13px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.9);
  background: linear-gradient(180deg, #eaf3ff 0%, #cfe1ff 100%);
  color: #1d4ed8;
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.9);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.98),
    inset 0 -2px 5px rgba(37, 99, 235, 0.18),
    0 6px 14px rgba(37, 99, 235, 0.16);
}

/* 3D headline: the dark-surface twin of .brand-copy h1. A navy gradient face
   over stacked drop-shadows builds a bevel; text-shadow cannot, because it
   paints on top of the background-clip:text gradient and floods it. */
.card-header h2 {
  margin-top: 14px;
  font-size: clamp(1.75rem, 2.6vw, 2.1rem);
  line-height: 1.08;
  letter-spacing: -0.055em;
  background: linear-gradient(180deg, #1e3a8a 0%, #16295c 44%, #0a1530 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: #0f172a;
  filter:
    drop-shadow(0 1px 0 rgba(255, 255, 255, 0.98))
    drop-shadow(0 2px 0 rgba(191, 219, 254, 0.6))
    drop-shadow(0 10px 18px rgba(15, 23, 42, 0.16));
}

.auth-card :deep(.el-form-item) {
  margin-bottom: 18px;
}

.auth-card :deep(.el-form-item__label) {
  padding-bottom: 6px;
  color: #344054;
  font-size: 0.8rem;
  font-weight: 800;
  letter-spacing: 0.01em;
  line-height: 1.2;
}

/* recessed field: the input sits *in* the card rather than on it */
.auth-card :deep(.el-input__wrapper) {
  min-height: 48px;
  padding: 0 15px;
  border-radius: 13px;
  background: linear-gradient(180deg, #f4f8fd 0%, #ffffff 62%);
  border: 1px solid rgba(203, 213, 225, 0.95);
  box-shadow:
    inset 0 2px 4px rgba(15, 23, 42, 0.06),
    inset 0 1px 0 rgba(148, 163, 184, 0.14);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.auth-card :deep(.el-input__wrapper:hover) {
  border-color: rgba(147, 197, 253, 0.95);
}

/* Element Plus draws focus with a hard blue outline — soften it into a glow */
.auth-card :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  border-color: rgba(37, 99, 235, 0.7);
  box-shadow:
    0 0 0 4px rgba(37, 99, 235, 0.13),
    inset 0 1px 2px rgba(15, 23, 42, 0.05);
}

/* Chromium washes autofilled fields in its own blue, which fights the card */
.auth-card :deep(.el-input__inner:-webkit-autofill) {
  -webkit-text-fill-color: #0f172a;
  caret-color: #0f172a;
  transition: background-color 600000s ease-in-out 0s;
}

/* ---- primary action: a glossy 3D pill sharing the crest's sheen sweep ---- */
.auth-submit {
  position: relative;
  overflow: hidden;
  width: 100%;
  margin-top: 10px;
  height: 50px;
  font-size: 0.98rem;
  font-weight: 800;
  letter-spacing: 0.01em;
  border: none;
  border-radius: 15px;
  background: linear-gradient(180deg, #3b82f6 0%, #2563eb 34%, #1d4ed8 66%, #0f172a 100%);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.42),
    inset 0 -2px 0 rgba(2, 6, 23, 0.4),
    0 12px 24px rgba(37, 99, 235, 0.32),
    0 2px 4px rgba(15, 23, 42, 0.22);
  transition: transform 0.2s ease, box-shadow 0.2s ease, filter 0.2s ease;
}

.auth-submit::after {
  content: '';
  position: absolute;
  top: -60%;
  left: -140%;
  width: 55%;
  height: 220%;
  transform: rotate(20deg);
  pointer-events: none;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.55), transparent);
  animation: authSubmitShine 5.5s ease-in-out infinite;
}

.auth-submit:hover {
  transform: translateY(-1px);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.5),
    inset 0 -2px 0 rgba(2, 6, 23, 0.4),
    0 18px 32px rgba(37, 99, 235, 0.4),
    0 3px 6px rgba(15, 23, 42, 0.22);
}

/* pressed: the pill sinks back into the card */
.auth-submit:active {
  transform: translateY(1px);
  box-shadow:
    inset 0 3px 8px rgba(2, 6, 23, 0.5),
    0 4px 10px rgba(37, 99, 235, 0.24);
}

@keyframes authSubmitShine {
  0%        { left: -140%; }
  40%, 100% { left: 170%; }
}

.switch-copy {
  margin-top: 20px;
  text-align: center;
  color: #475467;
  font-size: 0.92rem;
}

.switch-copy a {
  color: #2563eb;
  font-weight: 800;
  text-decoration: none;
  border-bottom: 1px solid rgba(37, 99, 235, 0.32);
}

.switch-copy a:hover {
  color: #1d4ed8;
  border-bottom-color: rgba(29, 78, 216, 0.75);
}

/* 640px panels are a desktop choice — stacked on a phone they double the page */
@media (max-width: 900px) {
  .auth-panel {
    min-height: 0;
    border-radius: 22px;
  }

  .brand-panel {
    padding: 20px;
  }

  .brand-copy {
    padding: 24px;
  }

  .brand-mark {
    width: 64px;
    height: 64px;
  }

  .brand-copy h1 {
    font-size: 1.9rem;
  }

  .form-panel {
    padding: 18px 14px 26px;
  }

  .auth-card {
    border-radius: 22px;
  }

  .auth-card :deep(.el-card__body) {
    padding: 22px 18px;
  }
}

/* the crest float and its sheen sweep are decoration only */
@media (prefers-reduced-motion: reduce) {
  .brand-mark,
  .brand-mark::after,
  .auth-submit::after {
    animation: none;
  }
}

/* ---- Dark theme surfaces ---- */
:global(html.dark) .form-panel {
  background:
    radial-gradient(760px 520px at 112% -16%, rgba(96, 165, 250, 0.16), transparent 62%),
    radial-gradient(620px 460px at -14% 114%, rgba(37, 99, 235, 0.14), transparent 64%),
    linear-gradient(180deg, #0b1220 0%, #0f172a 100%);
}
:global(html.dark) .auth-card {
  background: linear-gradient(168deg, var(--surface) 0%, var(--surface-elevated) 54%, var(--surface-2) 100%);
  border-color: var(--border-color);
}
:global(html.dark) .card-header h2 {
  background: linear-gradient(180deg, #ffffff 0%, #cfe3ff 44%, #93c5fd 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: #dbeafe;
  filter:
    drop-shadow(0 1px 0 rgba(59, 130, 246, 0.5))
    drop-shadow(0 10px 18px rgba(0, 0, 0, 0.55));
}
:global(html.dark) .card-kicker {
  background: linear-gradient(180deg, #1e3a8a 0%, #1d4ed8 100%);
  color: #dbeafe;
  border-color: rgba(147, 197, 253, 0.35);
  text-shadow: none;
}
:global(html.dark .auth-card .el-form-item__label) { color: var(--text-body); }
:global(html.dark .auth-card .el-input__wrapper) {
  background: linear-gradient(180deg, var(--surface-muted) 0%, var(--surface) 62%);
  border-color: var(--border-color);
}
:global(html.dark .auth-card .el-input__wrapper.is-focus) {
  background: var(--surface);
  border-color: rgba(96, 165, 250, 0.7);
}
:global(html.dark .auth-card .el-input__inner:-webkit-autofill) {
  -webkit-text-fill-color: #e6edf6;
  caret-color: #e6edf6;
}
:global(html.dark) .switch-copy { color: var(--text-muted); }
:global(html.dark) .switch-copy a { color: #93c5fd; }
</style>