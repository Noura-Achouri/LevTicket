<template>
  <div class="profile-page" v-loading="loading">
    <el-alert v-if="loadError" :title="loadError" type="error" show-icon :closable="false" />
    <template v-if="profile">
      <section class="profile-hero">
        <div class="identity-block">
          <button class="avatar-upload" type="button" :title="$t('Change profile picture')" @click="avatarInput?.click()">
            <span class="avatar-orb">
              <img
                v-if="!avatarFailed"
                :src="avatarSrc"
                :class="{ 'is-dragon': !profile.avatarUrl }"
                :alt="`${profile.username} — ${$t('profile picture')}`"
                @error="avatarFailed = true"
              >
              <span v-else class="avatar-initials">{{ initials }}</span>
            </span>
            <span class="avatar-edit" aria-hidden="true">+</span>
          </button>
          <input ref="avatarInput" class="avatar-input" type="file" accept="image/*" @change="handleAvatarUpload">
          <div class="identity-copy">
            <span class="eyebrow">{{ $t('Member profile') }}</span>
            <h1>{{ profile.username }}</h1>
            <p class="bio">{{ profile.bio || $t('Building a trusted ticket collection on LevTicket.') }}</p>
            <div class="identity-meta">
              <span class="meta-chip" :class="{ verified: isVerified }">
                <el-icon>
                  <CircleCheck v-if="isVerified" />
                  <Clock v-else />
                </el-icon>{{ $t(verificationLabel) }}
              </span>
              <span v-if="memberSince" class="meta-chip">
                <el-icon><Calendar /></el-icon>{{ $t('Member since {date}', { date: memberSince }) }}
              </span>
              <span v-if="reviewCount" class="meta-chip">
                <el-icon><Star /></el-icon>{{ $t('{rating} avg rating', { rating: averageRating }) }}
              </span>
              <button
                v-if="profile.avatarUrl"
                type="button"
                class="meta-chip remove-photo"
                :disabled="removingAvatar"
                @click="handleAvatarRemove"
              >
                <el-icon><Delete /></el-icon>{{ removingAvatar ? $t('Removing…') : $t('Remove photo') }}
              </button>
              <button type="button" class="meta-chip primary" @click="openEdit">
                <el-icon><EditPen /></el-icon>{{ $t('Edit profile') }}
              </button>
              <button type="button" class="meta-chip" @click="shareProfile">
                <el-icon><Share /></el-icon>{{ $t('Share profile') }}
              </button>
            </div>
          </div>
        </div>

        <div class="hero-aside">
          <div class="trust-block">
            <div class="trust-ring" :style="trustRingStyle"><strong>{{ trustScore }}</strong><span>/ 100</span></div>
            <div class="trust-copy">
              <span class="trust-label">{{ $t('Trust score') }}</span>
              <strong>{{ $t(trustLabel) }}</strong>
            </div>
          </div>
          <div class="hero-actions">
            <router-link to="/my-listings" class="ghost-btn">{{ $t('Manage listings') }}</router-link>
            <router-link to="/search" class="ghost-btn solid">{{ $t('Browse tickets') }}</router-link>
          </div>
        </div>
      </section>

      <section class="quick-actions" :aria-label="$t('Quick actions')">
        <router-link
          v-for="action in quickActions"
          :key="action.to"
          class="quick-action"
          :to="action.to"
        >
          <span class="qa-icon"><el-icon><component :is="action.icon" /></el-icon></span>
          <strong>{{ $t(action.label) }}</strong>
          <b v-if="action.badge" class="qa-badge" :class="{ alert: action.alert }">{{ action.badge }}</b>
        </router-link>
      </section>

      <section class="stat-grid" :aria-label="$t('Account activity')">
        <article class="stat-card buy">
          <span class="stat-icon buy"><el-icon><ShoppingCart /></el-icon></span>
          <div><span>{{ $t('Tickets bought') }}</span><strong>{{ activity.bought }}</strong></div>
        </article>
        <article class="stat-card sell">
          <span class="stat-icon sell"><el-icon><Sell /></el-icon></span>
          <div><span>{{ $t('Tickets sold') }}</span><strong>{{ activity.sold }}</strong></div>
        </article>
        <article class="stat-card listing">
          <span class="stat-icon listing"><el-icon><Tickets /></el-icon></span>
          <div><span>{{ $t('Active listings') }}</span><strong>{{ activity.listings }}</strong></div>
        </article>
        <article class="stat-card review">
          <span class="stat-icon review"><el-icon><Star /></el-icon></span>
          <div><span>{{ $t('Reviews received') }}</span><strong>{{ reviewCount }}</strong></div>
        </article>
        <article class="stat-card saved">
          <span class="stat-icon saved"><el-icon><Collection /></el-icon></span>
          <div><span>{{ $t('Saved') }}</span><strong>{{ savedCount }}</strong></div>
        </article>
        <article class="stat-card chat">
          <span class="stat-icon chat"><el-icon><ChatDotRound /></el-icon></span>
          <div><span>{{ $t('Unread messages') }}</span><strong>{{ unreadCount }}</strong></div>
        </article>
      </section>

      <section class="dashboard-grid">
        <article class="panel activity-panel">
          <div class="panel-heading"><div><span class="section-kicker">{{ $t('Marketplace activity') }}</span><h2>{{ $t('Buy & sell flow') }}</h2></div><span class="period-label">{{ $t('All time') }}</span></div>
          <div class="flow-chart" :aria-label="$t('Bought and sold ticket comparison')">
            <div class="chart-axis"><span v-for="tick in axisTicks" :key="tick">{{ tick }}</span></div>
            <div class="bars-area">
              <div class="bar-group"><div class="bar-value">{{ activity.bought }}</div><div class="bar bought" :class="{ empty: !activity.bought }" :style="{ height: barHeight(activity.bought) }"></div><span>{{ $t('Bought') }}</span></div>
              <div class="bar-group"><div class="bar-value">{{ activity.sold }}</div><div class="bar sold" :class="{ empty: !activity.sold }" :style="{ height: barHeight(activity.sold) }"></div><span>{{ $t('Sold') }}</span></div>
            </div>
          </div>
          <div class="chart-summary"><span><i class="legend-dot bought-dot"></i>{{ $t('Purchases') }}</span><span><i class="legend-dot sold-dot"></i>{{ $t('Sales') }}</span><strong>{{ $t('{count} total transactions', { count: totalActivity }) }}</strong></div>
        </article>

        <article class="panel trust-panel">
          <div class="panel-heading"><div><span class="section-kicker">{{ $t('Seller health') }}</span><h2>{{ $t('Performance') }}</h2></div></div>
          <div class="performance-row"><span>{{ $t('Successful auctions') }}</span><strong>{{ profile.trustScore?.successfulAuctions || 0 }}/{{ profile.trustScore?.totalAuctions || 0 }}</strong></div>
          <el-progress :percentage="auctionSuccessRate" :stroke-width="10" color="#2563eb" :show-text="false" />
          <p class="performance-copy">{{ $t('A clear history of completed trades helps buyers shop with confidence.') }}</p>
          <router-link to="/my-listings" class="panel-link">{{ $t('Manage listings') }} <span>→</span></router-link>
        </article>
      </section>

      <section class="dashboard-grid">
        <article class="panel completion-panel">
          <div class="panel-heading">
            <div><span class="section-kicker">{{ $t('Profile completion') }}</span><h2>{{ $t('{done} of {total} done', { done: completion.done, total: completion.total }) }}</h2></div>
            <span class="completion-pct">{{ completion.percent }}%</span>
          </div>
          <el-progress class="completion-bar" :percentage="completion.percent" :stroke-width="10" color="#2563eb" :show-text="false" />
          <ul class="checklist">
            <li v-for="item in checklist" :key="item.id" :class="{ done: item.done }">
              <span class="check-mark" aria-hidden="true">{{ item.done ? '✓' : '' }}</span>
              <span class="check-label">{{ $t(item.label) }}</span>
              <button v-if="!item.done" type="button" class="check-cta" @click="advance(item)">
                {{ $t('Next up') }} →
              </button>
            </li>
          </ul>
        </article>

        <article class="panel listings-panel">
          <div class="panel-heading">
            <div><span class="section-kicker">{{ $t('Seller activity') }}</span><h2>{{ $t('Recent listings') }}</h2></div>
            <router-link to="/my-listings" class="count-pill pill-link">{{ $t('My listings') }}</router-link>
          </div>
          <div v-if="recentListings.length" class="listing-list">
            <router-link
              v-for="ticket in recentListings"
              :key="ticket.id"
              class="listing-row"
              :to="`/auction/${ticket.id}`"
            >
              <span class="listing-thumb">
                <img v-if="thumbFor(ticket)" :src="thumbFor(ticket)" alt="" @error="$event.target.style.display = 'none'">
                <template v-else>🎟</template>
              </span>
              <span class="listing-copy">
                <strong>{{ ticket.eventName }}</strong>
                <small>
                  {{ formatEventDate(ticket.eventDate) }}<template v-if="ticket.eventLocation"> · {{ ticket.eventLocation }}</template>
                </small>
              </span>
              <b class="listing-price">{{ formatPrice(ticket.startPrice) }}</b>
            </router-link>
          </div>
          <el-empty v-else :description="$t('No active listings yet')" />
        </article>
      </section>

      <section class="panel bids-panel">
        <div class="panel-heading">
          <div><span class="section-kicker">{{ $t('Seller activity') }}</span><h2>{{ $t('Bids received') }}</h2></div>
          <strong class="bid-total">{{ $t('{count} total', { count: totalBids }) }}</strong>
        </div>
        <div v-if="bidStats.length" class="bid-chart" :aria-label="$t('Bids received by day')">
          <div v-for="item in bidStats" :key="item.bidDate" class="bid-day">
            <span class="bid-count">{{ item.bidCount }}</span>
            <div class="bid-bar" :style="{ height: bidHeight(item.bidCount) }"></div>
            <span>{{ formatShortDate(item.bidDate) }}</span>
          </div>
        </div>
        <el-empty v-else :description="$t('No bids received yet')" />
      </section>

      <section class="panel reviews-panel">
        <div class="panel-heading">
          <div><span class="section-kicker">{{ $t('Community feedback') }}</span><h2>{{ $t('Recent reviews') }}</h2></div>
          <span class="count-pill">{{ $t('{count} reviews', { count: reviewCount }) }}</span>
        </div>
        <div v-if="profile.reviews?.length" class="review-list">
          <article v-for="(review, index) in profile.reviews" :key="index" class="review">
            <div class="review-topline">
              <div>
                <span class="review-avatar">{{ (review.reviewerUsername || 'U').slice(0, 2).toUpperCase() }}</span>
                <div class="review-who">
                  <strong>{{ review.reviewerUsername }}</strong>
                  <el-rate :model-value="review.starRating" disabled />
                </div>
              </div>
              <span class="meta">{{ formatDate(review.createTime) }}</span>
            </div>
            <p>{{ review.comment || $t('Great transaction.') }}</p>
          </article>
        </div>
        <el-empty v-else :description="$t('No reviews yet')" />
      </section>
    </template>

    <el-dialog
      v-model="editOpen"
      :title="$t('Edit profile')"
      width="460px"
      append-to-body
      class="profile-edit-dialog"
    >
      <div class="edit-form">
        <label class="edit-field">
          <span>{{ $t('Phone') }}</span>
          <el-input v-model="form.phone" maxlength="32" :placeholder="$t('Add a phone number')" />
        </label>
        <label class="edit-field">
          <span>{{ $t('Bio') }}</span>
          <el-input
            v-model="form.bio"
            type="textarea"
            :rows="4"
            resize="none"
            maxlength="240"
            show-word-limit
            :placeholder="$t('Tell buyers who you are…')"
          />
        </label>
      </div>
      <template #footer>
        <el-button @click="editOpen = false">{{ $t('Cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="saveProfile">{{ $t('Save changes') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ShoppingCart, Sell, Tickets, Star, CircleCheck, Calendar, Clock, Delete,
  Collection, ChatDotRound, EditPen, Share, Plus, List
} from '@element-plus/icons-vue'
import { getMyProfile, getUserReviews, getBidStats, uploadAvatar, deleteAvatar, updateMyProfile } from '@/api/users'
import { getImageUrl } from '@/api/tickets'
import { getOrders } from '@/api/orders'
import { getMyTickets } from '@/api/tickets'
import { useAuthStore } from '@/stores/auth'
import { useWatchlistStore } from '@/stores/watchlist'
import { useChatStore } from '@/stores/chat'
import { t, locale } from '@/i18n'

const router = useRouter()
const auth = useAuthStore()
const watchlist = useWatchlistStore()
const chat = useChatStore()
const profile = ref(null)
const loading = ref(true)
const activity = ref({ bought: 0, sold: 0, listings: 0 })
const bidStats = ref([])
const avatarInput = ref(null)
const loadError = ref('')
const recentListings = ref([])
const editOpen = ref(false)
const saving = ref(false)
const form = ref({ bio: '', phone: '' })

// Dragon crest is the placeholder identity; an uploaded picture replaces it.
const DEFAULT_AVATAR = '/ai-dragon.png'
const avatarFailed = ref(false)
const removingAvatar = ref(false)

const initials = computed(() => (profile.value?.username || 'U').slice(0, 2).toUpperCase())
const avatarSrc = computed(() => (profile.value?.avatarUrl ? getImageUrl(profile.value.avatarUrl) : DEFAULT_AVATAR))
const trustScore = computed(() => Math.round(profile.value?.trustScore?.score || 0))
const trustLabel = computed(() => trustScore.value >= 80 ? 'Excellent standing' : trustScore.value >= 50 ? 'Good standing' : 'New seller')
const verificationLabel = computed(() => ({
  unverified: 'Unverified',
  email_verified: 'Email verified',
  phone_verified: 'Phone verified',
  fully_verified: 'Fully verified'
}[profile.value?.verificationStatus] || 'Unverified'))
const isVerified = computed(() => !!profile.value?.verificationStatus && profile.value.verificationStatus !== 'unverified')
const memberSince = computed(() => {
  const created = profile.value?.createTime
  if (!created) return ''
  return new Intl.DateTimeFormat(locale.value, { month: 'short', year: 'numeric' }).format(new Date(created))
})
const reviewCount = computed(() => profile.value?.reviews?.length || 0)
const averageRating = computed(() => {
  const reviews = profile.value?.reviews || []
  if (!reviews.length) return '—'
  const total = reviews.reduce((sum, review) => sum + Number(review.starRating || 0), 0)
  return (total / reviews.length).toFixed(1)
})
const totalActivity = computed(() => activity.value.bought + activity.value.sold)
const chartMax = computed(() => Math.max(activity.value.bought, activity.value.sold, 1))
// top / middle / baseline, without repeating a value when the max is 1 or 2
const axisTicks = computed(() => {
  const max = chartMax.value
  return [...new Set([max, Math.ceil(max / 2), 0])]
})
const auctionSuccessRate = computed(() => {
  const score = profile.value?.trustScore
  return score?.totalAuctions ? Math.round((score.successfulAuctions / score.totalAuctions) * 100) : 0
})
const totalBids = computed(() => bidStats.value.reduce((total, item) => total + Number(item.bidCount || 0), 0))
const bidMax = computed(() => Math.max(...bidStats.value.map((item) => Number(item.bidCount || 0)), 1))
const trustRingStyle = computed(() => ({ background: `conic-gradient(#2563eb ${trustScore.value}%, #e5edf8 0)` }))

// Counters owned by the global stores, so they stay in sync with the header.
const savedCount = computed(() => watchlist.count || 0)
const unreadCount = computed(() => chat.unreadCount || 0)

const quickActions = computed(() => [
  { to: '/post-ticket', label: 'Post a ticket', icon: Plus },
  { to: '/my-listings', label: 'My listings', icon: Tickets, badge: activity.value.listings },
  { to: '/orders', label: 'Orders', icon: List, badge: totalActivity.value },
  { to: '/saved', label: 'Saved', icon: Collection, badge: savedCount.value },
  { to: '/messages', label: 'Messages', icon: ChatDotRound, badge: unreadCount.value, alert: true }
])

/** What is still missing for a profile that buyers immediately trust. */
const checklist = computed(() => [
  { id: 'photo', label: 'Add a profile picture', done: !!profile.value?.avatarUrl, action: 'avatar' },
  { id: 'bio', label: 'Write a short bio', done: (profile.value?.bio || '').trim().length >= 12, action: 'edit' },
  { id: 'phone', label: 'Add a phone number', done: !!profile.value?.phone, action: 'edit' },
  { id: 'verified', label: 'Verify your email', done: isVerified.value },
  { id: 'listing', label: 'Post your first ticket', done: activity.value.listings > 0 || activity.value.sold > 0, to: '/post-ticket' },
  { id: 'review', label: 'Receive your first review', done: reviewCount.value > 0, to: '/my-listings' }
])

const completion = computed(() => {
  const total = checklist.value.length
  const done = checklist.value.filter((item) => item.done).length
  return { total, done, percent: total ? Math.round((done / total) * 100) : 0 }
})

function getCollection(data) {
  if (Array.isArray(data)) return data
  return data?.orders || data?.tickets || []
}

function barHeight(value) {
  return `${Math.max(value ? (value / chartMax.value) * 100 : 3, 3)}%`
}

function bidHeight(value) {
  return `${Math.max((Number(value || 0) / bidMax.value) * 100, 6)}%`
}

function formatShortDate(value) {
  return new Intl.DateTimeFormat(locale.value, { month: 'short', day: 'numeric' }).format(new Date(value))
}

function formatEventDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return new Intl.DateTimeFormat(locale.value, { month: 'short', day: 'numeric' }).format(date)
}

function formatPrice(value) {
  const num = Number(value)
  return Number.isFinite(num) ? `$${num.toFixed(2)}` : '—'
}

function thumbFor(ticket) {
  const first = Array.isArray(ticket?.imageUrls) ? ticket.imageUrls[0] : null
  return first ? getImageUrl(first) : ''
}

/* ---- profile details ---- */
function openEdit() {
  form.value = { bio: profile.value?.bio || '', phone: profile.value?.phone || '' }
  editOpen.value = true
}

async function saveProfile() {
  if (saving.value) return
  saving.value = true
  try {
    const res = await updateMyProfile(form.value.bio, form.value.phone)
    if (profile.value) {
      profile.value.bio = res.data?.bio ?? form.value.bio
      profile.value.phone = res.data?.phone ?? form.value.phone
    }
    editOpen.value = false
    ElMessage.success(t('Profile updated'))
  } catch (error) {
    ElMessage.error(error?.message || t('Could not update your profile'))
  } finally {
    saving.value = false
  }
}

async function shareProfile() {
  const url = `${window.location.origin}/profile/${profile.value?.id ?? ''}`
  if (navigator.share) {
    // The user dismissing the native sheet is not an error worth reporting.
    try {
      await navigator.share({ title: profile.value?.username || 'LevTicket', url })
      return
    } catch {
      return
    }
  }
  try {
    await navigator.clipboard.writeText(url)
    ElMessage.success(t('Link copied'))
  } catch {
    ElMessage.error(t('Could not copy link'))
  }
}

/** Sends the checklist row to whatever finishes it. */
function advance(item) {
  if (item.action === 'edit') openEdit()
  else if (item.action === 'avatar') avatarInput.value?.click()
  else if (item.to) router.push(item.to)
}

async function handleAvatarUpload(event) {
  const file = event.target.files?.[0]
  if (!file) return
  const formData = new FormData()
  formData.append('avatar', file)
  try {
    await uploadAvatar(formData)
    const [profileRes, reviewsRes] = await Promise.all([
      getMyProfile(),
      getUserReviews(auth.user.id)
    ])
    profile.value = profileRes.data
    profile.value.reviews = reviewsRes.data || []
    avatarFailed.value = false
    ElMessage.success(t('Profile picture updated'))
  } catch (error) {
    ElMessage.error(error?.message || t('Could not upload profile picture'))
  } finally {
    event.target.value = ''
  }
}

async function handleAvatarRemove() {
  if (!profile.value?.avatarUrl || removingAvatar.value) return
  removingAvatar.value = true
  try {
    await deleteAvatar()
    profile.value.avatarUrl = null
    avatarFailed.value = false
    ElMessage.success(t('Profile picture removed'))
  } catch (error) {
    ElMessage.error(error?.message || t('Could not remove profile picture'))
  } finally {
    removingAvatar.value = false
  }
}

function formatDate(value) {
  if (!value) return ''
  return new Intl.DateTimeFormat(locale.value, { month: 'short', day: 'numeric', year: 'numeric' }).format(new Date(value))
}

onMounted(async () => {
  // Header counter for the "Unread messages" card, in case we landed here directly.
  if (auth.token) chat.loadUnread()
  try {
    if (!auth.user?.id) throw new Error(t('Please sign in again to view your profile'))
    const profileRes = await getMyProfile()
    profile.value = profileRes.data
    const [boughtRes, soldRes, listingsRes, bidStatsRes] = await Promise.all([
      getOrders('bought'),
      getOrders('sold'),
      getMyTickets('active'),
      getBidStats()
    ])
    const myListings = getCollection(listingsRes.data)
    activity.value = {
      bought: getCollection(boughtRes.data).length,
      sold: getCollection(soldRes.data).length,
      listings: myListings.length
    }
    recentListings.value = myListings.slice(0, 3)
    bidStats.value = Array.isArray(bidStatsRes.data) ? bidStatsRes.data : []
  } catch (error) {
    loadError.value = error?.message || t('Could not load your profile')
    ElMessage.error(loadError.value)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* Shared surface language: hairline border + layered soft shadow, consistent radii */
.profile-hero,
.panel,
.stat-card {
  border: 1px solid rgba(15, 23, 42, 0.07);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 10px 30px rgba(15, 23, 42, 0.05);
}

/* ---- Hero banner ---- */
.profile-hero {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  padding: 34px 38px;
  border: 0;
  border-radius: 24px;
  background: linear-gradient(125deg, #0b1330 0%, #102a63 52%, #2563eb 100%);
  box-shadow: 0 16px 40px rgba(29, 78, 216, 0.22);
  color: #fff;
  overflow: hidden;
}

.profile-hero::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(520px 240px at 12% -20%, rgba(124, 58, 237, 0.45), transparent 65%),
    radial-gradient(560px 300px at 92% 130%, rgba(56, 189, 248, 0.35), transparent 60%);
}

.profile-hero > * { position: relative; }

.identity-block {
  display: flex;
  align-items: center;
  gap: 22px;
}

/* ---- Avatar: a 3D blue orb carrying the dragon crest by default, the
   uploaded photo once there is one. Same light source as the topbar brand.
   The button is only a frame (transparent, overflow visible) so the rim and
   the + badge can spill outside; the orb clips its own contents. ---- */
.avatar-upload {
  position: relative;
  /* optical nudge: centre the orb on the name, not on the whole text column */
  top: 14px;
  flex: none;
  width: 92px;
  height: 92px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: transparent;
  cursor: pointer;
  /* hairline white edge, soft halo, then the cast shadow beneath the orb */
  box-shadow:
    0 0 0 1px rgba(255, 255, 255, 0.55),
    0 0 0 8px rgba(255, 255, 255, 0.08),
    0 18px 30px rgba(2, 6, 23, 0.45),
    0 6px 14px rgba(37, 99, 235, 0.35);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.avatar-upload:hover {
  transform: translateY(-3px);
  box-shadow:
    0 0 0 1px rgba(255, 255, 255, 0.75),
    0 0 0 8px rgba(255, 255, 255, 0.14),
    0 24px 38px rgba(2, 6, 23, 0.5),
    0 8px 18px rgba(37, 99, 235, 0.45);
}

.avatar-upload:focus-visible {
  outline: 3px solid rgba(147, 197, 253, 0.9);
  outline-offset: 5px;
}

.avatar-orb {
  position: relative;
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: 50%;
  background:
    radial-gradient(circle at 30% 22%, rgba(255, 255, 255, 0.65), rgba(255, 255, 255, 0.16) 34%, transparent 52%),
    radial-gradient(circle at 70% 100%, rgba(59, 130, 246, 0.5), transparent 55%),
    linear-gradient(150deg, #60a5fa 0%, #2563eb 55%, #1e40af 100%);
  box-shadow:
    inset 0 2px 4px rgba(255, 255, 255, 0.4),
    inset 0 -9px 16px rgba(15, 23, 42, 0.35);
}

.avatar-orb img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

/* the crest floats above the orb: offset white edge, then its cast shadow */
.avatar-orb img.is-dragon {
  width: 86%;
  height: 86%;
  object-fit: contain;
  filter:
    drop-shadow(0 1px 0 rgba(255, 255, 255, 0.35))
    drop-shadow(0 3px 5px rgba(15, 23, 42, 0.55))
    drop-shadow(0 6px 12px rgba(30, 64, 175, 0.4));
}

.avatar-initials {
  color: #fff;
  font-size: 1.5rem;
  font-weight: 800;
  letter-spacing: 0.02em;
}

/* gloss sweeping across the orb, above the crest */
.avatar-orb::after {
  content: '';
  position: absolute;
  top: -60%;
  left: -140%;
  width: 55%;
  height: 220%;
  transform: rotate(22deg);
  pointer-events: none;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.55), transparent);
  animation: avatarSheen 6s ease-in-out infinite;
}

@keyframes avatarSheen {
  0%        { left: -140%; }
  45%, 100% { left: 170%; }
}

/* editable affordance, raised off the orb on the same light source */
.avatar-edit {
  position: absolute;
  right: -2px;
  bottom: 0;
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(180deg, #60a5fa 0%, #2563eb 55%, #1d4ed8 100%);
  border: 2px solid #fff;
  color: #fff;
  font-size: 1.05rem;
  font-weight: 700;
  line-height: 1;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.5),
    inset 0 -2px 5px rgba(2, 6, 23, 0.35),
    0 6px 14px rgba(2, 6, 23, 0.45);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.avatar-upload:hover .avatar-edit {
  transform: translateY(-1px) scale(1.08);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.55),
    inset 0 -2px 5px rgba(2, 6, 23, 0.35),
    0 9px 18px rgba(2, 6, 23, 0.5);
}

.avatar-input { display: none; }

.eyebrow,
.section-kicker {
  font-size: 0.7rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.eyebrow { color: rgba(191, 219, 254, 0.9); }
.section-kicker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #2563eb;
}
.section-kicker::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.profile-hero h1 {
  margin-top: 8px;
  font-size: clamp(1.9rem, 4vw, 2.9rem);
  letter-spacing: -0.05em;
  line-height: 1.1;
}

.identity-copy { min-width: 0; }

.profile-hero .bio {
  max-width: 560px;
  margin-top: 8px;
  color: rgba(255, 255, 255, 0.74);
  line-height: 1.55;
  text-wrap: pretty;
}

/* verification / tenure / rating chips */
.identity-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #e8eefb;
  font-size: 0.76rem;
  font-weight: 600;
  letter-spacing: 0.01em;
  white-space: nowrap;
  backdrop-filter: blur(6px);
}

.meta-chip .el-icon {
  font-size: 14px;
  color: rgba(191, 214, 250, 0.9);
}

.meta-chip.verified {
  background: rgba(16, 185, 129, 0.2);
  border-color: rgba(52, 211, 153, 0.5);
  color: #d1fae5;
}
.meta-chip.verified .el-icon { color: #6ee7b7; }

/* the one action we actually want people to press on this page */
.meta-chip.primary {
  border-color: #fff;
  background: #fff;
  color: #1d4ed8;
  font-weight: 800;
  box-shadow: 0 8px 18px rgba(2, 6, 23, 0.22);
}
.meta-chip.primary .el-icon { color: #1d4ed8; }
.meta-chip.primary:hover { background: #eaf1fb; border-color: #eaf1fb; transform: translateY(-1px); }

/* buttons reuse the chip look, so reset the UA defaults they bring along */
button.meta-chip {
  font-family: inherit;
  cursor: pointer;
}

/* quiet destructive action: reads as a chip until you hover it */
.remove-photo {
  background: rgba(248, 113, 113, 0.14);
  border-color: rgba(252, 165, 165, 0.4);
  color: #fee2e2;
  transition: background 0.2s ease, border-color 0.2s ease, transform 0.2s ease;
}
.remove-photo .el-icon { color: #fca5a5; }
.remove-photo:hover:not(:disabled) {
  background: rgba(248, 113, 113, 0.26);
  border-color: rgba(252, 165, 165, 0.65);
  transform: translateY(-1px);
}
.remove-photo:disabled { opacity: 0.6; cursor: default; }

/* trust card + quick actions on the right of the banner */
.hero-aside {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 12px;
  min-width: 288px;
}

.hero-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.ghost-btn {
  padding: 11px 14px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  font-size: 0.8rem;
  font-weight: 700;
  text-align: center;
  text-decoration: none;
  white-space: nowrap;
  transition: background 0.2s ease, transform 0.2s ease;
}

.ghost-btn:hover {
  background: rgba(255, 255, 255, 0.16);
  transform: translateY(-1px);
}

.ghost-btn.solid {
  background: #fff;
  border-color: #fff;
  color: #1d4ed8;
}

.ghost-btn.solid:hover { background: #eaf1fb; }

.trust-block {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(8px);
}

.trust-ring {
  position: relative;
  display: grid;
  place-items: center;
  flex: none;
  width: 84px;
  height: 84px;
  border-radius: 50%;
  box-shadow: 0 6px 18px rgba(2, 6, 23, 0.35);
}

.trust-ring::after {
  content: '';
  position: absolute;
  inset: 7px;
  border-radius: 50%;
  background: radial-gradient(circle at 50% 32%, #17336e 0%, #0e2150 70%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.14);
}

.trust-ring strong,
.trust-ring span { position: relative; z-index: 1; }
.trust-ring strong {
  color: #fff;
  font-size: 1.45rem;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}
.trust-ring span { color: rgba(255, 255, 255, 0.65); font-size: 0.7rem; }

.trust-copy { display: grid; gap: 4px; min-width: 0; }
.trust-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}
.trust-block strong { color: #fff; font-size: 0.95rem; }

/* ---- Stat cards ---- */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  overflow: hidden;
  border-radius: 20px;
  background: #ffffff;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

/* colour-coded hairline that ties each metric to its icon */
.stat-card::before {
  content: '';
  position: absolute;
  inset: 0 0 auto 0;
  height: 3px;
  background: var(--accent, #2563eb);
}

.stat-card.buy { --accent: linear-gradient(90deg, #2563eb, #60a5fa); }
.stat-card.sell { --accent: linear-gradient(90deg, #059669, #34d399); }
.stat-card.listing { --accent: linear-gradient(90deg, #b45309, #fbbf24); }
.stat-card.review { --accent: linear-gradient(90deg, #7c3aed, #a78bfa); }
.stat-card.saved { --accent: linear-gradient(90deg, #e11d48, #fb7185); }
.stat-card.chat { --accent: linear-gradient(90deg, #0891b2, #22d3ee); }

.stat-card:hover {
  transform: translateY(-3px);
  border-color: rgba(37, 99, 235, 0.18);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 18px 34px rgba(15, 23, 42, 0.08);
}

.stat-card div { display: grid; gap: 4px; min-width: 0; }
.stat-card span:not(.stat-icon) {
  color: #64748b;
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}
.stat-card strong {
  color: #0f172a;
  font-size: 1.75rem;
  letter-spacing: -0.03em;
  font-variant-numeric: tabular-nums;
}

.stat-icon {
  display: grid;
  place-items: center;
  flex: none;
  width: 46px;
  height: 46px;
  border-radius: 14px;
  font-size: 1.25rem;
  line-height: 1;
  transition: transform 0.2s ease;
}
.stat-card:hover .stat-icon { transform: scale(1.06) rotate(-4deg); }
.stat-icon.buy { background: #eff6ff; color: #2563eb; box-shadow: inset 0 0 0 1px rgba(37, 99, 235, 0.15); }
.stat-icon.sell { background: #ecfdf5; color: #047857; box-shadow: inset 0 0 0 1px rgba(4, 120, 87, 0.15); }
.stat-icon.listing { background: #fffbeb; color: #b45309; box-shadow: inset 0 0 0 1px rgba(180, 83, 9, 0.15); }
.stat-icon.review { background: #f5f3ff; color: #7c3aed; box-shadow: inset 0 0 0 1px rgba(124, 58, 237, 0.15); }
.stat-icon.saved { background: #fff1f2; color: #be123c; box-shadow: inset 0 0 0 1px rgba(190, 18, 60, 0.15); }
.stat-icon.chat { background: #ecfeff; color: #0e7490; box-shadow: inset 0 0 0 1px rgba(14, 116, 144, 0.15); }

/* ---- quick actions ---- */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.quick-action {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 9px;
  padding: 16px 10px;
  border: 1px solid rgba(15, 23, 42, 0.07);
  border-radius: 18px;
  background: #fff;
  color: #0f172a;
  font-size: 0.79rem;
  font-weight: 700;
  text-align: center;
  text-decoration: none;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 10px 30px rgba(15, 23, 42, 0.05);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}
.quick-action:hover {
  transform: translateY(-3px);
  border-color: rgba(37, 99, 235, 0.22);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 18px 34px rgba(15, 23, 42, 0.09);
}

.qa-icon {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 13px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 1.1rem;
  box-shadow: inset 0 0 0 1px rgba(37, 99, 235, 0.14);
  transition: transform 0.2s ease;
}
.quick-action:hover .qa-icon { transform: scale(1.07) rotate(-4deg); }

.qa-badge {
  position: absolute;
  top: 9px;
  right: 9px;
  min-width: 20px;
  padding: 2px 6px;
  border-radius: 999px;
  background: #eef2f6;
  color: #475467;
  font-size: 0.65rem;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}
.qa-badge.alert {
  background: linear-gradient(135deg, #f43f5e, #e11d48);
  color: #fff;
  box-shadow: 0 2px 6px rgba(225, 29, 72, 0.4);
}

/* ---- profile completion ---- */
.completion-pct {
  flex: none;
  padding: 6px 12px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 0.82rem;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  box-shadow: inset 0 0 0 1px rgba(37, 99, 235, 0.16);
}
.completion-bar { margin-top: 20px; }
.completion-panel :deep(.el-progress-bar__outer) { background: #eef2f6; }
.completion-panel :deep(.el-progress-bar__inner) { background: linear-gradient(90deg, #2563eb, #60a5fa); border-radius: 999px; }

.checklist { list-style: none; margin: 18px 0 0; padding: 0; display: grid; gap: 2px; }
.checklist li {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 11px 12px;
  border-radius: 12px;
  color: #475467;
  font-size: 0.87rem;
  transition: background 0.18s ease;
}
.checklist li:hover { background: #f8fafc; }

.check-mark {
  display: grid;
  place-items: center;
  flex: none;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 1.5px solid #d0d5dd;
  background: #fff;
  color: #fff;
  font-size: 0.7rem;
  font-weight: 800;
  transition: background 0.2s ease, border-color 0.2s ease;
}
.checklist li.done .check-mark { background: linear-gradient(135deg, #059669, #34d399); border-color: transparent; }
.checklist li.done .check-label { color: #98a2b3; text-decoration: line-through; }
.check-label { flex: 1; min-width: 0; }

.check-cta {
  flex: none;
  padding: 5px 11px;
  border: 1px solid rgba(37, 99, 235, 0.24);
  border-radius: 999px;
  background: #fff;
  color: #1d4ed8;
  font-family: inherit;
  font-size: 0.73rem;
  font-weight: 800;
  cursor: pointer;
  transition: background 0.18s ease, transform 0.18s ease;
}
.check-cta:hover { background: #eff6ff; transform: translateY(-1px); }

/* ---- recent listings ---- */
.pill-link { text-decoration: none; }
.pill-link:hover { background: #eaf1fb; }

.listing-list { margin-top: 14px; display: grid; gap: 6px; }
.listing-row {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 11px;
  border-radius: 14px;
  text-decoration: none;
  transition: background 0.18s ease, transform 0.18s ease;
}
.listing-row:hover { background: #f8fafc; transform: translateX(3px); }

.listing-thumb {
  display: grid;
  place-items: center;
  flex: none;
  width: 46px;
  height: 46px;
  overflow: hidden;
  border-radius: 12px;
  background: linear-gradient(140deg, #e0edff, #cfe0fb);
  font-size: 1.15rem;
}
.listing-thumb img { width: 100%; height: 100%; object-fit: cover; }

.listing-copy { display: grid; gap: 3px; min-width: 0; flex: 1; }
.listing-copy strong { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #0f172a; font-size: 0.9rem; }
.listing-copy small { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #667085; font-size: 0.76rem; }
.listing-price { flex: none; color: #1d4ed8; font-size: 0.9rem; font-weight: 800; font-variant-numeric: tabular-nums; }

/* ---- edit dialog ---- */
.edit-form { display: grid; gap: 16px; }
.edit-field { display: grid; gap: 7px; }
.edit-field > span { color: #475467; font-size: 0.8rem; font-weight: 700; }
.profile-edit-dialog :deep(.el-textarea__inner) { font-family: inherit; }

/* ---- Panels ---- */
.dashboard-grid {
  display: grid;
  grid-template-columns: 1.45fr 1fr;
  gap: 20px;
}

.panel {
  padding: 26px;
  border-radius: 24px;
  background: #ffffff;
}

.panel-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eef2f6;
}

.panel h2 {
  margin-top: 6px;
  color: #0f172a;
  font-size: 1.3rem;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.period-label,
.meta { color: #667085; font-size: 0.78rem; }

.period-label {
  flex: none;
  padding: 6px 12px;
  border-radius: 999px;
  background: #f4f7fc;
  color: #475467;
  font-weight: 600;
  white-space: nowrap;
}

/* activity chart */
.flow-chart {
  display: flex;
  height: 230px;
  margin-top: 24px;
  padding: 0 12px 0 0;
  border-bottom: 1px solid #e4e7ec;
}

.chart-axis {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 0 14px 22px 0;
  color: #98a2b3;
  font-size: 0.72rem;
}

.bars-area {
  flex: 1;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: clamp(28px, 10vw, 90px);
  background: repeating-linear-gradient(to bottom, transparent 0, transparent 32%, rgba(228, 231, 236, 0.6) 33%, transparent 34%);
}

.bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  height: 100%;
  gap: 8px;
  color: #475467;
  font-size: 0.8rem;
}

.bar {
  width: clamp(46px, 8vw, 78px);
  min-height: 6px;
  border-radius: 12px 12px 0 0;
  transition: height 0.45s ease;
  box-shadow: 0 -4px 14px rgba(37, 99, 235, 0.18);
}
.bar.bought { background: linear-gradient(180deg, #60a5fa, #2563eb); }
.bar.sold { background: linear-gradient(180deg, #34d399, #059669); box-shadow: 0 -4px 14px rgba(5, 150, 105, 0.18); }
/* nothing to show yet: neutral track instead of a coloured sliver */
.bar.empty { background: #e6ecf5; box-shadow: none; }

.bar-value {
  padding: 3px 10px;
  border-radius: 999px;
  background: #f4f7fc;
  color: #0f172a;
  font-size: 0.8rem;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}

.chart-summary {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 16px;
  color: #667085;
  font-size: 0.78rem;
}
.chart-summary strong { margin-left: auto; color: #344054; }
.legend-dot { display: inline-block; width: 9px; height: 9px; margin-right: 6px; border-radius: 50%; }
.bought-dot { background: #2563eb; }
.sold-dot { background: #059669; }

/* trust panel */
.trust-panel { display: flex; flex-direction: column; }
.performance-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 24px;
  padding-bottom: 12px;
  color: #475467;
  font-size: 0.9rem;
}
.performance-row strong {
  padding: 4px 12px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 0.9rem;
  box-shadow: inset 0 0 0 1px rgba(37, 99, 235, 0.16);
}
.trust-panel :deep(.el-progress) { margin-top: 14px; }
.trust-panel :deep(.el-progress-bar__outer) { background: #eef2f6; }
.trust-panel :deep(.el-progress-bar__inner) {
  border-radius: 999px;
  background: linear-gradient(90deg, #2563eb, #60a5fa);
}

.performance-copy {
  margin-top: 22px;
  color: #667085;
  font-size: 0.88rem;
  line-height: 1.6;
}

.panel-link {
  display: flex;
  justify-content: space-between;
  margin-top: auto;
  padding: 18px 16px;
  border-radius: 12px;
  background: #f4f7fc;
  color: #2563eb;
  font-size: 0.88rem;
  font-weight: 700;
  text-decoration: none;
  transition: background 0.2s ease;
}
.panel-link:hover { background: #eaf1fb; }
.panel-link span { transition: transform 0.2s ease; }
.panel-link:hover span { transform: translateX(5px); }

/* reviews */
.reviews-panel { padding-bottom: 8px; }
.review-list { margin-top: 16px; }

.count-pill {
  flex: none;
  padding: 6px 12px;
  border-radius: 999px;
  background: #f4f7fc;
  color: #475467;
  font-size: 0.78rem;
  font-weight: 700;
  white-space: nowrap;
}
.review {
  padding: 18px 16px;
  margin-top: 2px;
  border-top: 1px solid #eef2f6;
  border-radius: 14px;
  transition: background 0.2s ease;
}
.review:hover { background: #f8fafc; }
.review-topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.review-topline > div { display: flex; align-items: center; gap: 12px; min-width: 0; }
.review-topline strong { color: #0f172a; }
.review p { margin-top: 10px; color: #475467; line-height: 1.55; text-wrap: pretty; }

.review-avatar {
  display: grid;
  place-items: center;
  flex: none;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: linear-gradient(135deg, #1e3a8a, #2563eb);
  color: #fff;
  font-size: 0.78rem;
  font-weight: 800;
  letter-spacing: 0.02em;
}

.review-who { display: grid; gap: 3px; min-width: 0; }
.review :deep(.el-rate) { height: 14px; line-height: 14px; }
.review :deep(.el-rate__icon) { margin-right: 1px; font-size: 14px; }

/* bids */
.bid-total {
  flex: none;
  padding: 6px 12px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 0.82rem;
  font-weight: 700;
  white-space: nowrap;
  box-shadow: inset 0 0 0 1px rgba(37, 99, 235, 0.16);
}
/* keep empty panels from feeling oversized */
.panel :deep(.el-empty) { padding: 34px 0 18px; }
.panel :deep(.el-empty__description p) { color: #98a2b3; }
.panel :deep(.el-tag) {
  border: 0;
  border-radius: 999px;
  font-weight: 700;
}
.bid-chart {
  display: flex;
  align-items: flex-end;
  gap: clamp(10px, 3vw, 28px);
  min-height: 190px;
  margin-top: 24px;
  padding: 0 8px;
  border-bottom: 1px solid #e4e7ec;
  overflow-x: auto;
}
.bid-day {
  display: flex;
  flex: 1 0 48px;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  min-height: 170px;
  color: #667085;
  font-size: 0.72rem;
}
.bid-bar {
  width: min(46px, 8vw);
  min-height: 8px;
  border-radius: 10px 10px 0 0;
  background: linear-gradient(180deg, #fbbf24, #f59e0b);
}
.bid-count {
  padding: 2px 9px;
  border-radius: 999px;
  background: #fffbeb;
  color: #b45309;
  font-size: 0.74rem;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}
.bid-day > span:last-child {
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

@media (max-width: 820px) {
  .profile-hero, .trust-block { align-items: flex-start; }
  .profile-hero { flex-direction: column; }
  .hero-aside { width: 100%; min-width: 0; }
  .trust-block { width: 100%; }
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
  .dashboard-grid { grid-template-columns: 1fr; }
  .quick-actions { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}

@media (max-width: 520px) {
  .profile-hero { padding: 24px; }
  .identity-block { align-items: flex-start; }
  /* stacked layout: the optical nudge no longer applies */
  .avatar-upload { top: 0; width: 70px; height: 70px; }
  .avatar-edit { width: 25px; height: 25px; font-size: 0.9rem; }
  .avatar-initials { font-size: 1.15rem; }
  .stat-grid { grid-template-columns: 1fr 1fr; gap: 10px; }
  .quick-actions { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
  .quick-action { padding: 13px 8px; font-size: 0.74rem; }
  .qa-icon { width: 34px; height: 34px; font-size: 1rem; }
  .stat-card { padding: 14px; }
  .stat-icon { width: 38px; height: 38px; font-size: 1.05rem; }
  .stat-card strong { font-size: 1.3rem; }
  .panel { padding: 20px; }
  .hero-actions { flex-direction: column; }
  .chart-summary { flex-wrap: wrap; }
  .chart-summary strong { width: 100%; margin-left: 0; }
}

@media (prefers-reduced-motion: reduce) {
  .stat-card,
  .stat-card:hover,
  .stat-icon,
  .stat-card:hover .stat-icon,
  .avatar-upload,
  .avatar-upload:hover,
  .avatar-edit,
  .avatar-upload:hover .avatar-edit,
  .remove-photo,
  .remove-photo:hover:not(:disabled),
  .ghost-btn,
  .ghost-btn:hover,
  .panel-link span {
    transform: none;
    transition: none;
  }

  .avatar-orb::after { animation: none; }
}

/* ---- Dark theme surfaces ---- */
:global(html.dark) .stat-card {
  background: var(--surface);
  border-color: var(--border-color);
}
:global(html.dark) .stat-card strong { color: var(--text-strong); }
:global(html.dark) .stat-card span:not(.stat-icon) { color: var(--text-muted); }
:global(html.dark) .stat-icon.buy { background: rgba(59, 130, 246, 0.18); color: #93c5fd; }
:global(html.dark) .stat-icon.sell { background: rgba(16, 185, 129, 0.18); color: #6ee7b7; }
:global(html.dark) .stat-icon.listing { background: rgba(245, 158, 11, 0.18); color: #fcd34d; }
:global(html.dark) .stat-icon.review { background: rgba(139, 92, 246, 0.2); color: #c4b5fd; }
:global(html.dark) .panel { background: var(--surface); }
:global(html.dark) .panel-heading { border-bottom-color: var(--divider); }
:global(html.dark) .panel-heading h2 { color: var(--text-strong); }
:global(html.dark) .chart-axis span { color: var(--text-soft); }
:global(html.dark) .bar-group span { color: var(--text-muted); }
:global(html.dark) .bar-value { color: var(--text-strong); }
:global(html.dark) .chart-summary { color: var(--text-muted); }
:global(html.dark) .chart-summary strong { color: var(--text-body); }
:global(html.dark) .performance-row { border-bottom-color: var(--divider); }
:global(html.dark) .performance-row span { color: var(--text-muted); }
:global(html.dark) .performance-row strong { color: var(--text-strong); }
:global(html.dark) .performance-copy { color: var(--text-muted); }
:global(html.dark) .panel-link { background: var(--surface-muted); color: #93c5fd; }
:global(html.dark) .panel-link:hover { background: rgba(37, 99, 235, 0.22); }
:global(html.dark) .count-pill { background: var(--surface-muted); color: var(--text-body); }
:global(html.dark) .review { border-top-color: var(--divider); }
:global(html.dark) .review:hover { background: var(--surface-2); }
:global(html.dark) .review-topline strong { color: var(--text-strong); }
:global(html.dark) .review p { color: var(--text-body); }
:global(html.dark) .review .meta { color: var(--text-soft); }
:global(html.dark) .bid-total { background: rgba(37, 99, 235, 0.2); color: #93c5fd; }
:global(html.dark .panel .el-empty__description p) { color: var(--text-soft); }
:global(html.dark) .bid-chart { border-bottom-color: var(--border-color); }
:global(html.dark) .bid-day { color: var(--text-muted); }
:global(html.dark) .bid-count { background: rgba(245, 158, 11, 0.18); color: #fcd34d; }

/* dark: new blocks */
:global(html.dark) .stat-icon.saved { background: rgba(244, 63, 94, 0.18); color: #fda4af; }
:global(html.dark) .stat-icon.chat { background: rgba(34, 211, 238, 0.16); color: #67e8f9; }

:global(html.dark) .quick-action {
  background: var(--surface);
  border-color: var(--border-color);
  color: var(--text-strong);
}
:global(html.dark) .qa-icon { background: rgba(59, 130, 246, 0.18); color: #93c5fd; }
:global(html.dark) .qa-badge { background: var(--surface-muted); color: var(--text-body); }

:global(html.dark) .completion-pct { background: rgba(37, 99, 235, 0.2); color: #93c5fd; }
:global(html.dark) .completion-panel :deep(.el-progress-bar__outer) { background: var(--surface-muted); }
:global(html.dark) .checklist li { color: var(--text-body); }
:global(html.dark) .checklist li:hover { background: var(--surface-2); }
:global(html.dark) .check-mark { background: var(--surface-2); border-color: var(--border-color); }
:global(html.dark) .check-cta { background: var(--surface-2); border-color: rgba(59, 130, 246, 0.4); color: #93c5fd; }
:global(html.dark) .check-cta:hover { background: rgba(37, 99, 235, 0.2); }

:global(html.dark) .listing-row:hover { background: var(--surface-2); }
:global(html.dark) .listing-thumb { background: rgba(59, 130, 246, 0.2); }
:global(html.dark) .listing-copy strong { color: var(--text-strong); }
:global(html.dark) .listing-copy small { color: var(--text-muted); }
:global(html.dark) .listing-price { color: #93c5fd; }
:global(html.dark) .edit-field > span { color: var(--text-muted); }
</style>