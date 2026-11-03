<template>
  <div class="results-page">
    <section class="results-header">
      <i class="orb orb-a"></i>
      <i class="orb orb-b"></i>
      <div class="hero-copy">
        <h1>{{ $t('Search') }} <span class="hl">{{ $t('tickets') }}<i></i></span></h1>
        <p class="page-sub">{{ $t('Search, bid, and win premium event tickets — live auctions, honest sellers, secure checkout.') }}</p>
        <div class="hero-meta">
          <span class="eyebrow dark"><i></i>{{ $t('Browse events') }}</span>
          <span class="meta-hint">{{ $t('Live auctions across every category') }}</span>
        </div>
      </div>
      <div class="summary-pill">
        <b>{{ tickets.length }}</b>
        <span>{{ tickets.length === 1 ? $t('listing') : $t('listings') }}</span>
      </div>
    </section>

    <div class="filters-panel">
      <el-input v-model="keyword" :placeholder="$t('Search event name...')" @keyup.enter="search" />
      <el-select v-model="categoryId" :placeholder="$t('Category')" clearable @change="search">
        <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
      </el-select>
      <el-select v-model="location" :placeholder="$t('Location / country')" clearable filterable @change="search">
        <el-option v-for="country in countries" :key="country" :label="country" :value="country" />
      </el-select>
      <el-select v-model="sort" @change="search">
        <el-option :label="$t('Recent')" value="recent" />
        <el-option :label="$t('Ending Soon')" value="ending_soon" />
        <el-option :label="$t('Price: Low to High')" value="price_asc" />
        <el-option :label="$t('Price: High to Low')" value="price_desc" />
      </el-select>
      <el-button type="primary" class="search-btn" @click="search">{{ $t('Search') }}</el-button>
    </div>

    <div class="results-grid" v-loading="loading">
      <article
        v-for="(ticket, i) in tickets"
        :key="ticket.id"
        class="result-card"
        :style="{ animationDelay: `${i * 70}ms` }"
        @click="$router.push(`/auction/${ticket.id}`)"
        @mousemove="onCardMove"
        @mouseleave="onCardLeave"
      >
        <div class="result-cover" :style="getVisualStyle(ticket.eventName)">
          <img v-if="getImageUrl(ticket.imageUrls?.[0])" :src="getImageUrl(ticket.imageUrls?.[0])" alt="" @error="$event.target.style.display = 'none'">
          <WatchButton class="cover-watch" :ticket-id="ticket.id" />
          <span v-if="timeLeft(ticket)" class="cover-time" :class="{ ended: getListingStatus(ticket) !== 'Active' }">⏳ {{ timeLeft(ticket) }}</span>
          <span class="cover-cat">{{ ticket.categoryName || $t('Event') }}</span>
          <span v-if="getListingStatus(ticket) === 'Active'" class="cover-open">{{ $t('Open auction →') }}</span>
        </div>
        <div class="result-body">
          <div class="result-top">
            <h3>{{ ticket.eventName }}</h3>
            <span class="status" :class="getStatusClass(ticket)"><i></i>{{ $t(getListingStatus(ticket)) }}</span>
          </div>
          <p class="result-cat">{{ ticket.categoryName }}</p>
          <p class="seller-line">{{ $t('Sold by') }} <router-link :to="`/profile/${ticket.sellerId}`" @click.stop>{{ ticket.sellerUsername || $t('Seller') }}</router-link></p>
          <div class="tag-row">
            <el-tag v-for="tag in getTags(ticket.tags)" :key="tag" size="small">{{ tag }}</el-tag>
          </div>
          <div class="result-price">
            <span>{{ $t('Current bid') }}</span>
            <span class="price-side">
              <strong>${{ ticket.currentBid ?? ticket.startPrice }}</strong>
              <em class="view-hint">{{ $t('Bid now') }}</em>
            </span>
          </div>
        </div>
      </article>
    </div>

    <el-empty v-if="!loading && !tickets.length" :description="$t('No tickets found')" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getImageUrl, getTickets } from '@/api/tickets'
import { getCategories } from '@/api/categories'
import WatchButton from '@/components/WatchButton.vue'
import { countries } from '@/data/countries'
import { t } from '@/i18n'

const route = useRoute()
const keyword = ref('')
const categoryId = ref(null)
const location = ref('')
const sort = ref('recent')
const tickets = ref([])
const categories = ref([])
const loading = ref(true)

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

function getStatusClass(ticket) {
  return getListingStatus(ticket) === 'Active' ? 'active' : 'closed'
}

// ---- 3D tilt + glare tracking ----
function onCardMove(e) {
  const card = e.currentTarget
  const r = card.getBoundingClientRect()
  const px = Math.min(Math.max((e.clientX - r.left) / r.width, 0), 1)
  const py = Math.min(Math.max((e.clientY - r.top) / r.height, 0), 1)
  card.style.setProperty('--rx', `${((0.5 - py) * 10).toFixed(2)}deg`)
  card.style.setProperty('--ry', `${((px - 0.5) * 12).toFixed(2)}deg`)
  card.style.setProperty('--gx', `${(px * 100).toFixed(1)}%`)
  card.style.setProperty('--gy', `${(py * 100).toFixed(1)}%`)
  card.classList.add('tilting')
}

function onCardLeave(e) {
  const card = e.currentTarget
  card.classList.remove('tilting')
}

function timeLeft(ticket) {
  if (getListingStatus(ticket) !== 'Active') return null
  if (!ticket.auctionEndTime) return null
  const diff = new Date(ticket.auctionEndTime) - Date.now()
  if (diff <= 0) return null
  const d = Math.floor(diff / 86400000)
  const h = Math.floor((diff % 86400000) / 3600000)
  const m = Math.floor((diff % 3600000) / 60000)
  if (d > 0) return t('{d}d {h}h left', { d, h })
  if (h > 0) return t('{h}h {m}m left', { h, m })
  return t('{m}m left', { m })
}

async function search() {
  loading.value = true
  try {
    const res = await getTickets({ keyword: keyword.value, categoryId: categoryId.value, location: location.value, sort: sort.value })
    tickets.value = res.data.tickets
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  keyword.value = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  const rawCategory = Number(route.query.categoryId)
  categoryId.value = Number.isInteger(rawCategory) && rawCategory > 0 ? rawCategory : null
  const catRes = await getCategories().catch(() => ({ data: [] }))
  categories.value = catRes.data || []
  search()
})
</script>

<style scoped>
.results-page {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.results-page::before {
  content: '';
  position: absolute;
  top: -60px;
  left: 50%;
  transform: translateX(-50%);
  width: 860px;
  height: 420px;
  background: radial-gradient(50% 50% at 50% 50%, rgba(37, 99, 235, 0.12), transparent 70%);
  pointer-events: none;
  z-index: 0;
}

.results-page > * {
  position: relative;
  z-index: 1;
}

.results-header {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 32px 34px 30px;
  border-radius: 26px;
  overflow: hidden;
  background: linear-gradient(145deg, #f6f8ff 0%, #eef2fd 48%, #e7eefb 100%);
  border: 1px solid rgba(37, 99, 235, 0.14);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.92),
    0 12px 30px rgba(37, 99, 235, 0.1),
    0 24px 60px rgba(37, 99, 235, 0.08);
}

/* soft gradient washes inside the panel */
.results-header::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background:
    radial-gradient(60% 90% at 88% -10%, rgba(59, 130, 246, 0.16), transparent 62%),
    radial-gradient(52% 70% at 0% 112%, rgba(124, 58, 237, 0.1), transparent 62%);
}

/* diagonal glass shine that sweeps across the whole panel */
.results-header::after {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 3;
  pointer-events: none;
  background: linear-gradient(115deg, transparent 42%, rgba(255, 255, 255, 0.55) 50%, transparent 58%);
  transform: translateX(-100%);
}
.results-header:hover::after {
  animation: heroShine 0.9s ease;
}
@keyframes heroShine {
  to { transform: translateX(100%); }
}

/* floating gradient orbs */
.orb {
  position: absolute;
  z-index: 1;
  border-radius: 50%;
  pointer-events: none;
  filter: blur(1px);
}
.orb-a {
  width: 130px;
  height: 130px;
  top: -34px;
  right: 78px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.3), transparent 68%);
  animation: orbFloat 8s ease-in-out infinite;
}
.orb-b {
  width: 96px;
  height: 96px;
  bottom: -26px;
  left: 42%;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.22), transparent 68%);
  animation: orbFloat 10s ease-in-out infinite reverse;
}
@keyframes orbFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(12px, -16px) scale(1.08); }
}

.hero-copy,
.summary-pill {
  position: relative;
  z-index: 2;
}

/* meta row under the subtitle: browse chip + live hint */
.hero-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 18px;
}
.meta-hint {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: #64748b;
  font-size: 0.88rem;
  font-weight: 500;
}
.meta-hint::before {
  content: '';
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #94a3b8;
}

@media (max-width: 720px) {
  .results-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 18px;
    padding: 24px;
  }
}

.eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px;
  border-radius: 999px;
  background: #dbeafe;
  color: #1d4ed8;
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.eyebrow.dark {
  background: linear-gradient(165deg, #e9effc 0%, #d7e2f8 100%);
  color: #1d4ed8;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.85),
    inset 0 -6px 12px rgba(37, 99, 235, 0.08),
    0 2px 6px rgba(37, 99, 235, 0.1);
}

.eyebrow i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.18);
  animation: dotGlow 2.4s ease-in-out infinite;
}
@keyframes dotGlow {
  0%, 100% { box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.18); }
  50% { box-shadow: 0 0 0 5px rgba(34, 197, 94, 0.06); }
}

.results-header h1 {
  display: inline-block;
  margin: 0;
  font-size: clamp(2.3rem, 4.2vw, 3.2rem);
  font-weight: 850;
  letter-spacing: -0.045em;
  line-height: 1.05;
  color: #0b1220;
  text-wrap: balance;
  font-variant-numeric: tabular-nums;
}

/* gradient accent on the keyword */
.hl {
  position: relative;
  display: inline-block;
  background: linear-gradient(94deg, #2563eb 0%, #0ea5e9 55%, #7c3aed 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  filter: drop-shadow(0 2px 10px rgba(37, 99, 235, 0.24));
}
.hl i {
  position: absolute;
  left: 0;
  bottom: -0.06em;
  width: 100%;
  height: 0.12em;
  border-radius: 999px;
  background: linear-gradient(90deg, #2563eb, #7c3aed);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}
.results-header:hover .hl i { transform: scaleX(1); }

.page-sub {
  margin-top: 12px;
  max-width: 62ch;
  color: #5b6b83;
  font-size: 1rem;
  line-height: 1.6;
  text-wrap: balance;
}

.summary-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px 10px 12px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid rgba(37, 99, 235, 0.16);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 1px 2px rgba(15, 23, 42, 0.04),
    0 8px 20px rgba(37, 99, 235, 0.1);
  color: #475467;
  font-size: 0.9rem;
  font-weight: 600;
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}
.summary-pill:hover {
  transform: translateY(-2px);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.95),
    0 2px 6px rgba(15, 23, 42, 0.06),
    0 14px 30px rgba(37, 99, 235, 0.2);
}
.summary-pill b {
  display: inline-grid;
  place-items: center;
  min-width: 28px;
  height: 28px;
  padding: 0 9px;
  border-radius: 999px;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  color: #fff;
  font-size: 0.95rem;
  font-weight: 800;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.3), 0 4px 10px rgba(37, 99, 235, 0.3);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.summary-pill:hover b { transform: scale(1.12) rotate(-4deg); }
.summary-pill span { letter-spacing: 0.02em; }

/* ---------- Filter toolbar ---------- */
.filters-panel {
  position: relative;
  display: grid;
  grid-template-columns: minmax(220px, 1.4fr) repeat(3, minmax(150px, 0.9fr)) auto;
  gap: 12px;
  padding: 16px;
  border-radius: 18px;
  background: #ffffff;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.95),
    0 1px 2px rgba(15, 23, 42, 0.04),
    0 10px 30px rgba(15, 23, 42, 0.05),
    0 20px 50px rgba(37, 99, 235, 0.06);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}
.filters-panel:hover {
  transform: translateY(-3px);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.95),
    0 3px 8px rgba(15, 23, 42, 0.06),
    0 22px 50px rgba(37, 99, 235, 0.14);
}
.filters-panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: 22px;
  right: 22px;
  height: 3px;
  border-radius: 0 0 6px 6px;
  background: linear-gradient(90deg, transparent, rgba(37, 99, 235, 0.5), transparent);
}

.filters-panel :deep(.el-input__wrapper),
.filters-panel :deep(.el-select__wrapper) {
  min-height: 42px;
  border-radius: 10px;
  background: #fbfcfe;
  box-shadow: 0 0 0 1px #d8dee9 inset;
  transition: box-shadow 0.2s ease, background 0.2s ease;
}

.filters-panel :deep(.el-input__wrapper:hover),
.filters-panel :deep(.el-select__wrapper:hover) {
  background: #ffffff;
  box-shadow: 0 0 0 1px #9db6d8 inset;
}

.filters-panel :deep(.el-input__wrapper.is-focus),
.filters-panel :deep(.el-select__wrapper.is-focused) {
  background: #ffffff;
  box-shadow: 0 0 0 1px #2563eb inset, 0 0 0 4px rgba(37, 99, 235, 0.14);
}

.filters-panel :deep(.el-input__inner) {
  font-size: 0.95rem;
  color: #0f172a;
}

.search-btn {
  position: relative;
  overflow: hidden;
  height: 42px;
  padding: 0 24px;
  border: 0;
  border-radius: 10px;
  font-weight: 700;
  letter-spacing: 0.04em;
  background-image: linear-gradient(90deg, #2563eb 0%, #3b82f6 100%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.3), 0 4px 12px rgba(37, 99, 235, 0.25);
  transition: transform 0.15s ease, box-shadow 0.15s ease, filter 0.15s ease;
}

.search-btn::after {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  left: -60%;
  width: 35%;
  background: linear-gradient(100deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transform: skewX(-18deg);
}

.search-btn:hover {
  transform: translateY(-2px);
  filter: brightness(1.06) saturate(1.05);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.35), 0 10px 22px rgba(37, 99, 235, 0.4);
}
.search-btn:hover::after { animation: coverShine 0.9s ease; }
.search-btn:active {
  transform: translateY(0) scale(0.98);
  box-shadow: inset 0 2px 8px rgba(2, 6, 23, 0.2);
}

/* ---------- Results grid ---------- */
.results-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 22px;
  align-items: stretch;
}

.result-card {
  --rx: 0deg;
  --ry: 0deg;
  --gx: 50%;
  --gy: 50%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 18px;
  background: #ffffff;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05), 0 10px 24px rgba(15, 23, 42, 0.08);
  cursor: pointer;
  will-change: transform;
  animation: cardIn 0.55s cubic-bezier(0.22, 1, 0.36, 1) backwards;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.3s ease, border-color 0.3s ease;
}

/* entrance */
@keyframes cardIn {
  from { opacity: 0; transform: translateY(26px) scale(0.96); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.result-card:hover {
  border-color: rgba(37, 99, 235, 0.4);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 2px 6px rgba(15, 23, 42, 0.06),
    0 30px 60px rgba(15, 23, 42, 0.18),
    0 18px 44px rgba(37, 99, 235, 0.18);
}

/* while the mouse glides: track the cursor in 3D */
.result-card.tilting {
  transform: perspective(950px) rotateX(var(--rx)) rotateY(var(--ry)) translateY(-6px);
  transition: transform 0.09s linear, box-shadow 0.09s linear, border-color 0.09s linear;
}
/* spring back smoothly on leave */
.result-card:not(.tilting):hover {
  transform: perspective(950px) translateY(-6px);
}

.result-card:hover .result-top h3 { color: #1d4ed8; }

/* cursor-follow glare */
.result-card::after {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 6;
  pointer-events: none;
  border-radius: inherit;
  opacity: 0;
  background: radial-gradient(360px circle at var(--gx) var(--gy), rgba(255, 255, 255, 0.55), rgba(255, 255, 255, 0) 46%);
  mix-blend-mode: overlay;
  transition: opacity 0.25s ease;
}
.result-card:hover::after,
.result-card.tilting::after { opacity: 1; }

/* Cover */
.result-cover {
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: flex-start;
  height: 168px;
  flex: none;
  padding: 14px 16px;
  color: #fff;
  font-weight: 700;
  overflow: hidden;
}

.result-cover::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0) 40%, rgba(2, 6, 23, 0.5) 100%);
  transition: background 0.35s ease;
}
.result-card:hover .result-cover::after {
  background: linear-gradient(180deg, rgba(0, 0, 0, 0) 30%, rgba(2, 6, 23, 0.62) 100%);
}

.result-cover img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.22, 1, 0.36, 1);
}

/* save-to-watchlist heart, floats over the cover */
.cover-watch {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 5;
  width: 38px;
  min-width: 38px;
  height: 38px;
  padding: 0;
}

.result-card:hover .result-cover img { transform: scale(1.08); }

/* sheen that sweeps across the cover on hover */
.result-cover::before {
  content: '';
  position: absolute;
  top: -20%;
  bottom: -20%;
  left: -45%;
  width: 36%;
  z-index: 3;
  background: linear-gradient(100deg, transparent, rgba(255, 255, 255, 0.35), transparent);
  transform: skewX(-18deg);
  opacity: 0;
  pointer-events: none;
}
.result-card:hover .result-cover::before {
  animation: coverShine 0.95s ease forwards;
}
@keyframes coverShine {
  0% { opacity: 0; left: -45%; }
  30% { opacity: 1; }
  100% { opacity: 0; left: 130%; }
}

.cover-cat {
  position: relative;
  z-index: 1;
  padding: 5px 11px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.94);
  color: #0f172a;
  font-size: 0.7rem;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.16);
  transition: transform 0.25s ease, background 0.25s ease, color 0.25s ease;
}
.result-card:hover .cover-cat {
  background: #2563eb;
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 6px 14px rgba(37, 99, 235, 0.35);
}

.cover-time {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.62);
  border: 1px solid rgba(255, 255, 255, 0.25);
  color: #fff;
  font-size: 0.7rem;
  font-weight: 700;
  backdrop-filter: blur(6px);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.2);
}

.cover-time.ended { background: rgba(15, 23, 42, 0.5); }

/* "Open auction" chip slides up on hover */
.cover-open {
  position: absolute;
  right: 14px;
  bottom: 14px;
  z-index: 4;
  padding: 7px 13px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.95);
  color: #1d4ed8;
  font-size: 0.74rem;
  font-weight: 800;
  letter-spacing: 0.02em;
  box-shadow: 0 6px 18px rgba(2, 6, 23, 0.25);
  opacity: 0;
  transform: translateY(12px);
  transition: opacity 0.25s ease, transform 0.28s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.result-card:hover .cover-open {
  opacity: 1;
  transform: translateY(0);
}

/* Body */
.result-body {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding: 16px 18px 18px;
}

.result-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.result-top h3 {
  font-size: 1.15rem;
  font-weight: 800;
  letter-spacing: -0.025em;
  line-height: 1.2;
  color: #0f172a;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex: none;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 700;
}

.status i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.status.active {
  color: #047857;
  background: #ecfdf5;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}
.status.active i {
  background: #10b981;
  animation: statusPulse 1.8s ease-out infinite;
}
.status.closed { color: #64748b; background: #f1f5f9; }
.status.closed i { background: #94a3b8; }

@keyframes statusPulse {
  0% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4); }
  70% { box-shadow: 0 0 0 6px rgba(16, 185, 129, 0); }
  100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
}

.result-cat {
  margin-top: 8px;
  color: #64748b;
  font-size: 0.82rem;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.seller-line {
  margin-top: 8px;
  color: #98a2b3;
  font-size: 0.82rem;
}

.seller-line a {
  color: #2563eb;
  text-decoration: none;
  font-weight: 600;
  position: relative;
  transition: color 0.2s ease;
}

.seller-line a::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -1px;
  height: 1.5px;
  width: 100%;
  background: #2563eb;
  border-radius: 999px;
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.25s ease;
}

.seller-line a:hover { color: #1d4ed8; }
.seller-line a:hover::after { transform: scaleX(1); }

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 14px;
}

.tag-row :deep(.el-tag) {
  border-radius: 999px;
  background: #f1f5f9;
  border-color: transparent;
  color: #475467;
  font-weight: 600;
  transition: background 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.result-card:hover .tag-row :deep(.el-tag) {
  background: #eff6ff;
  color: #1d4ed8;
  border-color: rgba(37, 99, 235, 0.2);
}
.result-card .tag-row :deep(.el-tag):hover {
  background: #2563eb;
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(37, 99, 235, 0.28);
}

.result-price {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid #eef2f6;
  color: #98a2b3;
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.price-side {
  display: flex;
  align-items: center;
  gap: 10px;
}

.result-price strong {
  font-size: 1.5rem;
  color: #0f172a;
  letter-spacing: -0.05em;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  transition: color 0.25s ease, transform 0.25s ease;
}
.result-card:hover .result-price strong {
  color: #1d4ed8;
  transform: scale(1.06);
}

/* "Bid now" pill appears on hover */
.view-hint {
  font-style: normal;
  font-weight: 800;
  font-size: 0.74rem;
  color: #fff;
  padding: 6px 12px;
  border-radius: 999px;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.3), 0 6px 14px rgba(37, 99, 235, 0.35);
  white-space: nowrap;
  opacity: 0;
  transform: translateX(10px);
  transition: opacity 0.25s ease, transform 0.28s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.result-card:hover .view-hint {
  opacity: 1;
  transform: translateX(0);
}

@media (max-width: 980px) {
  .filters-panel,
  .results-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 520px) {
  .result-cover { height: 150px; }
}

/* ---- Dark theme surfaces ---- */
:global(html.dark) .results-header {
  background: linear-gradient(145deg, var(--surface) 0%, var(--surface-2) 48%, var(--surface-muted) 100%);
  border-color: var(--border-color);
}
:global(html.dark) .results-header h1 { color: var(--text-strong); }
:global(html.dark) .page-sub { color: var(--text-muted); }
:global(html.dark) .meta-hint { color: var(--text-muted); }
:global(html.dark) .summary-pill {
  background: var(--surface);
  border-color: var(--border-color);
  color: var(--text-body);
}
:global(html.dark) .filters-panel {
  background: var(--surface);
  border-color: var(--border-color);
}
:global(html.dark .filters-panel .el-select__wrapper) {
  background: var(--surface-2);
  border-color: var(--border-color);
}
:global(html.dark .filters-panel .el-input__inner) { color: var(--text-strong); }
:global(html.dark) .result-card {
  background: var(--surface);
  border-color: var(--border-color);
}
:global(html.dark) .result-top h3 { color: var(--text-strong); }
:global(html.dark) .result-cat { color: var(--text-muted); }
:global(html.dark) .seller-line { color: var(--text-soft); }
:global(html.dark) .result-price {
  border-top-color: var(--divider);
  color: var(--text-soft);
}
:global(html.dark) .result-price strong { color: var(--text-strong); }
:global(html.dark) .status.closed { color: var(--text-muted); background: var(--surface-muted); }
:global(html.dark .result-card .tag-row .el-tag) {
  background: var(--surface-muted);
  color: var(--text-body);
}
</style>