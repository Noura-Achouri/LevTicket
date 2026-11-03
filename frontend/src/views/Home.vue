<template>
  <div class="marketplace-page">
    <section
      class="hero-section"
      ref="heroRef"
      @mousemove="onHeroMove"
      @mouseleave="onHeroLeave"
    >
      <div class="hero-scene" ref="sceneRef" aria-hidden="true">
        <div class="blob blob-1"></div>
        <div class="blob blob-2"></div>
        <div class="blob blob-3"></div>
        <div class="blob blob-4"></div>
        <div class="ticket-chip t-1"></div>
        <div class="ticket-chip t-2"></div>
        <div class="ticket-chip t-3"></div>
      </div>

      <div class="hero-copy" ref="copyRef">
        <span class="eyebrow">{{ $t('Trusted ticket trading for live events') }}</span>
        <h1>{{ $t('Find your next unforgettable night out.') }}</h1>
        <p>
          {{ $t('Buy and sell verified event tickets in a secure marketplace built for fans, collectors, and premium experiences.') }}
        </p>

        <div class="search-row">
          <el-input
            v-model="keyword"
            :placeholder="$t('Search concerts, sports, festivals...')"
            clearable
            size="large"
            @keyup.enter="goSearch"
          >
            <template #append>
              <el-button type="primary" @click="goSearch">{{ $t('Search') }}</el-button>
            </template>
          </el-input>
        </div>

        <div class="hero-metrics">
          <div>
            <strong>18k+</strong>
            <span>{{ $t('Verified listings') }}</span>
          </div>
          <div>
            <strong>4.9/5</strong>
            <span>{{ $t('Buyer satisfaction') }}</span>
          </div>
          <div>
            <strong>2 min</strong>
            <span>{{ $t('Average response') }}</span>
          </div>
        </div>
      </div>

      <div class="hero-visual" ref="visualRef">
        <div class="ticket-showcase primary">
          <div class="showcase-tag-row">
            <span class="ticket-badge">{{ $t('Featured') }}</span>
            <span class="ticket-date">Jun 24</span>
          </div>
          <div class="showcase-body">
            <h3>{{ $t('Summer Sound Festival') }}</h3>
            <p class="ticket-location">
              <el-icon><Location /></el-icon>
              {{ $t('Golden Gate Park, San Francisco') }}
            </p>
          </div>
          <div class="ticket-price-row">
            <span>{{ $t('From') }}</span>
            <strong>$148</strong>
          </div>
          <button class="showcase-cta" @click="goSearch">{{ $t('Get tickets') }}</button>
        </div>

        <div class="ticket-showcase secondary">
          <div class="live-label">
            <span class="live-dot"></span>
            {{ $t('Live auction') }}
          </div>
          <strong class="live-title">{{ $t('Premier League Final') }}</strong>
          <div class="live-bid-row">
            <span class="live-bid-label">{{ $t('Current bid') }}</span>
            <span class="live-bid">$1,240</span>
          </div>
          <div class="live-progress"><span></span></div>
          <span class="live-hint">{{ $t('Ends in {time} · {seats} seats left', { time: '14 min', seats: 2 }) }}</span>
        </div>
      </div>
    </section>

    <section class="category-strip" :aria-label="$t('Popular categories')">
      <button
        v-for="cat in categories"
        :key="cat.id ?? cat.name"
        type="button"
        class="category-item"
        @click="goCategory(cat)"
      >
        <span>{{ categoryEmoji(cat.name) }}</span>
        {{ $t(cat.name) }}
      </button>
    </section>

    <section class="content-block">
      <div class="section-header">
        <div class="section-heading">
          <h2>{{ $t('Hot tickets right now') }}</h2>
          <span class="eyebrow dark">{{ $t('Marketplace') }}</span>
        </div>
        <router-link to="/search" class="view-link">{{ $t('Browse all') }}</router-link>
      </div>

      <el-row :gutter="20" v-loading="loading">
        <el-col :xs="24" :md="12" :lg="8" v-for="(ticket, i) in tickets" :key="ticket.id" class="ticket-col">
          <el-card
            class="deal-card"
            shadow="never"
            :style="{ animationDelay: `${i * 80}ms` }"
            @click="$router.push(`/auction/${ticket.id}`)"
            @mousemove="onDealMove"
            @mouseleave="onDealLeave"
          >
            <div class="deal-visual" :style="getVisualStyle(ticket.eventName)">
              <img v-if="getImageUrl(ticket.imageUrls?.[0])" :src="getImageUrl(ticket.imageUrls?.[0])" alt="" @error="$event.target.style.display = 'none'">
              <WatchButton class="deal-watch" :ticket-id="ticket.id" />
              <span class="deal-badge">{{ ticket.categoryName || $t('Featured') }}</span>
              <span class="deal-open">{{ $t('View auction →') }}</span>
            </div>
            <div class="deal-body">
              <div class="deal-header-row">
                <h3>{{ ticket.eventName }}</h3>
                <el-tag class="status-tag" :type="getListingStatus(ticket) === 'Active' ? 'success' : 'info'">
                  {{ $t(getListingStatus(ticket)) }}
                </el-tag>
              </div>

              <p v-if="ticket.categoryName" class="deal-category">{{ ticket.categoryName }}</p>

              <ul class="deal-details">
                <li v-if="ticket.eventLocation">
                  <el-icon><Location /></el-icon>
                  <span class="detail-label">{{ $t('Venue') }}</span>
                  <span class="detail-value">{{ ticket.eventLocation }}</span>
                </li>
                <li>
                  <el-icon><User /></el-icon>
                  <span class="detail-label">{{ $t('Seller') }}</span>
                  <router-link class="detail-value seller-link" :to="`/profile/${ticket.sellerId}`" @click.stop>
                    {{ ticket.sellerUsername || $t('Seller') }}
                  </router-link>
                </li>
              </ul>

              <div v-if="getTags(ticket.tags).length" class="tag-row">
                <el-tag v-for="tag in getTags(ticket.tags)" :key="tag" size="small">{{ tag }}</el-tag>
              </div>

              <div class="deal-meta">
                <span>{{ $t('Current bid') }}</span>
                <span class="deal-price">
                  <strong>${{ ticket.currentBid ?? ticket.startPrice }}</strong>
                  <em class="bid-chip">{{ $t('Bid now') }}</em>
                </span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="!loading && !tickets.length" :description="$t('No tickets available')" />
    </section>

    <section class="promise-grid">
      <article>
        <span class="promise-icon">✅</span>
        <h3>{{ $t('Secure checkout') }}</h3>
        <p>{{ $t('Protected payments and transparent transfer steps from start to finish.') }}</p>
      </article>
      <article>
        <span class="promise-icon">🎯</span>
        <h3>{{ $t('Curated listings') }}</h3>
        <p>{{ $t('Only verified sellers and high-quality inventory are listed for each event.') }}</p>
      </article>
      <article>
        <span class="promise-icon">⚡</span>
        <h3>{{ $t('Instant support') }}</h3>
        <p>{{ $t('Get help from our team and AI concierge whenever you need it.') }}</p>
      </article>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { Location, User } from '@element-plus/icons-vue'
import { getImageUrl, getTickets } from '@/api/tickets'
import { getCategories } from '@/api/categories'
import WatchButton from '@/components/WatchButton.vue'

const router = useRouter()
const tickets = ref([])
const categories = ref([])
const keyword = ref('')
const loading = ref(true)

/* emoji shown on each category chip, matched by category name */
const CATEGORY_EMOJI = {
  Sports: '🏟️',
  Football: '⚽',
  Music: '🎵',
  Concerts: '🎵',
  Flights: '✈️',
  Other: '🎫',
  Theater: '🎭',
  'Live Shows': '🎤',
  Festivals: '🎊'
}

/* used only if the categories API is unavailable, so the strip never disappears */
const FALLBACK_CATEGORIES = [
  { id: null, name: 'Sports' },
  { id: null, name: 'Music' },
  { id: null, name: 'Theater' },
  { id: null, name: 'Live Shows' },
  { id: null, name: 'Festivals' }
]

function categoryEmoji(name) {
  return CATEGORY_EMOJI[name] || '🎫'
}

/* clicking a chip opens the search page filtered by that category */
function goCategory(cat) {
  if (cat.id != null) {
    router.push({ path: '/search', query: { categoryId: cat.id } })
  } else {
    router.push({ path: '/search', query: { keyword: cat.name } })
  }
}

/* ---- 3D hero parallax ---- */
const heroRef = ref(null)
const sceneRef = ref(null)
const copyRef = ref(null)
const visualRef = ref(null)

let reduceMotion = false
const parallax = { tx: 0, ty: 0, cx: 0, cy: 0, frame: 0 }

function onHeroMove(event) {
  if (reduceMotion) return
  const rect = heroRef.value.getBoundingClientRect()
  parallax.tx = ((event.clientX - rect.left) / rect.width - 0.5) * 2
  parallax.ty = ((event.clientY - rect.top) / rect.height - 0.5) * 2
  if (!parallax.frame) parallax.frame = requestAnimationFrame(renderParallax)
}

function onHeroLeave() {
  if (reduceMotion) return
  parallax.tx = 0
  parallax.ty = 0
  if (!parallax.frame) parallax.frame = requestAnimationFrame(renderParallax)
}

function applyParallax(cy, cx) {
  if (sceneRef.value) sceneRef.value.style.transform = `rotateX(${(-cy * 7).toFixed(3)}deg) rotateY(${(cx * 9).toFixed(3)}deg)`
  if (copyRef.value) copyRef.value.style.transform = `translateZ(34px) rotateX(${(-cy * 2.4).toFixed(3)}deg) rotateY(${(cx * 3.2).toFixed(3)}deg)`
  if (visualRef.value) visualRef.value.style.transform = `translateZ(60px) rotateX(${(-cy * 3.6).toFixed(3)}deg) rotateY(${(cx * 4.8).toFixed(3)}deg)`
}

function renderParallax() {
  const p = parallax
  p.cx += (p.tx - p.cx) * 0.08
  p.cy += (p.ty - p.cy) * 0.08
  if (Math.abs(p.tx - p.cx) < 0.0005 && Math.abs(p.ty - p.cy) < 0.0005) {
    p.cx = p.tx
    p.cy = p.ty
    applyParallax(p.cy, p.cx)
    p.frame = 0
    return
  }
  applyParallax(p.cy, p.cx)
  p.frame = requestAnimationFrame(renderParallax)
}

function goSearch() {
  router.push({ path: '/search', query: { keyword: keyword.value } })
}

/* ---- 3D tilt on deal cards ---- */
function onDealMove(event) {
  if (reduceMotion) return
  const card = event.currentTarget
  const rect = card.getBoundingClientRect()
  const px = Math.min(Math.max((event.clientX - rect.left) / rect.width, 0), 1)
  const py = Math.min(Math.max((event.clientY - rect.top) / rect.height, 0), 1)
  card.style.setProperty('--rx', `${((0.5 - py) * 8).toFixed(2)}deg`)
  card.style.setProperty('--ry', `${((px - 0.5) * 10).toFixed(2)}deg`)
  card.style.setProperty('--gx', `${(px * 100).toFixed(1)}%`)
  card.style.setProperty('--gy', `${(py * 100).toFixed(1)}%`)
  card.classList.add('tilting')
}

function onDealLeave(event) {
  const card = event.currentTarget
  card.classList.remove('tilting')
}

function getVisualStyle(eventName) {
  const palettes = [
    'linear-gradient(135deg, #1d4ed8 0%, #60a5fa 100%)',
    'linear-gradient(135deg, #0f172a 0%, #334155 100%)',
    'linear-gradient(135deg, #2563eb 0%, #0ea5e9 100%)',
    'linear-gradient(135deg, #7c3aed 0%, #a78bfa 100%)'
  ]
  const index = (eventName?.length || 0) % palettes.length
  return { background: palettes[index] }
}

function getTags(tags) {
  return Array.isArray(tags) ? tags : (tags || '').split(',').map((tag) => tag.trim()).filter(Boolean)
}

function getListingStatus(ticket) {
  if (ticket.status !== 'active') return 'Closed'
  if (ticket.auctionStatus && ticket.auctionStatus !== 'ongoing') return 'Closed'
  if (ticket.auctionEndTime && new Date(ticket.auctionEndTime) <= new Date()) return 'Expired'
  return 'Active'
}

onMounted(async () => {
  reduceMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches || false
  if (!reduceMotion) {
    applyParallax(0, 0)
  }
  try {
    const [ticketRes, categoryRes] = await Promise.all([
      getTickets({}),
      getCategories().catch(() => ({ data: [] }))
    ])
    tickets.value = ticketRes.data.tickets
    categories.value = categoryRes.data?.length ? categoryRes.data : FALLBACK_CATEGORIES
  } catch (err) {
    console.error('Failed to load tickets:', err)
    categories.value = FALLBACK_CATEGORIES
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(() => {
  if (parallax.frame) cancelAnimationFrame(parallax.frame)
})
</script>

<style scoped>
.marketplace-page {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

/* =====================================================
   Hero — 3D stage
   Background : drifting blue blobs + spinning tickets
   Foreground : the existing ticket cards
===================================================== */
.hero-section {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 26px;
  padding: clamp(28px, 4.5vw, 56px) clamp(22px, 4vw, 54px);
  border-radius: var(--radius-lg);
  color: #fff;
  isolation: isolate;
  perspective: 1500px;
  background:
    radial-gradient(1100px 640px at 84% -14%, rgba(96, 165, 250, 0.34), transparent 60%),
    radial-gradient(760px 560px at -10% 118%, rgba(59, 130, 246, 0.3), transparent 62%),
    radial-gradient(560px 440px at 112% 96%, rgba(37, 99, 235, 0.38), transparent 66%),
    linear-gradient(142deg, #040a1d 0%, #0a1a3e 46%, #0d2a70 100%);
  box-shadow: var(--shadow-strong);
}

/* ---- animated 3D scene (background layer) ---- */
.hero-scene {
  position: absolute;
  inset: -12%;
  z-index: 0;
  transform-style: preserve-3d;
  pointer-events: none;
  will-change: transform;
}

.blob {
  position: absolute;
  border-radius: 50%;
  transform-style: preserve-3d;
  will-change: transform;
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

.blob-1 { top: -24%; right: -13%; width: 480px; height: 480px; animation: blobDrift1 18s infinite; }
.blob-2 { bottom: -28%; right: -5%; width: 430px; height: 430px; animation: blobDrift2 15s infinite; }
.blob-3 { bottom: -22%; left: 6%; width: 400px; height: 400px; opacity: 0.85; animation: blobDrift3 21s infinite; }
.blob-4 { top: -16%; left: -11%; width: 460px; height: 460px; opacity: 0.8; animation: blobDrift4 16s infinite; }

@keyframes blobDrift1 {
  0%   { transform: translate3d(0, 0, -260px) scale(1); }
  100% { transform: translate3d(90px, 60px, -260px) scale(1.22); }
}
@keyframes blobDrift2 {
  0%   { transform: translate3d(0, 0, -160px) scale(1.1); }
  100% { transform: translate3d(-80px, -70px, -160px) scale(0.92); }
}
@keyframes blobDrift3 {
  0%   { transform: translate3d(0, 0, -330px) scale(1); }
  100% { transform: translate3d(70px, -50px, -330px) scale(1.2); }
}
@keyframes blobDrift4 {
  0%   { transform: translate3d(0, 0, -200px) scale(1); }
  100% { transform: translate3d(-60px, 80px, -200px) scale(1.15); }
}

/* ---- floating 3D tickets ---- */
.ticket-chip {
  position: absolute;
  width: 148px;
  height: 88px;
  border-radius: 18px;
  transform-style: preserve-3d;
  will-change: transform;
  animation-timing-function: ease-in-out;
  animation-iteration-count: infinite;
}

.ticket-chip::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  border: 1px solid rgba(255, 255, 255, 0.28);
  background: linear-gradient(135deg, rgba(147, 197, 253, 0.95) 0%, rgba(59, 130, 246, 0.72) 55%, rgba(29, 78, 216, 0.85) 100%);
  box-shadow:
    inset 0 1px 1px rgba(255, 255, 255, 0.45),
    inset 0 -12px 26px rgba(15, 23, 42, 0.38),
    0 22px 46px rgba(2, 6, 23, 0.35);
}

.ticket-chip::after {
  content: '';
  position: absolute;
  right: 14px;
  bottom: 13px;
  width: 56px;
  height: 24px;
  border-radius: 7px;
  background: repeating-linear-gradient(90deg,
    rgba(255, 255, 255, 0.92) 0 3px,
    transparent 3px 5px,
    rgba(255, 255, 255, 0.92) 5px 8px,
    transparent 8px 12px);
  opacity: 0.85;
}

.t-1 { top: 7%; right: -2%; animation: ticketSpin1 19s infinite; }
.t-2 { bottom: 9%; right: 6%; animation: ticketSpin2 15s infinite; }
.t-3 { bottom: -2%; left: 27%; animation: ticketSpin3 22s infinite; }

@keyframes ticketSpin1 {
  0%   { transform: translate3d(-40px, 0, -120px) rotateZ(-8deg) rotateY(0deg); }
  50%  { transform: translate3d(20px, 16px, -120px) rotateZ(-3deg) rotateY(180deg); }
  100% { transform: translate3d(-40px, 0, -120px) rotateZ(-8deg) rotateY(360deg); }
}
@keyframes ticketSpin2 {
  0%   { transform: translate3d(30px, 0, -190px) rotateZ(10deg) rotateY(360deg); }
  50%  { transform: translate3d(-20px, -14px, -190px) rotateZ(4deg) rotateY(180deg); }
  100% { transform: translate3d(30px, 0, -190px) rotateZ(10deg) rotateY(0deg); }
}
@keyframes ticketSpin3 {
  0%   { transform: translate3d(0, 30px, -240px) rotateZ(-6deg) rotateY(0deg); }
  50%  { transform: translate3d(24px, -10px, -240px) rotateZ(-1deg) rotateY(180deg); }
  100% { transform: translate3d(0, 30px, -240px) rotateZ(-6deg) rotateY(360deg); }
}

/* ---- foreground panels (glass, sit above the scene) ---- */
.hero-copy,
.hero-visual {
  position: relative;
  z-index: 1;
}

.hero-copy {
  border-radius: var(--radius-lg);
  padding: clamp(24px, 3vw, 38px);
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.12) 0%, rgba(255, 255, 255, 0.05) 100%);
  border: 1px solid rgba(255, 255, 255, 0.16);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.14),
    0 26px 60px rgba(2, 6, 23, 0.38);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  transform-style: preserve-3d;
  will-change: transform;
}

.eyebrow {
  display: inline-flex;
  padding: 7px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.18);
  color: #dbeafe;
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.eyebrow.dark {
  background: linear-gradient(165deg, #e9effc 0%, #d7e2f8 100%);
  border-color: transparent;
  color: var(--brand-700);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.85),
    inset 0 -6px 12px rgba(37, 99, 235, 0.08),
    0 2px 6px rgba(37, 99, 235, 0.1);
}

.hero-copy h1 {
  margin-top: 20px;
  font-size: clamp(2.4rem, 4.8vw, 3.9rem);
  line-height: 1.02;
  letter-spacing: -0.06em;
  color: #fff;
  text-shadow: 0 2px 30px rgba(2, 6, 23, 0.3);
}

.hero-copy p {
  max-width: 620px;
  margin-top: 18px;
  color: rgba(226, 232, 240, 0.84);
  font-size: 1.08rem;
  line-height: 1.7;
}

.search-row {
  margin-top: 30px;
}

.search-row :deep(.el-input__wrapper) {
  border-radius: 12px 0 0 12px;
}

.search-row :deep(.el-input-group__append) {
  padding: 0;
  background: transparent;
  border: none;
  box-shadow: none;
  border-radius: 0 12px 12px 0;
}

.search-row :deep(.el-input-group__append .el-button) {
  height: 100%;
  margin: 0;
  padding: 0 22px;
  border: none;
  border-radius: 0 12px 12px 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0.95rem;
  font-weight: 700;
  white-space: nowrap;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 24px;
  margin-top: 36px;
  padding-top: 28px;
  border-top: 1px solid rgba(255, 255, 255, 0.16);
}

.hero-metrics div {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.hero-metrics strong {
  font-size: 2.1rem;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.05em;
}

.hero-metrics span {
  color: rgba(203, 213, 225, 0.78);
  font-size: 0.86rem;
  font-weight: 500;
}

.hero-visual {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 22px;
  transform-style: preserve-3d;
  will-change: transform;
}

.ticket-showcase {
  border-radius: 24px;
  overflow: hidden;
  box-shadow: var(--shadow-strong);
  transform: translateZ(0);
}

.ticket-showcase.primary {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 300px;
  padding: 26px 26px 24px;
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background:
    radial-gradient(120% 90% at 80% 0%, rgba(59, 130, 246, 0.55), transparent 60%),
    linear-gradient(145deg, #0f172a 0%, #1e3a8a 55%, #1d4ed8 100%);
  animation: cardFloat 9s ease-in-out infinite;
}

.ticket-showcase.secondary {
  animation: cardFloat 7.5s ease-in-out infinite;
  animation-delay: -2.5s;
}

@keyframes cardFloat {
  0%, 100% { transform: translate3d(0, 0, 0); }
  50% { transform: translate3d(0, -9px, 0); }
}

.showcase-tag-row,
.ticket-price-row,
.deal-header-row,
.deal-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ticket-badge {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.18);
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.ticket-date {
  color: rgba(255, 255, 255, 0.72);
  font-size: 0.82rem;
}

.showcase-body {
  margin-top: auto;
}

.ticket-showcase h3 {
  font-size: clamp(1.7rem, 3vw, 2.2rem);
  line-height: 1.05;
  letter-spacing: -0.05em;
}

.ticket-location {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.92rem;
}

.ticket-price-row {
  margin-top: 22px;
  font-size: 0.9rem;
}

.ticket-price-row strong {
  font-size: 2rem;
  letter-spacing: -0.04em;
}

.showcase-cta {
  margin-top: 18px;
  width: 100%;
  padding: 12px 16px;
  border: none;
  border-radius: 12px;
  background: #fff;
  color: #1d4ed8;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.2s ease;
}

.showcase-cta:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.25);
}

/* Live auction card (glass so the scene glows through) */
.ticket-showcase.secondary {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 22px 24px;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.18);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.12),
    0 24px 50px rgba(2, 6, 23, 0.32);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.live-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #34d399;
  font-size: 0.76rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.live-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 0 0 rgba(52, 211, 153, 0.5);
  animation: pulse 1.8s infinite;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(52, 211, 153, 0.5); }
  70% { box-shadow: 0 0 0 8px rgba(52, 211, 153, 0); }
  100% { box-shadow: 0 0 0 0 rgba(52, 211, 153, 0); }
}

.live-title {
  font-size: 1.35rem;
  color: #fff;
  letter-spacing: -0.03em;
  margin-top: 4px;
}

.live-bid-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 10px;
}

.live-bid-label {
  color: rgba(203, 213, 225, 0.8);
  font-size: 0.85rem;
}

.live-bid {
  font-size: 1.5rem;
  font-weight: 800;
  color: #fff;
  text-shadow: 0 2px 18px rgba(2, 6, 23, 0.35);
  letter-spacing: -0.03em;
}

.live-progress {
  margin-top: 12px;
  height: 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  overflow: hidden;
}

.live-progress span {
  display: block;
  height: 100%;
  width: 68%;
  border-radius: 999px;
  background: linear-gradient(90deg, #93c5fd, #3b82f6);
}

.live-hint {
  margin-top: 8px;
  color: rgba(203, 213, 225, 0.8);
  font-size: 0.82rem;
}

/* keep the sections below white */
.content-block,
.promise-grid,
.category-strip {
  background: var(--white);
  border: 1px solid var(--slate-200);
  box-shadow: var(--shadow-soft);
}

@media (max-width: 640px) {
  .hero-section {
    padding: 20px 16px;
  }
  .ticket-chip {
    display: none;
  }
  .ticket-showcase.primary {
    min-height: 236px;
    padding: 20px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .blob,
  .ticket-chip,
  .ticket-showcase,
  .live-dot {
    animation: none !important;
  }
}

.category-strip {
  position: relative;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 14px;
  padding: 16px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: linear-gradient(180deg, #ffffff 0%, #f7faff 100%);
  border: 1px solid rgba(37, 99, 235, 0.12);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.92),
    0 10px 30px rgba(37, 99, 235, 0.08),
    0 22px 50px rgba(37, 99, 235, 0.06);
}
.category-strip::before {
  content: '';
  position: absolute;
  top: 0;
  left: 24px;
  right: 24px;
  height: 3px;
  border-radius: 0 0 6px 6px;
  background: linear-gradient(90deg, transparent, rgba(37, 99, 235, 0.45), transparent);
  pointer-events: none;
}

.category-item {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 74px;
  padding: 0 16px;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(160deg, #ffffff 0%, #f2f6fd 100%);
  color: var(--text-strong);
  font-family: inherit;
  font-weight: 700;
  font-size: 0.98rem;
  letter-spacing: -0.01em;
  text-align: center;
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 2px 6px rgba(15, 23, 42, 0.05);
  cursor: pointer;
  transition:
    transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1),
    box-shadow 0.25s ease,
    border-color 0.25s ease,
    background 0.25s ease,
    color 0.25s ease;
}

/* emoji badge */
.category-item span {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  flex: none;
  border-radius: 13px;
  background: #ffffff;
  font-size: 1.2rem;
  line-height: 1;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 3px 10px rgba(15, 23, 42, 0.1);
  transition: transform 0.32s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.3s ease;
}

/* sheen sweep */
.category-item::after {
  content: '';
  position: absolute;
  top: -20%;
  bottom: -20%;
  left: -40%;
  width: 34%;
  background: linear-gradient(100deg, transparent, rgba(255, 255, 255, 0.55), transparent);
  transform: skewX(-18deg);
  opacity: 0;
  pointer-events: none;
}

.category-item:hover {
  transform: translateY(-4px);
  color: var(--brand-700);
  background: linear-gradient(160deg, #eff6ff 0%, #dbeafe 100%);
  border-color: rgba(37, 99, 235, 0.3);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 12px 26px rgba(37, 99, 235, 0.22);
}
.category-item:hover span {
  transform: scale(1.14) rotate(-6deg);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 8px 18px rgba(37, 99, 235, 0.28);
}
.category-item:hover::after { animation: catShine 0.8s ease; }
.category-item:active { transform: translateY(-1px) scale(0.99); }
.category-item:focus-visible {
  outline: none;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 0 0 3px rgba(37, 99, 235, 0.35);
}

@keyframes catShine {
  0% { opacity: 0; left: -40%; }
  30% { opacity: 1; }
  100% { opacity: 0; left: 130%; }
}

.content-block {
  position: relative;
  border-radius: var(--radius-lg);
  padding: 32px 28px 20px;
  overflow: hidden;
  background: linear-gradient(180deg, #ffffff 0%, #fafcff 100%);
  border: 1px solid rgba(37, 99, 235, 0.12);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.95),
    0 10px 30px rgba(37, 99, 235, 0.07),
    0 24px 54px rgba(37, 99, 235, 0.06);
}
.content-block::before {
  content: '';
  position: absolute;
  top: 0;
  left: 28px;
  right: 28px;
  height: 3px;
  border-radius: 0 0 6px 6px;
  background: linear-gradient(90deg, transparent, rgba(37, 99, 235, 0.45), transparent);
  pointer-events: none;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 30px;
}

.section-header h2 {
  position: relative;
  display: inline-block;
  margin-top: 12px;
  font-size: clamp(1.8rem, 2.8vw, 2.4rem);
  font-weight: 850;
  letter-spacing: -0.045em;
  color: var(--brand-900);
  text-wrap: balance;
}

/* short gradient underline accent */
.section-header h2::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -12px;
  width: 58px;
  height: 4px;
  border-radius: 999px;
  background: linear-gradient(90deg, #2563eb, #7c3aed);
  box-shadow: 0 3px 10px rgba(37, 99, 235, 0.35);
  transform: scaleX(0);
  transform-origin: left;
  animation: barGrow 0.7s 0.15s cubic-bezier(0.22, 1, 0.36, 1) forwards;
}
@keyframes barGrow {
  to { transform: scaleX(1); }
}

/* title on top, "Marketplace" chip tucked underneath it */
.section-heading {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 26px;
}

.section-header .section-heading h2 {
  margin-top: 0;
}

.view-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border-radius: 999px;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  color: #fff;
  font-size: 0.9rem;
  font-weight: 700;
  letter-spacing: 0.01em;
  text-decoration: none;
  white-space: nowrap;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.3),
    0 6px 16px rgba(37, 99, 235, 0.3);
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.25s ease, filter 0.25s ease;
}
.view-link::after {
  content: '→';
  transition: transform 0.25s ease;
}
.view-link:hover {
  transform: translateY(-2px);
  filter: brightness(1.06);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.35),
    0 12px 26px rgba(37, 99, 235, 0.42);
}
.view-link:hover::after { transform: translateX(4px); }
.view-link:active { transform: translateY(0) scale(0.98); }

.ticket-col {
  margin-bottom: 20px;
}

.deal-card {
  --rx: 0deg;
  --ry: 0deg;
  --gx: 50%;
  --gy: 50%;
  position: relative;
  height: 100%;
  border-radius: 22px;
  cursor: pointer;
  overflow: hidden;
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.08);
  will-change: transform;
  animation: dealIn 0.55s cubic-bezier(0.22, 1, 0.36, 1) backwards;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 1px 2px rgba(15, 23, 42, 0.05),
    0 12px 28px rgba(15, 23, 42, 0.08);
  transition:
    transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1),
    box-shadow 0.3s ease,
    border-color 0.3s ease;
}

@keyframes dealIn {
  from { opacity: 0; transform: translateY(24px) scale(0.97); }
}

.deal-card:hover {
  border-color: rgba(37, 99, 235, 0.4);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 2px 6px rgba(15, 23, 42, 0.06),
    0 30px 60px rgba(15, 23, 42, 0.18),
    0 18px 44px rgba(37, 99, 235, 0.18);
}

/* cursor-tracked 3D tilt */
.deal-card.tilting {
  transform: perspective(950px) rotateX(var(--rx)) rotateY(var(--ry)) translateY(-6px);
  transition: transform 0.09s linear, box-shadow 0.09s linear, border-color 0.09s linear;
}
.deal-card:not(.tilting):hover {
  transform: perspective(950px) translateY(-6px);
}

/* cursor-following glare */
.deal-card::after {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 6;
  pointer-events: none;
  border-radius: inherit;
  opacity: 0;
  background: radial-gradient(340px circle at var(--gx) var(--gy), rgba(255, 255, 255, 0.5), rgba(255, 255, 255, 0) 46%);
  mix-blend-mode: overlay;
  transition: opacity 0.25s ease;
}
.deal-card:hover::after,
.deal-card.tilting::after { opacity: 1; }

.deal-card :deep(.el-card__body) {
  display: flex;
  flex: 1;
  flex-direction: column;
  padding: 0;
}

.deal-visual {
  position: relative;
  display: flex;
  flex: 0 0 auto;
  align-items: flex-end;
  justify-content: flex-start;
  padding: 18px 18px 16px;
  min-height: 160px;
  color: white;
  font-weight: 700;
  overflow: hidden;
}

/* dark gradient so the badge stays readable */
.deal-visual::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(2, 6, 23, 0) 34%, rgba(2, 6, 23, 0.62) 100%);
  transition: background 0.35s ease;
}
.deal-card:hover .deal-visual::after {
  background: linear-gradient(180deg, rgba(2, 6, 23, 0) 20%, rgba(2, 6, 23, 0.78) 100%);
}

.deal-visual img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.22, 1, 0.36, 1);
}
.deal-card:hover .deal-visual img { transform: scale(1.08); }

/* save-to-watchlist heart, floats over the cover */
.deal-watch {
  position: absolute;
  top: 14px;
  right: 14px;
  z-index: 4;
  width: 40px;
  min-width: 40px;
  height: 40px;
  padding: 0;
}

/* sheen sweep across the cover */
.deal-visual::before {
  content: '';
  position: absolute;
  top: -20%;
  bottom: -20%;
  left: -45%;
  width: 34%;
  z-index: 2;
  background: linear-gradient(100deg, transparent, rgba(255, 255, 255, 0.38), transparent);
  transform: skewX(-18deg);
  opacity: 0;
  pointer-events: none;
}
.deal-card:hover .deal-visual::before { animation: dealShine 0.95s ease forwards; }
@keyframes dealShine {
  0% { opacity: 0; left: -45%; }
  30% { opacity: 1; }
  100% { opacity: 0; left: 130%; }
}

/* category badge */
.deal-badge {
  position: relative;
  z-index: 3;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.94);
  color: #0f172a;
  font-size: 0.7rem;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.18);
  transition: transform 0.25s ease, background 0.25s ease, color 0.25s ease, box-shadow 0.25s ease;
}
.deal-card:hover .deal-badge {
  background: #2563eb;
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 6px 14px rgba(37, 99, 235, 0.35);
}

/* "View auction" chip slides up on hover */
.deal-open {
  position: absolute;
  right: 16px;
  bottom: 16px;
  z-index: 3;
  padding: 7px 13px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.95);
  color: #1d4ed8;
  font-size: 0.74rem;
  font-weight: 800;
  letter-spacing: 0.02em;
  box-shadow: 0 6px 18px rgba(2, 6, 23, 0.3);
  opacity: 0;
  transform: translateY(12px);
  transition: opacity 0.25s ease, transform 0.28s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.deal-card:hover .deal-open {
  opacity: 1;
  transform: translateY(0);
}

.deal-body {
  display: flex;
  flex: 1;
  flex-direction: column;
  padding: 18px;
}

.deal-header-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.deal-header-row h3 {
  flex: 1;
  min-width: 0;
  font-size: 1.2rem;
  line-height: 1.25;
  color: var(--brand-900);
  letter-spacing: -0.04em;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.25s ease;
}
.deal-card:hover .deal-header-row h3 { color: #1d4ed8; }

.deal-card :deep(.status-tag) {
  border: 0;
  border-radius: 999px;
  font-weight: 700;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

/* small uppercase category label under the title */
.deal-category {
  margin-top: 10px;
  color: var(--brand-700);
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

/* aligned label/value rows for venue + seller */
.deal-details {
  display: grid;
  gap: 8px;
  margin: 14px 0 0;
  padding: 0;
  list-style: none;
}

.deal-details li {
  display: grid;
  grid-template-columns: 18px 58px minmax(0, 1fr);
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  line-height: 1.35;
}

.deal-details .el-icon {
  color: var(--brand-700);
  font-size: 15px;
}

.detail-label {
  color: #98a2b3;
  font-weight: 600;
}

.detail-value {
  min-width: 0;
  color: #475467;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.seller-link {
  color: #2563eb;
  text-decoration: none;
  width: fit-content;
  max-width: 100%;
  transition: color 0.2s ease;
}
.seller-link:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

.deal-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid #eef2f6;
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: #98a2b3;
}

.deal-price {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.deal-meta strong {
  font-size: 1.5rem;
  color: var(--brand-900);
  letter-spacing: -0.05em;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  transition: color 0.25s ease, transform 0.25s ease;
}
.deal-card:hover .deal-meta strong {
  color: #1d4ed8;
  transform: scale(1.06);
}

/* "Bid now" pill appears on hover */
.bid-chip {
  font-style: normal;
  font-weight: 800;
  font-size: 0.74rem;
  color: #fff;
  padding: 6px 12px;
  border-radius: 999px;
  white-space: nowrap;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.3),
    0 6px 14px rgba(37, 99, 235, 0.35);
  opacity: 0;
  transform: translateX(10px);
  transition: opacity 0.25s ease, transform 0.28s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.deal-card:hover .bid-chip {
  opacity: 1;
  transform: translateX(0);
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.deal-card :deep(.tag-row .el-tag) {
  border-radius: 999px;
  background: #f1f5f9;
  border-color: transparent;
  color: #475467;
  font-weight: 600;
  transition: background 0.2s ease, color 0.2s ease, transform 0.2s ease;
}
.deal-card:hover :deep(.tag-row .el-tag) {
  background: #eff6ff;
  color: #1d4ed8;
}
.deal-card :deep(.tag-row .el-tag):hover {
  background: #2563eb;
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(37, 99, 235, 0.28);
}

.promise-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
  padding: 28px;
  border-radius: var(--radius-lg);
}

.promise-grid article {
  padding: 28px 24px;
  border-radius: var(--radius-md);
  background: var(--slate-100);
  border: 1px solid var(--slate-200);
}

.promise-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: var(--brand-100);
  font-size: 1.6rem;
}

.promise-grid h3 {
  margin-top: 20px;
  font-size: 1.2rem;
  color: var(--brand-900);
}

.promise-grid p {
  margin-top: 12px;
  color: var(--slate-500);
  line-height: 1.7;
}

@media (max-width: 980px) {
  .hero-section,
  .category-strip,
  .promise-grid {
    grid-template-columns: 1fr;
  }

  .hero-copy {
    padding: 26px 22px;
  }

  .hero-metrics {
    grid-template-columns: 1fr;
  }
}

/* ---- Dark theme surfaces ---- */
:global(html.dark) .content-block {
  background: linear-gradient(180deg, var(--surface) 0%, var(--surface-2) 100%);
  border-color: var(--border-color);
}
:global(html.dark) .category-strip {
  background: linear-gradient(180deg, var(--surface) 0%, var(--surface-2) 100%);
  border-color: var(--border-color);
}
:global(html.dark) .category-item {
  background: linear-gradient(160deg, var(--surface-2) 0%, var(--surface-muted) 100%);
  color: var(--text-body);
  border-color: var(--border-soft);
}
:global(html.dark) .category-item span { background: var(--surface); }
:global(html.dark) .category-item:hover {
  background: linear-gradient(160deg, rgba(37, 99, 235, 0.28), rgba(37, 99, 235, 0.16));
  color: #93c5fd;
}
:global(html.dark) .deal-card {
  background: var(--surface);
  border-color: var(--border-color);
}
:global(html.dark) .deal-meta { border-top-color: var(--divider); }
:global(html.dark) .detail-label { color: var(--text-soft); }
:global(html.dark) .detail-value { color: var(--text-body); }
:global(html.dark .deal-card .tag-row .el-tag) {
  background: var(--surface-muted);
  color: var(--text-body);
}
:global(html.dark) .eyebrow.dark {
  background: linear-gradient(165deg, var(--surface-muted), var(--surface-2));
  color: #93c5fd;
}
:global(html.dark) .promise-grid article {
  background: var(--surface-muted);
  border-color: var(--border-color);
}
</style>