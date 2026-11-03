<template>
  <div class="page" v-loading="loading">
    <template v-if="ticket">
      <div class="auction-layout">
        <!-- MAIN -->
        <div class="auction-main">
          <!-- Hero -->
          <div class="gallery">
            <div class="gallery-hero" :style="heroStyle">
              <img v-if="activeImageUrl" :src="activeImageUrl" class="hero-img" :alt="$t('Event cover')" @error="$event.target.style.display = 'none'">
              <div class="hero-overlay"></div>
              <span class="status-badge" :class="auctionLabel === 'Active auction' ? 'live' : 'off'">
                <i></i>{{ $t(auctionLabel) }}
              </span>
              <WatchButton class="hero-watch" :ticket-id="ticket.id" />
              <div class="hero-caption">
                <span class="hero-eyebrow">{{ ticket.categoryName || $t('Live auction') }}</span>
                <h1>{{ ticket.eventName }}</h1>
                <span class="hero-meta">
                  <template v-if="ticket.eventLocation">{{ ticket.eventLocation }} · </template>{{ formatTime(ticket.eventDate) }}
                </span>
                <div class="hero-chips">
                  <span class="chip"><b>{{ $t('Seat') }}</b> {{ ticket.seatInfo || $t('General admission') }}</span>
                  <span class="chip"><b>{{ $t('Delivery') }}</b> {{ $t('Instant mobile transfer') }}</span>
                </div>
              </div>
            </div>
            <div v-if="images.length > 1" class="gallery-thumbs">
              <button
                v-for="(img, i) in images"
                :key="img"
                :class="{ active: i === activeImage }"
                @click="activeImage = i"
              >
                <img :src="getImageUrl(img)" alt="" @error="$event.target.style.display = 'none'">
              </button>
            </div>
          </div>

          <!-- Description -->
          <section class="card desc-card">
            <div class="section-head">
              <span class="section-icon">🎟</span>
              <div>
                <h3>{{ $t('About this ticket') }}</h3>
                <p class="section-sub">{{ $t('Everything you need to know before bidding.') }}</p>
              </div>
            </div>
            <p class="desc-text">{{ ticket.description }}</p>
            <div class="info-grid">
              <div class="info-item">
                <span class="info-ico ico-date">📅</span>
                <div class="info-body"><span>{{ $t('Event date') }}</span><b>{{ formatTime(ticket.eventDate) || '—' }}</b></div>
              </div>
              <div class="info-item">
                <span class="info-ico ico-venue">📍</span>
                <div class="info-body"><span>{{ $t('Venue') }}</span><b>{{ ticket.eventLocation || '—' }}</b></div>
              </div>
              <div class="info-item">
                <span class="info-ico ico-seat">💺</span>
                <div class="info-body"><span>{{ $t('Seat') }}</span><b>{{ ticket.seatInfo || $t('General admission') }}</b></div>
              </div>
              <div class="info-item">
                <span class="info-ico ico-cat">🏷</span>
                <div class="info-body"><span>{{ $t('Category') }}</span><b>{{ ticket.categoryName || '—' }}</b></div>
              </div>
              <div class="info-item">
                <span class="info-ico ico-end">⏳</span>
                <div class="info-body"><span>{{ $t('Bidding ends') }}</span><b>{{ auction?.endTime ? formatTime(auction.endTime) : '—' }}</b></div>
              </div>
              <div class="info-item">
                <span class="info-ico ico-del">📲</span>
                <div class="info-body"><span>{{ $t('Delivery') }}</span><b>{{ $t('Instant mobile transfer') }}</b></div>
              </div>
            </div>
          </section>

          <!-- Seller -->
          <section class="card seller-card">
            <div class="seller-top">
              <div class="seller-avatar">{{ initials(sellerName) }}</div>
              <div class="seller-info">
                <p class="seller-label">{{ $t('Sold by') }}</p>
                <router-link v-if="ticket.sellerId" class="seller-name" :to="`/profile/${ticket.sellerId}`">{{ sellerName }}</router-link>
                <span v-else class="seller-name">{{ sellerName }}</span>
                <p class="seller-meta">{{ $t('Verified seller · Instant mobile transfer') }}</p>
              </div>
              <div class="seller-actions">
                <button
                  v-if="ticket.sellerId && !isSeller"
                  type="button"
                  class="message-seller-btn"
                  @click="openSellerChat"
                >
                  💬 {{ $t('Message seller') }}
                </button>
                <span v-if="ticket.seller?.trustScore" class="trust">★ {{ $t('{score} trust', { score: ticket.seller.trustScore }) }}</span>
                <router-link v-if="ticket.sellerId" :to="`/profile/${ticket.sellerId}`" class="profile-link">{{ $t('View profile →') }}</router-link>
              </div>
            </div>
          </section>

          <!-- Bid history -->
          <section class="card history-card">
            <h3>{{ $t('Bid History') }} <span class="count">{{ bidHistory.length }}</span></h3>
            <div v-if="bidHistory.length" class="history-list">
              <div v-for="(row, i) in bidHistory" :key="i" class="history-row" :class="{ leader: i === 0 }">
                <span class="rank">#{{ bidHistory.length - i }}</span>
                <span class="bidder"><i>{{ (row.bidderUsername || $t('Buyer')).charAt(0).toUpperCase() }}</i>{{ row.bidderUsername || $t('Buyer') }}</span>
                <span class="amount">${{ row.amount }}</span>
                <span class="time">{{ formatTime(row.bidTime) }}</span>
              </div>
            </div>
            <el-empty v-else :description="$t('No bids yet — be the first!')" :image-size="60" />
          </section>
        </div>

        <!-- SIDEBAR -->
        <aside class="auction-side">
          <div class="bid-panel">
            <div class="panel-head">
              <span class="panel-label">{{ $t('Current bid') }}</span>
              <span v-if="canBid" class="live"><i></i>{{ $t('Live') }}</span>
            </div>
            <div class="bid-amount" :class="{ flash: liveUpdated }">${{ auction?.currentBid ?? '—' }}</div>

            <div class="panel-meta">
              <span v-if="watcherCount > 0" class="meta-chip">👀 {{ $t('{count} watching', { count: watcherCount }) }}</span>
              <span class="meta-chip">{{ $t('{count} bids', { count: bidHistory.length }) }}</span>
              <span v-if="livePolling" class="meta-chip live-chip"><i></i>{{ $t('Live updates') }}</span>
            </div>

            <div v-if="auction" class="countdown" :class="{ ended: isAuctionExpired }">
              <template v-if="!isAuctionExpired">⏳ {{ $t('Ends in') }} {{ countdown }}</template>
              <template v-else>{{ $t('Auction ended') }}</template>
            </div>

            <template v-if="canBid">
              <div class="bid-actions">
                <p class="next-min">{{ $t('Next bid: at least') }} <b>${{ (auction.currentBid || 0) + 1 }}</b></p>
                <div class="bid-input">
                  <el-input-number v-model="bidAmount" :min="(auction.currentBid || 0) + 1" :step="1" />
                  <el-button type="primary" class="place-btn" @click="placeBidNow">{{ $t('Place Bid →') }}</el-button>
                </div>
              </div>
            </template>

            <div v-else-if="isSeller" class="side-note">{{ $t("You're the seller of this ticket.") }}</div>
            <div v-else-if="isAuctionExpired" class="side-note">{{ $t('Bidding has closed.') }}</div>
            <div v-else class="side-note">{{ $t('Bidding is closed.') }}</div>

            <div v-if="auction && auction.status !== 'ongoing' && auction.winnerId" class="winner">
              🏆 {{ $t('Winner') }}: {{ auction.winnerUsername || $t('Buyer') }}
            </div>
          </div>

          <!-- Guarantee -->
          <div class="guarantee-card">
            <div class="guarantee-head">
              <span class="guarantee-icon">🛡️</span>
              <h4>{{ $t('Buy with confidence') }}</h4>
            </div>
            <ul>
              <li><i></i>{{ $t('Verified seller & genuine ticket') }}</li>
              <li><i></i>{{ $t('Instant mobile transfer on payment') }}</li>
              <li><i></i>{{ $t('Secure checkout & buyer protection') }}</li>
            </ul>
          </div>

          <!-- Seller close action -->
          <div v-if="isSeller && auction && auction.status === 'ongoing'" class="seller-actions">
            <el-button type="danger" :loading="closing" @click="closeNow">
              {{ $t('🔨 Close auction & deliver ticket') }}
            </el-button>
            <p class="reveal-note">
              {{ $t('Closing ends bidding immediately. The highest bidder instantly receives the private ticket photo.') }}
            </p>
          </div>

          <!-- Private ticket photo -->
          <div v-if="privatePhotos.length > 0" class="reveal-panel">
            <h3>{{ $t('🎟️ Your ticket') }}</h3>
            <p v-if="!isSeller" class="reveal-note">{{ $t('The seller closed the auction — your ticket is ready below.') }}</p>
            <div class="ticket-images">
              <img v-for="img in privatePhotos" :key="img" :src="getImageUrl(img)" :alt="$t('Your ticket')" @error="$event.target.style.display = 'none'">
            </div>
          </div>
        </aside>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getImageUrl, getTicketById, getTicketPhoto } from '@/api/tickets'
import { getAuctionByTicket, getBidHistory, placeBid, closeAuction } from '@/api/auctions'
import { getWatcherCount } from '@/api/watchlist'
import WatchButton from '@/components/WatchButton.vue'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { t } from '@/i18n'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const chat = useChatStore()
const ticket = ref(null)
const auction = ref(null)
const bidHistory = ref([])
const bidAmount = ref(0)
const activeImage = ref(0)
const privatePhotos = ref([])
const loading = ref(true)
const closing = ref(false)
const watcherCount = ref(0)
const liveUpdated = ref(false)
const livePolling = ref(false)

/* Polling keeps the bid panel in sync while the auction is open. */
const POLL_INTERVAL = 5000
let pollTimer = null
let lastKnownTop = null

const isSeller = computed(
  () => !!auth.user?.id && ticket.value != null && ticket.value.sellerId === auth.user.id
)
const canBid = computed(
  () => !!auction.value && auction.value.status === 'ongoing' && !isAuctionExpired.value && !isSeller.value
)
const isAuctionExpired = computed(
  () => !!auction.value?.endTime && new Date(auction.value.endTime) <= new Date()
)
const auctionLabel = computed(() => {
  if (auction.value?.status !== 'ongoing') return 'Closed'
  return isAuctionExpired.value ? 'Expired' : 'Active auction'
})
const sellerName = computed(() => ticket.value?.sellerUsername || ticket.value?.seller?.username || 'Seller')
const images = computed(() => ticket.value?.imageUrls || [])
const activeImageUrl = computed(() => getImageUrl(images.value[activeImage.value]))
const heroStyle = computed(() => {
  const palettes = [
    'linear-gradient(135deg, #1d4ed8 0%, #60a5fa 100%)',
    'linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%)',
    'linear-gradient(135deg, #0f172a 0%, #334155 100%)',
    'linear-gradient(135deg, #4f46e5 0%, #818cf8 100%)'
  ]
  const idx = (ticket.value?.eventName?.length || 0) % palettes.length
  return { background: palettes[idx] }
})
const countdown = computed(() => {
  if (!auction.value?.endTime) return ''
  const diff = new Date(auction.value.endTime) - Date.now()
  if (diff <= 0) return t('Ended')
  const d = Math.floor(diff / 86400000)
  const h = Math.floor((diff % 86400000) / 3600000)
  const m = Math.floor((diff % 3600000) / 60000)
  const s = Math.floor((diff % 60000) / 1000)
  if (d > 0) return `${d}d ${h}h ${m}m`
  if (h > 0) return `${h}h ${m}m ${s}s`
  return `${m}m ${s}s`
})

function formatTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : ''
}

function initials(name) {
  return (name || '?').trim().charAt(0).toUpperCase()
}

/* Opens the floating buyer <-> seller chat, keeping the bidder on this page. */
function openSellerChat() {
  if (!ticket.value?.sellerId) return
  if (!auth.token) {
    ElMessage.warning(t('Sign in to message the seller'))
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (isSeller.value) return
  chat.openWith({
    id: ticket.value.sellerId,
    username: sellerName.value,
    ticketId: ticket.value.id,
    ticketLabel: ticket.value.eventName
  })
}


async function loadBidHistory() {
  if (!auction.value) return
  const res = await getBidHistory(auction.value.id)
  if (res.code === 200) bidHistory.value = res.data || []
}

async function loadPrivatePhoto() {
  privatePhotos.value = []
  if (!ticket.value || !auth.user?.id) return
  try {
    const res = await getTicketPhoto(ticket.value.id)
    if (res.code === 200 && res.data?.imageUrls?.length) {
      privatePhotos.value = res.data.imageUrls
    }
  } catch (err) {
    // Not entitled yet (e.g. auction still open) - simply stay hidden.
  }
}

async function placeBidNow() {
  if (!auction.value) return
  try {
    const res = await placeBid(auction.value.id, bidAmount.value)
    if (res.code !== 200) {
      ElMessage.error(res.message || t('Failed to place bid'))
      return
    }
    auction.value.currentBid = res.data.newCurrentBid
    bidAmount.value = res.data.newCurrentBid + 1
    lastKnownTop = res.data.newCurrentBid
    ElMessage.success(t('Bid placed!'))
    await loadBidHistory()
  } catch (err) {
    ElMessage.error(err?.message || t('Failed to place bid'))
  }
}

async function closeNow() {
  if (!auction.value) return
  closing.value = true
  try {
    const res = await closeAuction(auction.value.id)
    if (res.code !== 200) {
      ElMessage.error(res.message || t('Failed to close auction'))
      return
    }
    if (res.data?.winnerId) {
      ElMessage.success(t('Auction closed - ticket delivered to the winning bidder!'))
    } else {
      ElMessage.success(t('Auction closed - no bids yet, your ticket stays available.'))
    }
    await loadDetail()
  } catch (err) {
    ElMessage.error(err?.message || t('Failed to close auction'))
  } finally {
    closing.value = false
  }
}

async function loadWatcherCount() {
  try {
    const res = await getWatcherCount(route.params.id)
    watcherCount.value = res.data?.count || 0
  } catch {
    watcherCount.value = 0
  }
}

/**
 * Silent background refresh: pulls the latest bid + history without a loading
 * spinner, and flashes the amount when somebody outbids us mid-session.
 */
async function refreshLive() {
  if (!auction.value || auction.value.status !== 'ongoing') return
  try {
    const res = await getAuctionByTicket(route.params.id)
    const fresh = res.data
    if (!fresh) return

    const newTop = fresh.currentBid
    if (lastKnownTop != null && Number(newTop) > Number(lastKnownTop)) {
      const iAmTopBidder = auth.user?.id && fresh.winnerId === auth.user.id
      if (!iAmTopBidder && !isSeller.value) {
        ElMessage.warning(t("You've been outbid! Someone placed a higher bid."))
      }
      liveUpdated.value = true
      setTimeout(() => { liveUpdated.value = false }, 1400)
    }
    lastKnownTop = newTop

    auction.value = { ...auction.value, ...fresh }
    bidAmount.value = Math.max(bidAmount.value, (fresh.currentBid || 0) + 1)
    await loadBidHistory()
  } catch {
    // Transient network hiccup - keep the last known state on screen.
  }
}

function startLivePolling() {
  stopLivePolling()
  livePolling.value = true
  pollTimer = setInterval(refreshLive, POLL_INTERVAL)
}

function stopLivePolling() {
  if (pollTimer) clearInterval(pollTimer)
  pollTimer = null
  livePolling.value = false
}

async function loadDetail() {
  stopLivePolling()
  loading.value = true
  try {
    const res = await getTicketById(route.params.id)
    ticket.value = res.data
    try {
      const auctionRes = await getAuctionByTicket(route.params.id)
      auction.value = auctionRes.data
      bidAmount.value = (auction.value.currentBid || 0) + 1
      lastKnownTop = auction.value.currentBid
      await loadBidHistory()
      if (auction.value.status === 'ongoing') startLivePolling()
    } catch {
      auction.value = null
    }
    await loadPrivatePhoto()
    await loadWatcherCount()
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)
onBeforeUnmount(stopLivePolling)
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* ---------- Layout ---------- */
.auction-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 380px;
  gap: 24px;
  align-items: start;
}
.auction-main { display: flex; flex-direction: column; gap: 20px; min-width: 0; }

/* ---------- Hero ---------- */
.gallery {
  overflow: hidden;
  border-radius: 20px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 10px 24px rgba(15, 23, 42, 0.06);
}
.gallery-hero {
  position: relative;
  height: 320px;
  overflow: hidden;
  display: flex;
  align-items: flex-end;
}
.hero-img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; }
.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(2, 6, 23, 0.2) 0%, rgba(2, 6, 23, 0) 42%, rgba(2, 6, 23, 0.78) 100%);
}

.status-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 3;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 7px 13px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.22);
  backdrop-filter: blur(6px);
  font-weight: 700;
  font-size: 0.75rem;
}
.status-badge i { width: 8px; height: 8px; border-radius: 50%; }
.status-badge.live { color: #047857; }
.status-badge.live i { background: #10b981; box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.18); animation: pulse 1.6s infinite; }
.status-badge.off { color: #64748b; }
.status-badge.off i { background: #94a3b8; }

/* save-to-watchlist heart, top-left so it never collides with the status badge */
.hero-watch {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 4;
  width: 44px;
  min-width: 44px;
  height: 44px;
  padding: 0;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4); }
  70% { box-shadow: 0 0 0 5px rgba(16, 185, 129, 0); }
  100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
}

.hero-caption {
  position: relative;
  z-index: 2;
  padding: 26px;
  color: #fff;
}
.hero-eyebrow {
  display: inline-flex;
  padding: 5px 11px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  color: #0f172a;
  font-size: 0.7rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}
.hero-caption h1 {
  margin-top: 12px;
  font-size: clamp(1.7rem, 2.8vw, 2.4rem);
  letter-spacing: -0.04em;
  line-height: 1.1;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.45);
  max-width: 20ch;
}
.hero-meta {
  display: block;
  margin-top: 8px;
  color: rgba(255, 255, 255, 0.92);
  font-weight: 600;
  font-size: 0.95rem;
  text-shadow: 0 1px 6px rgba(0, 0, 0, 0.4);
}
.hero-chips { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 14px; }
.chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 13px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(4px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.18);
  font-size: 0.8rem;
  color: #0f172a;
}
.chip b { font-weight: 700; }

.gallery-thumbs { display: flex; gap: 10px; padding: 12px; background: #fff; border-top: 1px solid #eef2f6; }
.gallery-thumbs button {
  border: 2px solid transparent;
  border-radius: 12px;
  overflow: hidden;
  padding: 0;
  cursor: pointer;
  background: #fff;
  transition: border-color 0.15s ease;
}
.gallery-thumbs button:hover { border-color: #93c5fd; }
.gallery-thumbs button.active { border-color: #2563eb; }
.gallery-thumbs img { display: block; width: 88px; height: 60px; object-fit: cover; }

/* ---------- Cards ---------- */
.card {
  position: relative;
  background:
    radial-gradient(120% 60% at 0% 0%, rgba(37, 99, 235, 0.04), transparent 55%),
    #fff;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 18px;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 1px 2px rgba(15, 23, 42, 0.04),
    0 8px 20px rgba(15, 23, 42, 0.05);
  padding: 22px;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}
.card:hover {
  transform: translateY(-3px);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 3px 8px rgba(15, 23, 42, 0.06),
    0 20px 44px rgba(37, 99, 235, 0.12);
}
.card h3 { margin: 0; font-size: 1.1rem; color: #0f172a; letter-spacing: -0.02em; }

/* Section head with icon chip */
.section-head { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
.section-icon {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  flex: none;
  border-radius: 13px;
  font-size: 19px;
  background:
    linear-gradient(150deg, #eff6ff, #dbeafe) padding-box;
  border: 1px solid rgba(37, 99, 235, 0.15);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 4px 10px rgba(37, 99, 235, 0.14);
}
.section-sub { margin: 3px 0 0; color: #98a2b3; font-size: 0.82rem; }

.desc-text {
  margin: 0;
  padding: 14px 16px;
  border-radius: 12px;
  background: linear-gradient(90deg, #f8fafc, #f4f6fb);
  border: 1px solid #eef2f7;
  color: #475467;
  line-height: 1.7;
  font-size: 0.92rem;
}

/* Info grid as raised icon tiles */
.info-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}
.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #eef2f7;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}
.info-item:hover {
  transform: translateY(-4px);
  border-color: rgba(37, 99, 235, 0.35);
  background: #fbfdff;
  box-shadow: 0 10px 22px rgba(37, 99, 235, 0.14);
}
.info-ico {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  flex: none;
  border-radius: 11px;
  font-size: 16px;
  transition: transform 0.2s ease;
}
.info-item:hover .info-ico { transform: scale(1.1) rotate(-4deg); }
.ico-date { background: #eff6ff; }
.ico-venue { background: #fdf3ff; }
.ico-seat { background: #fff7ed; }
.ico-cat { background: #f0fdf4; }
.ico-end { background: #fef9c3; }
.ico-del { background: #eff6ff; }
.info-body { min-width: 0; }
.info-body span {
  display: block;
  color: #98a2b3;
  font-size: 0.66rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.info-body b {
  display: block;
  margin-top: 4px;
  color: #0f172a;
  font-size: 0.92rem;
  font-weight: 700;
  letter-spacing: -0.01em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ---------- Seller ---------- */
.seller-top { display: flex; align-items: center; gap: 16px; }
.seller-avatar {
  display: grid;
  place-items: center;
  width: 50px;
  height: 50px;
  flex: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #0f172a, #2563eb);
  color: #fff;
  font-weight: 800;
  font-size: 1.15rem;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.16);
}
.seller-info { min-width: 0; }
.seller-label { color: #98a2b3; font-size: 0.7rem; text-transform: uppercase; letter-spacing: 0.07em; font-weight: 700; }
.seller-name { display: inline-block; color: #0f172a; font-weight: 800; font-size: 1.05rem; letter-spacing: -0.02em; text-decoration: none; }
a.seller-name:hover { color: #2563eb; }
.seller-meta { margin-top: 4px; color: #64748b; font-size: 0.82rem; }
.seller-actions { margin-left: auto; display: flex; flex-direction: column; align-items: flex-end; gap: 8px; }
.trust { padding: 6px 12px; border-radius: 999px; background: #fffbeb; color: #b45309; font-weight: 700; font-size: 0.78rem; white-space: nowrap; }
.profile-link { color: #2563eb; font-size: 0.85rem; font-weight: 700; text-decoration: none; white-space: nowrap; }
.profile-link:hover { text-decoration: underline; }

/* "Message seller" — opens the floating buyer <-> seller chat window. */
.message-seller-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 16px;
  border: 1px solid rgba(37, 99, 235, 0.28);
  border-radius: 999px;
  background: linear-gradient(135deg, #1d4ed8, #3b82f6);
  color: #fff;
  font-family: inherit;
  font-size: 0.83rem;
  font-weight: 700;
  white-space: nowrap;
  cursor: pointer;
  box-shadow: 0 10px 22px rgba(37, 99, 235, 0.28), inset 0 1px 0 rgba(255, 255, 255, 0.28);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.message-seller-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 30px rgba(37, 99, 235, 0.36), inset 0 1px 0 rgba(255, 255, 255, 0.32);
}
.message-seller-btn:active { transform: translateY(0); }

/* ---------- Bid history ---------- */
.history-card .count { color: #98a2b3; font-weight: 700; margin-left: 6px; }
.history-list { display: flex; flex-direction: column; }
.history-row {
  display: grid;
  grid-template-columns: 40px 1fr auto 160px;
  gap: 12px;
  align-items: center;
  padding: 13px 6px;
  border-bottom: 1px solid #f1f5f9;
  border-radius: 10px;
  transition: background 0.15s ease;
}
.history-row:last-child { border-bottom: 0; }
.history-row:hover { background: #f8fafc; }
.history-row .rank {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 22px;
  border-radius: 7px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 0.72rem;
  font-weight: 700;
}
.history-row .bidder { display: flex; align-items: center; gap: 10px; color: #0f172a; font-weight: 600; min-width: 0; }
.history-row .bidder i {
  display: grid;
  place-items: center;
  width: 28px;
  height: 28px;
  flex: none;
  border-radius: 9px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 0.72rem;
  font-style: normal;
  font-weight: 800;
  text-transform: uppercase;
}
.history-row .amount { color: #2563eb; font-weight: 800; letter-spacing: -0.02em; }
.history-row .time { color: #98a2b3; font-size: 0.8rem; text-align: right; }

/* Current leader */
.history-row.leader {
  background: #eff6ff;
  padding: 14px 12px;
}
.history-row.leader .rank { background: #2563eb; color: #fff; }
.history-row.leader .bidder { color: #1d4ed8; font-weight: 800; }
.history-row.leader .bidder i { background: #2563eb; color: #fff; }

/* ---------- Sidebar ---------- */
.auction-side { position: sticky; top: 88px; display: flex; flex-direction: column; gap: 16px; }
.bid-panel {
  position: relative;
  overflow: hidden;
  padding: 20px 20px 22px;
  border-radius: 20px;
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 14px 34px rgba(15, 23, 42, 0.08);
}
.bid-panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #2563eb, #38bdf8 55%, #2563eb);
}
.panel-head { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.panel-label { color: #64748b; font-size: 0.73rem; font-weight: 800; text-transform: uppercase; letter-spacing: 0.09em; }
.live {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #059669;
  font-size: 0.68rem;
  font-weight: 800;
  letter-spacing: 0.09em;
  text-transform: uppercase;
}
.live i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #10b981;
  animation: live-pulse 1.6s ease-out infinite;
}
@keyframes live-pulse {
  0% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.45); }
  70% { box-shadow: 0 0 0 7px rgba(16, 185, 129, 0); }
  100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
}
.bid-amount {
  margin-top: 10px;
  color: #0f172a;
  font-size: 3rem;
  font-weight: 800;
  letter-spacing: -0.05em;
  line-height: 1.05;
}
/* flashes when a rival bid lands while the page is open */
.bid-amount.flash {
  animation: bidFlash 1.4s ease;
}
@keyframes bidFlash {
  0% { color: #0f172a; transform: scale(1); }
  18% { color: #e11d48; transform: scale(1.06); }
  100% { color: #0f172a; transform: scale(1); }
}

.panel-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
}
.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 11px;
  border-radius: 999px;
  background: #f1f5f9;
  color: #475467;
  font-size: 0.74rem;
  font-weight: 700;
}
.meta-chip.live-chip { background: #ecfdf5; color: #047857; }
.meta-chip.live-chip i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #10b981;
  animation: pulse 1.6s infinite;
}
.countdown {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  margin-top: 10px;
  padding: 7px 13px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-weight: 700;
  font-size: 0.85rem;
}
.countdown.ended { background: #f1f5f9; color: #64748b; }
.bid-actions {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid #eef2f6;
}
.next-min { margin: 0 0 10px; color: #98a2b3; font-size: 0.83rem; font-weight: 600; }
.next-min b { color: #0f172a; font-weight: 800; }
.bid-input { display: flex; flex-direction: column; gap: 10px; }
.bid-input :deep(.el-input-number) { width: 100%; }
.bid-input :deep(.el-input__wrapper) { min-height: 46px; border-radius: 12px; box-shadow: inset 0 0 0 1px #d9e1ec; }
.bid-input :deep(.el-input__wrapper.is-focus) { box-shadow: inset 0 0 0 1.5px #2563eb; }
.bid-input :deep(.el-input__inner) { font-weight: 700; color: #0f172a; }
.place-btn {
  width: 100%;
  height: 46px;
  margin-left: 0;
  border-radius: 12px;
  border: 0;
  background-image: linear-gradient(90deg, #2563eb, #3b82f6);
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.28);
  font-size: 0.95rem;
  font-weight: 700;
  letter-spacing: 0.02em;
  transition: transform 0.15s ease, box-shadow 0.15s ease, filter 0.15s ease;
}
.place-btn:hover { transform: translateY(-1px); filter: brightness(1.06); box-shadow: 0 12px 24px rgba(37, 99, 235, 0.34); }
.place-btn:active { transform: translateY(0); }
.side-note { margin-top: 16px; padding: 12px; border-radius: 12px; background: #f8fafc; color: #64748b; font-size: 0.85rem; text-align: center; }
.winner { margin-top: 16px; padding: 12px; border-radius: 12px; background: #fdf2f8; color: #be185d; font-weight: 700; font-size: 0.9rem; text-align: center; }

/* ---------- Guarantee ---------- */
.guarantee-card {
  position: relative;
  overflow: hidden;
  padding: 20px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 8px 20px rgba(15, 23, 42, 0.05);
}
.guarantee-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(180deg, #2563eb, #38bdf8);
}
.guarantee-head { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.guarantee-icon {
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  flex: none;
  border-radius: 9px;
  background: #eff6ff;
  font-size: 15px;
}
.guarantee-head h4 { margin: 0; font-size: 1rem; color: #0f172a; letter-spacing: -0.02em; }
.guarantee-card ul { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 12px; }
.guarantee-card li { display: flex; align-items: center; gap: 10px; color: #475467; font-size: 0.88rem; }
.guarantee-card li i { width: 22px; height: 22px; flex: none; border-radius: 50%; background: #ecfdf5; position: relative; }
.guarantee-card li i::after {
  content: '';
  position: absolute;
  left: 7px;
  top: 5px;
  width: 6px;
  height: 10px;
  border: solid #10b981;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

/* ---------- Seller close ---------- */
.seller-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 18px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid rgba(239, 68, 68, 0.2);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.05);
}
.seller-actions .el-button { border: 0; border-radius: 10px; font-weight: 700; }
.reveal-note { color: #6b7280; font-size: 0.82rem; }

/* ---------- Reveal ---------- */
.reveal-panel {
  padding: 20px;
  border-radius: 20px;
  border: 1px solid rgba(16, 185, 129, 0.4);
  background: linear-gradient(180deg, #ecfdf5 0%, #f0fdf4 100%);
}
.reveal-panel h3 { margin: 0 0 6px; }
.ticket-images { display: flex; gap: 10px; overflow-x: auto; margin-top: 12px; }
.ticket-images img { width: 160px; height: 110px; flex: 0 0 auto; object-fit: cover; border-radius: 12px; }

@media (max-width: 980px) {
  .auction-layout { grid-template-columns: 1fr; }
  .auction-side { position: static; }
  .info-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .gallery-hero { height: 260px; }
  .hero-caption { padding: 20px; }
}

@media (max-width: 640px) {
  .info-grid { grid-template-columns: minmax(0, 1fr); }
  .info-item:hover { transform: translateY(-2px); }
}

@media (max-width: 520px) {
  .info-grid { grid-template-columns: minmax(0, 1fr); }
  .gallery-hero { height: 240px; }
}

/* ---- Dark theme surfaces ---- */
:global(html.dark) .card {
  background: linear-gradient(180deg, var(--surface) 0%, var(--surface-2) 100%);
  border-color: var(--border-color);
}
:global(html.dark) .card h3 { color: var(--text-strong); }
:global(html.dark) .section-sub { color: var(--text-soft); }
:global(html.dark) .desc-text {
  background: linear-gradient(90deg, var(--surface-2), var(--surface-muted));
  color: var(--text-body);
}
:global(html.dark) .info-item {
  background: var(--surface-2);
  border-color: var(--border-color);
}
:global(html.dark) .info-item:hover { background: var(--surface-muted); }
:global(html.dark) .info-body span { color: var(--text-soft); }
:global(html.dark) .info-body b { color: var(--text-strong); }
:global(html.dark) .info-ico { background: rgba(148, 163, 184, 0.16) !important; }
:global(html.dark) .seller-label { color: var(--text-soft); }
:global(html.dark) .seller-name { color: var(--text-strong); }
:global(html.dark) .seller-meta { color: var(--text-muted); }
:global(html.dark) .trust { background: rgba(245, 158, 11, 0.18); color: #fcd34d; }
:global(html.dark) .profile-link,
:global(html.dark) .history-row .amount { color: #93c5fd; }
:global(html.dark) .history-card .count { color: var(--text-soft); }
:global(html.dark) .history-row { border-bottom-color: var(--divider); }
:global(html.dark) .history-row:hover { background: var(--surface-2); }
:global(html.dark) .history-row .rank { background: var(--surface-muted); color: var(--text-muted); }
:global(html.dark) .history-row .bidder { color: var(--text-strong); }
:global(html.dark) .history-row .bidder i { background: rgba(37, 99, 235, 0.22); color: #93c5fd; }
:global(html.dark) .history-row .time { color: var(--text-soft); }
:global(html.dark) .history-row.leader { background: rgba(37, 99, 235, 0.16); }
:global(html.dark) .history-row.leader .bidder { color: #93c5fd; }
:global(html.dark) .gallery-thumbs {
  background: var(--surface);
  border-top-color: var(--divider);
}
:global(html.dark) .gallery-thumbs button { background: var(--surface-2); }
:global(html.dark) .bid-panel { background: var(--surface); }
:global(html.dark) .panel-label { color: var(--text-muted); }
:global(html.dark) .bid-amount { color: var(--text-strong); }
:global(html.dark) .countdown { background: rgba(37, 99, 235, 0.2); color: #93c5fd; }
:global(html.dark) .countdown.ended { background: var(--surface-muted); color: var(--text-muted); }
:global(html.dark) .next-min { color: var(--text-soft); }
:global(html.dark) .next-min b { color: var(--text-strong); }
:global(html.dark .bid-input .el-input__wrapper) { box-shadow: inset 0 0 0 1px var(--border-color); }
:global(html.dark .bid-input .el-input__inner) { color: var(--text-strong); }
:global(html.dark) .side-note { color: var(--text-muted); }
:global(html.dark) .winner { color: var(--text-strong); }
:global(html.dark) .guarantee-card { background: var(--surface); border-color: var(--border-color); }
:global(html.dark) .guarantee-head h4 { color: var(--text-strong); }
:global(html.dark) .guarantee-card li { color: var(--text-body); }
:global(html.dark) .guarantee-icon { background: rgba(37, 99, 235, 0.2); }
:global(html.dark) .seller-actions { background: var(--surface); }
:global(html.dark) .reveal-note { color: var(--text-muted); }
:global(html.dark) .reveal-panel {
  background: linear-gradient(180deg, rgba(16, 185, 129, 0.16) 0%, rgba(16, 185, 129, 0.08) 100%);
}
:global(html.dark) .status-badge {
  background: rgba(15, 23, 42, 0.85);
}
:global(html.dark) .status-badge.live { color: #6ee7b7; }
:global(html.dark) .status-badge.off { color: var(--text-muted); }
</style>
