<template>
  <div class="saved-page">
    <section class="saved-hero">
      <div class="hero-copy">
        <span class="eyebrow">{{ $t('Watchlist') }}</span>
        <h1>{{ $t('Saved') }} <em>{{ $t('tickets') }}</em></h1>
        <p>{{ $t('Everything you are keeping an eye on, in one place. We will notify you when a saved auction ends.') }}</p>
      </div>
      <div class="hero-count">
        <strong>{{ tickets.length }}</strong>
        <span>{{ $t('saved') }}</span>
      </div>
    </section>

    <div v-if="tickets.length" class="stat-row">
      <div class="stat-card">
        <span>{{ $t('Active auctions') }}</span>
        <strong>{{ stats.active }}</strong>
      </div>
      <div class="stat-card highlight">
        <span>{{ $t('Ending soon') }}</span>
        <strong>{{ stats.endingSoon }}</strong>
      </div>
      <div class="stat-card">
        <span>{{ $t('Ended') }}</span>
        <strong>{{ stats.ended }}</strong>
      </div>
    </div>

    <div class="saved-grid" v-loading="loading">
      <article
        v-for="(ticket, i) in tickets"
        :key="ticket.id"
        class="saved-card"
        :style="{ animationDelay: `${i * 60}ms` }"
        role="link"
        tabindex="0"
        @click="open(ticket)"
        @keyup.enter="open(ticket)"
      >
        <div class="card-cover" :style="getVisualStyle(ticket.eventName)">
          <img
            v-if="getImageUrl(ticket.imageUrls?.[0])"
            :src="getImageUrl(ticket.imageUrls?.[0])"
            alt=""
            @error="$event.target.style.display = 'none'"
          >
          <WatchButton class="card-watch" :ticket-id="ticket.id" @change="onWatchChange" />
          <span class="card-status" :class="statusClass(ticket)">{{ $t(statusOf(ticket)) }}</span>
          <span v-if="timeLeft(ticket)" class="card-timer">⏳ {{ timeLeft(ticket) }}</span>
        </div>

        <div class="card-body">
          <h3>{{ ticket.eventName }}</h3>
          <ul class="card-meta">
            <li v-if="ticket.eventDate">
              <span class="meta-label">{{ $t('Date') }}</span>
              <span class="meta-value">{{ formatDate(ticket.eventDate) }}</span>
            </li>
            <li v-if="ticket.eventLocation">
              <span class="meta-label">{{ $t('Venue') }}</span>
              <span class="meta-value">{{ ticket.eventLocation }}</span>
            </li>
            <li v-if="ticket.sellerUsername">
              <span class="meta-label">{{ $t('Seller') }}</span>
              <span class="meta-value">{{ ticket.sellerUsername }}</span>
            </li>
          </ul>

          <div class="card-footer">
            <div class="price-block">
              <span>{{ $t('Starting at') }}</span>
              <strong>${{ ticket.startPrice }}</strong>
            </div>
            <span class="open-link">{{ $t('Open auction →') }}</span>
          </div>
        </div>
      </article>
    </div>

    <el-empty
      v-if="!loading && !tickets.length"
      :description="$t('No saved tickets yet. Tap the heart on any listing to keep it here.')"
    >
      <router-link to="/search" class="browse-button">{{ $t('Browse tickets') }}</router-link>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getWatchlist } from '@/api/watchlist'
import { getImageUrl } from '@/api/tickets'
import WatchButton from '@/components/WatchButton.vue'
import { useWatchlistStore } from '@/stores/watchlist'
import { t } from '@/i18n'

const router = useRouter()
const watchlist = useWatchlistStore()

const tickets = ref([])
const loading = ref(true)

const stats = computed(() => tickets.value.reduce((acc, ticket) => {
  const status = statusOf(ticket)
  if (status === 'Active') {
    acc.active++
    if (endingSoon(ticket)) acc.endingSoon++
  } else {
    acc.ended++
  }
  return acc
}, { active: 0, endingSoon: 0, ended: 0 }))

function statusOf(ticket) {
  if (ticket.status !== 'active') return 'Closed'
  if (ticket.auctionStatus && ticket.auctionStatus !== 'ongoing') return 'Closed'
  if (ticket.auctionEndTime && new Date(ticket.auctionEndTime) <= new Date()) return 'Expired'
  return 'Active'
}

function statusClass(ticket) {
  return statusOf(ticket) === 'Active' ? 'active' : 'closed'
}

function endingSoon(ticket) {
  if (!ticket.auctionEndTime) return false
  return new Date(ticket.auctionEndTime) - Date.now() < 86400000
}

function timeLeft(ticket) {
  if (statusOf(ticket) !== 'Active' || !ticket.auctionEndTime) return null
  const diff = new Date(ticket.auctionEndTime) - Date.now()
  if (diff <= 0) return null
  const d = Math.floor(diff / 86400000)
  const h = Math.floor((diff % 86400000) / 3600000)
  const m = Math.floor((diff % 3600000) / 60000)
  if (d > 0) return t('{d}d {h}h left', { d, h })
  if (h > 0) return t('{h}h {m}m left', { h, m })
  return t('{m}m left', { m })
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })
}

function getVisualStyle(eventName) {
  const palettes = [
    'linear-gradient(135deg, #1d4ed8 0%, #60a5fa 100%)',
    'linear-gradient(135deg, #0f172a 0%, #334155 100%)',
    'linear-gradient(135deg, #2563eb 0%, #0ea5e9 100%)',
    'linear-gradient(135deg, #7c3aed 0%, #a78bfa 100%)'
  ]
  return { background: palettes[(eventName?.length || 0) % palettes.length] }
}

function open(ticket) {
  router.push(`/auction/${ticket.id}`)
}

/* Un-hearting from this page should remove the card right away. */
function onWatchChange(result) {
  if (result?.watching === false) {
    tickets.value = tickets.value.filter((item) => watchlist.isWatched(item.id))
  }
}

onMounted(async () => {
  try {
    const res = await getWatchlist()
    tickets.value = res.data || []
  } catch (err) {
    ElMessage.error(t('Could not load your saved tickets'))
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.saved-page {
  display: grid;
  gap: 24px;
  padding-bottom: 20px;
}

.saved-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 30px 34px;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 24px;
  background:
    radial-gradient(120% 180% at 12% -30%, rgba(59, 130, 246, 0.22), transparent 60%),
    linear-gradient(150deg, #ffffff 0%, #eff6ff 100%);
  box-shadow: inset 0 1px 0 #fff, 0 16px 38px rgba(30, 64, 175, 0.12);
}

.hero-copy { display: grid; gap: 10px; }

.eyebrow {
  justify-self: start;
  padding: 5px 12px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.12);
  color: #1d4ed8;
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.saved-hero h1 {
  margin: 0;
  color: #0f172a;
  font-size: 2.1rem;
  font-weight: 800;
  letter-spacing: -0.03em;
}
.saved-hero h1 em {
  font-style: normal;
  background: linear-gradient(120deg, #2563eb, #7c3aed);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.saved-hero p {
  max-width: 560px;
  margin: 0;
  color: #475467;
  font-size: 0.95rem;
  line-height: 1.6;
}

.hero-count {
  display: grid;
  place-items: center;
  gap: 2px;
  flex-shrink: 0;
  width: 108px;
  height: 108px;
  border-radius: 50%;
  background: linear-gradient(150deg, #2563eb, #1e40af);
  color: #fff;
  box-shadow: inset 0 2px 6px rgba(255, 255, 255, 0.35), 0 14px 30px rgba(37, 99, 235, 0.35);
}
.hero-count strong { font-size: 2rem; font-weight: 800; line-height: 1; }
.hero-count span { font-size: 0.72rem; font-weight: 700; letter-spacing: 0.08em; text-transform: uppercase; opacity: 0.85; }

.stat-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}
.stat-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
}
.stat-card span { color: #667085; font-size: 0.85rem; font-weight: 600; }
.stat-card strong { color: #0f172a; font-size: 1.5rem; font-weight: 800; }
.stat-card.highlight { border-color: rgba(245, 158, 11, 0.4); background: linear-gradient(180deg, #fffbeb, #fff); }
.stat-card.highlight strong { color: #b45309; }

.saved-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 22px;
  min-height: 120px;
}

.saved-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 20px;
  background: #fff;
  cursor: pointer;
  box-shadow: 0 10px 26px rgba(15, 23, 42, 0.08);
  animation: cardIn 0.45s cubic-bezier(0.22, 1, 0.36, 1) both;
  transition: transform 0.28s ease, box-shadow 0.28s ease;
}
@keyframes cardIn {
  from { opacity: 0; transform: translateY(14px); }
  to { opacity: 1; transform: translateY(0); }
}
.saved-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 22px 46px rgba(30, 64, 175, 0.2);
}
.saved-card:focus-visible { outline: 3px solid rgba(37, 99, 235, 0.45); outline-offset: 2px; }

.card-cover {
  position: relative;
  height: 168px;
  overflow: hidden;
}
.card-cover img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.22, 1, 0.36, 1);
}
.saved-card:hover .card-cover img { transform: scale(1.07); }
.card-cover::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(2, 6, 23, 0.05) 30%, rgba(2, 6, 23, 0.6) 100%);
}

.card-watch {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 4;
  width: 38px;
  min-width: 38px;
  height: 38px;
  padding: 0;
}

.card-status {
  position: absolute;
  left: 12px;
  bottom: 12px;
  z-index: 3;
  padding: 5px 11px;
  border-radius: 999px;
  font-size: 0.7rem;
  font-weight: 800;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}
.card-status.active { background: rgba(34, 197, 94, 0.95); color: #fff; }
.card-status.closed { background: rgba(255, 255, 255, 0.92); color: #475467; }

.card-timer {
  position: absolute;
  right: 12px;
  bottom: 12px;
  z-index: 3;
  padding: 5px 11px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.78);
  color: #fff;
  font-size: 0.72rem;
  font-weight: 700;
  backdrop-filter: blur(4px);
}

.card-body {
  display: grid;
  gap: 12px;
  padding: 16px 18px 18px;
}

.card-body h3 {
  margin: 0;
  color: #0f172a;
  font-size: 1.06rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1.35;
}

.card-meta {
  display: grid;
  gap: 6px;
  margin: 0;
  padding: 0;
  list-style: none;
}
.card-meta li {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 0.82rem;
}
.meta-label {
  flex-shrink: 0;
  min-width: 54px;
  color: #98a2b3;
  font-weight: 700;
  font-size: 0.72rem;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}
.meta-value {
  color: #475467;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.22);
}
.price-block { display: grid; gap: 1px; }
.price-block span { color: #98a2b3; font-size: 0.7rem; font-weight: 700; letter-spacing: 0.04em; text-transform: uppercase; }
.price-block strong { color: #1d4ed8; font-size: 1.15rem; font-weight: 800; }
.open-link {
  color: #2563eb;
  font-size: 0.82rem;
  font-weight: 800;
  white-space: nowrap;
}
.saved-card:hover .open-link { text-decoration: underline; }

.browse-button {
  display: inline-flex;
  padding: 11px 24px;
  border-radius: 999px;
  background: linear-gradient(135deg, #1d4ed8, #3b82f6);
  color: #fff;
  font-weight: 700;
  text-decoration: none;
  box-shadow: 0 12px 24px rgba(37, 99, 235, 0.3);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.browse-button:hover { transform: translateY(-2px); box-shadow: 0 18px 34px rgba(37, 99, 235, 0.4); }

@media (max-width: 760px) {
  .saved-hero { flex-direction: column; align-items: flex-start; padding: 24px; }
  .saved-hero h1 { font-size: 1.7rem; }
  .stat-row { grid-template-columns: 1fr; }
}

/* ---- dark mode ---- */
:global(html.dark) .saved-hero {
  background:
    radial-gradient(120% 180% at 12% -30%, rgba(59, 130, 246, 0.24), transparent 60%),
    linear-gradient(150deg, var(--surface-2) 0%, var(--surface-muted) 100%);
  border-color: var(--border-color);
}
:global(html.dark) .saved-hero h1,
:global(html.dark) .card-body h3,
:global(html.dark) .stat-card strong { color: var(--text-strong); }
:global(html.dark) .saved-hero p,
:global(html.dark) .meta-value { color: var(--text-muted); }
:global(html.dark) .eyebrow { background: rgba(59, 130, 246, 0.2); color: #93c5fd; }
:global(html.dark) .stat-card,
:global(html.dark) .saved-card {
  background: var(--surface-2);
  border-color: var(--border-color);
}
:global(html.dark) .stat-card.highlight {
  background: linear-gradient(180deg, rgba(245, 158, 11, 0.14), var(--surface-2));
}
</style>
