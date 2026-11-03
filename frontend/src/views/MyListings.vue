<template>
  <div class="my-listings-page">
    <!-- Hero header -->
    <header class="listings-hero">
      <div class="listings-hero-inner">
        <span class="section-kicker">{{ $t('Manage inventory') }}</span>
        <h1>{{ $t('My Listings') }}</h1>
        <p>{{ $t('Active auctions, sold tickets and expired listings in one place.') }}</p>
      </div>
    </header>

    <div class="listings-card">
      <el-tabs v-model="activeTab" class="listings-tabs" @tab-change="loadTickets">
        <el-tab-pane name="active">
          <template #label><span class="tab-label">{{ $t('Active') }}<b>{{ counts.active }}</b></span></template>
        </el-tab-pane>
        <el-tab-pane name="sold">
          <template #label><span class="tab-label">{{ $t('Sold') }}<b>{{ counts.sold }}</b></span></template>
        </el-tab-pane>
        <el-tab-pane name="expired">
          <template #label><span class="tab-label">{{ $t('Expired') }}<b>{{ counts.expired }}</b></span></template>
        </el-tab-pane>
      </el-tabs>

      <div class="summary-bar" v-if="tickets.length">
        <div class="summary-item">
          <span class="summary-dot"></span>
          <strong>{{ tickets.length }}</strong>
          {{ $t(tabLabel) }} {{ tickets.length === 1 ? $t('listing') : $t('listings') }}
        </div>
        <div class="summary-item">
          <span class="summary-dot is-blue"></span>
          {{ $t('Total value') }}
          <strong class="summary-total">{{ formatPrice(totalValue) }}</strong>
        </div>
      </div>

      <el-table :data="tickets" v-loading="loading" class="listings-table" row-key="id" @row-click="$router.push(`/auction/${$event.id}`)">
        <template #empty>
          <div class="empty-state">
            <div class="empty-icon">
              <span></span>
            </div>
            <strong>{{ $t(emptyLabel) }}</strong>
            <p>{{ $t('Listings you create will appear here. Start a sale to see them.') }}</p>
            <router-link to="/sell" class="empty-cta">{{ $t('Create a listing') }}</router-link>
          </div>
        </template>

        <el-table-column :label="$t('Event')" min-width="260">
          <template #default="{ row }">
            <div class="event-cell">
              <div class="thumb">
                <img v-if="imageUrl(row)" :src="imageUrl(row)" alt="" @error="$event.target.style.display = 'none'">
              </div>
              <div class="event-info">
                <strong>{{ row.eventName }}</strong>
                <small>{{ row.venue || $t('Live auction') }}</small>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="$t('Current Bid')" width="140">
          <template #default="{ row }">
            <span class="price">{{ formatPrice(row.auction?.currentBid ?? row.startPrice) }}</span>
          </template>
        </el-table-column>

        <el-table-column :label="$t('Status')" width="140">
          <template #default="{ row }">
            <span class="status-badge" :class="listingBadge(row).cls">
              <i></i>{{ $t(listingBadge(row).label) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column :label="$t('Action')" width="200">
          <template #default="{ row }">
            <div class="actions">
              <el-button size="small" class="act-btn" @click.stop="$router.push(`/auction/${row.id}`)">
                {{ $t('View') }}
              </el-button>
              <el-button size="small" type="danger" plain class="act-btn" @click.stop="removeTicket(row)">
                {{ $t('Remove') }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteTicket, getImageUrl, getMyTickets } from '@/api/tickets'
import { getAuctionByTicket } from '@/api/auctions'
import { t } from '@/i18n'

const activeTab = ref('active')
const tickets = ref([])
const loading = ref(false)
const counts = ref({ active: 0, sold: 0, expired: 0 })

const tabLabel = computed(() =>
  activeTab.value === 'active' ? 'Active' : activeTab.value === 'sold' ? 'Sold' : 'Expired'
)

const emptyLabel = computed(() => `No ${activeTab.value} listings`)

const totalValue = computed(() =>
  tickets.value.reduce((sum, t) => {
    const price = Number(t.auction?.currentBid ?? t.startPrice)
    return sum + (Number.isFinite(price) ? price : 0)
  }, 0)
)

function imageUrl(ticket) {
  return getImageUrl(ticket.imageUrls?.[0])
}

function getListingStatus(ticket) {
  if (ticket.status !== 'active') return 'Closed'
  if (ticket.auction?.status && ticket.auction.status !== 'ongoing') return 'Closed'
  if (ticket.auction?.endTime && new Date(ticket.auction.endTime) <= new Date()) return 'Expired'
  return 'Active'
}

const LISTING_BADGE = {
  Active:  { cls: 'is-active',    label: 'Active' },
  Sold:    { cls: 'is-sold',      label: 'Sold' },
  Expired: { cls: 'is-expired',   label: 'Expired' },
  Closed:  { cls: 'is-expired',   label: 'Closed' }
}

function listingBadge(ticket) {
  const label = getListingStatus(ticket)
  return LISTING_BADGE[label] || { cls: 'is-expired', label: label || 'Closed' }
}

function formatPrice(value) {
  const num = Number(value)
  return Number.isFinite(num) ? `$${num.toFixed(2)}` : '—'
}

async function removeTicket(ticket) {
  try {
    await ElMessageBox.confirm(t('Remove "{name}" from your listings?', { name: ticket.eventName }), t('Remove event'), {
      type: 'warning',
      confirmButtonText: t('Remove'),
      cancelButtonText: t('Cancel')
    })
    await deleteTicket(ticket.id)
    tickets.value = tickets.value.filter((item) => item.id !== ticket.id)
    ElMessage.success(t('Event removed'))
    refreshCounts()
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') ElMessage.error(err.message || t('Could not remove event'))
  }
}

async function refreshCounts() {
  try {
    const [active, sold, expired] = await Promise.all([
      getMyTickets('active'),
      getMyTickets('sold'),
      getMyTickets('expired')
    ])
    const size = (res) => {
      const list = Array.isArray(res?.data) ? res.data : (res?.data?.tickets || [])
      return list.length
    }
    counts.value = { active: size(active), sold: size(sold), expired: size(expired) }
  } catch {
    /* counts are decorative — keep whatever we have */
  }
}

async function loadTickets() {
  loading.value = true
  try {
    const res = await getMyTickets(activeTab.value)
    const ticketList = Array.isArray(res.data) ? res.data : (res.data.tickets || [])
    tickets.value = await Promise.all(ticketList.map(async (ticket) => {
      try {
        const auctionRes = await getAuctionByTicket(ticket.id)
        return { ...ticket, auction: auctionRes.data }
      } catch {
        return ticket
      }
    }))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTickets()
  refreshCounts()
})
</script>

<style scoped>
/* ---------- Hero header (3D panel) ---------- */
.listings-hero {
  position: relative;
  overflow: hidden;
  border-radius: 24px;
  padding: 42px 44px 46px;
  margin-bottom: 30px;
  color: #fff;
  /* Layered gradients fake a curved, glossy 3D slab:
     top specular -> rich blue body -> deeper edge */
  background:
    radial-gradient(120% 90% at 18% 8%, rgba(255, 255, 255, 0.4), transparent 42%),
    radial-gradient(90% 140% at 85% -10%, rgba(147, 197, 253, 0.55), transparent 50%),
    radial-gradient(80% 90% at 12% 110%, rgba(23, 37, 84, 0.55), transparent 65%),
    linear-gradient(135deg, #2f6bff 0%, #2563eb 40%, #1e40af 100%);
  border: 1px solid rgba(255, 255, 255, 0.35);
  box-shadow:
    inset 0 2px 2px rgba(255, 255, 255, 0.45),
    inset 0 -16px 30px rgba(15, 23, 42, 0.4),
    inset 0 0 0 1px rgba(255, 255, 255, 0.08),
    0 24px 50px rgba(37, 99, 235, 0.38),
    0 6px 14px rgba(15, 23, 42, 0.2);
}
/* glossy sweep that slowly drifts across the surface */
.listings-hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(105deg, transparent 20%, rgba(255, 255, 255, 0.28) 45%, transparent 70%);
  transform: translateX(-120%);
  animation: hero-sheen 6s ease-in-out infinite;
  pointer-events: none;
}
@keyframes hero-sheen {
  0% { transform: translateX(-120%); }
  55%, 100% { transform: translateX(120%); }
}
/* deep inner shadow at the bottom edge = panel thickness */
.listings-hero::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 34%;
  background: linear-gradient(180deg, transparent, rgba(15, 23, 42, 0.32));
  pointer-events: none;
}
.listings-hero-inner { position: relative; z-index: 2; }
.section-kicker {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 6px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.28);
  backdrop-filter: blur(4px);
  color: #fff;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.35), 0 4px 10px rgba(15, 23, 42, 0.18);
}
.section-kicker::before {
  content: '';
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #7ce38b;
  box-shadow: 0 0 0 3px rgba(124, 227, 139, 0.25);
}
.listings-hero h1 {
  margin: 14px 0 8px;
  font-size: 2.15rem;
  font-weight: 800;
  letter-spacing: -0.03em;
  text-shadow: 0 2px 8px rgba(15, 23, 42, 0.35);
}
.listings-hero p {
  margin: 0;
  max-width: 520px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 1rem;
  text-shadow: 0 1px 4px rgba(15, 23, 42, 0.3);
}

/* ---------- Card container ---------- */
.listings-card {
  border: 1px solid rgba(15, 23, 42, 0.07);
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 12px 32px rgba(15, 23, 42, 0.06);
  overflow: hidden;
  padding: 4px 22px 20px;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}
.listings-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 3px 8px rgba(15, 23, 42, 0.05), 0 24px 50px rgba(37, 99, 235, 0.14);
}

/* ---------- Tabs ---------- */
.listings-tabs :deep(.el-tabs__header) {
  margin: 0 0 8px;
}
.listings-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(37, 99, 235, 0.16), transparent);
}
.listings-tabs :deep(.el-tabs__item) {
  position: relative;
  height: 46px;
  font-size: 0.95rem;
  font-weight: 600;
  color: #667085;
  transition: color 0.2s ease, transform 0.2s ease;
}
.listings-tabs :deep(.el-tabs__item:hover) {
  color: #1d4ed8;
  transform: translateY(-1px);
}
.listings-tabs :deep(.el-tabs__item.is-active) {
  color: #1d4ed8;
  font-weight: 700;
}
.listings-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 999px;
  background: linear-gradient(90deg, #2563eb, #60a5fa);
  box-shadow: 0 2px 6px rgba(37, 99, 235, 0.3);
}
.tab-label { display: inline-flex; align-items: center; gap: 8px; }
.tab-label b {
  display: inline-grid;
  place-items: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.16);
  color: inherit;
  font-size: 0.72rem;
  font-weight: 700;
  transition: background 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
}
.is-active .tab-label b {
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  color: #fff;
  box-shadow: 0 4px 10px rgba(37, 99, 235, 0.3);
}
.listings-tabs :deep(.el-tabs__item:hover) .tab-label b {
  background: #e0e7ff;
  color: #1d4ed8;
}
.listings-tabs :deep(.el-tabs__item.is-active:hover) .tab-label b {
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  color: #fff;
}

/* ---------- Summary bar ---------- */
.summary-bar {
  display: flex;
  align-items: center;
  gap: 28px;
  margin: 2px 2px 14px;
  padding: 10px 16px;
  border: 1px solid rgba(37, 99, 235, 0.1);
  border-radius: 12px;
  background: linear-gradient(90deg, #f8faff, #f2f6ff);
}
.summary-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 0.84rem;
}
.summary-item strong:not(.summary-total) {
  color: #0f172a;
  font-size: 0.92rem;
}
.summary-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.15);
}
.summary-dot.is-blue { background: #2563eb; box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15); }
.summary-total { color: #1d4ed8; font-size: 1rem; }
.summary-item + .summary-item { border-left: 1px solid rgba(148, 163, 184, 0.3); padding-left: 28px; }

/* ---------- Table ---------- */
.listings-table {
  --el-table-border-color: #eef2f7;
  --el-table-header-bg-color: #f7f9fc;
  --el-table-header-text-color: #475467;
  --el-table-row-hover-bg-color: #f5f8ff;
  font-size: 0.92rem;
  width: 100%;
}
.listings-table :deep(.el-table__header th) {
  height: 50px;
  font-weight: 700;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #64748b;
}
.listings-table :deep(.el-table__row td) {
  padding: 13px 0;
}
.listings-table :deep(.el-table__row) { cursor: pointer; }
.listings-table :deep(.el-table__row) .price,
.listings-table :deep(.el-table__row) .status-badge {
  transition: transform 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
}
.listings-table :deep(.el-table__row:hover) .price {
  transform: scale(1.06);
  color: #1d4ed8;
}
.listings-table :deep(.el-table__row:hover) .status-badge {
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.08);
}

.event-cell { display: flex; align-items: center; gap: 14px; }
.thumb {
  width: 56px;
  height: 42px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, #eef2ff, #e0e7ff);
  box-shadow: 0 2px 6px rgba(15, 23, 42, 0.08);
}
.thumb img { width: 100%; height: 100%; object-fit: cover; display: block; }
.event-info { display: grid; gap: 4px; min-width: 0; }
.event-info strong {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #0f172a;
  font-size: 0.98rem;
  letter-spacing: -0.01em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.2s ease;
}
.event-info strong::after {
  content: '→';
  font-size: 0.85rem;
  font-weight: 700;
  color: #2563eb;
  opacity: 0;
  transform: translateX(-4px);
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.event-info small { color: #94a3b8; font-size: 0.8rem; }

/* row hover: tint title, reveal arrow, glow the thumbnail */
.listings-table :deep(.el-table__row:hover) .event-info strong { color: #1d4ed8; }
.listings-table :deep(.el-table__row:hover) .event-info strong::after {
  opacity: 1;
  transform: translateX(0);
}
.listings-table :deep(.el-table__row:hover) .thumb {
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.22);
}

.price {
  display: inline-block;
  color: #0f172a;
  font-weight: 700;
  font-size: 0.98rem;
  font-variant-numeric: tabular-nums;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 5px 12px;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 700;
  white-space: nowrap;
}
.status-badge i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 0 3px rgba(127, 127, 127, 0.12);
}
.status-badge.is-active   { color: #15803d; background: #dcfce7; }
.status-badge.is-sold     { color: #2563eb; background: #dbeafe; }
.status-badge.is-expired  { color: #64748b; background: #f1f5f9; }

.actions { display: flex; flex-wrap: wrap; gap: 8px; }
.act-btn {
  border-radius: 9px;
  font-weight: 600;
  transition: transform 0.18s ease, box-shadow 0.18s ease, filter 0.18s ease;
}
.act-btn:hover { transform: translateY(-1px); filter: brightness(1.05); }
.act-btn:active { transform: translateY(0); }

/* ---------- Empty state ---------- */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 52px 24px 56px;
  text-align: center;
}
.empty-icon {
  display: grid;
  place-items: center;
  width: 68px;
  height: 68px;
  margin-bottom: 12px;
  border-radius: 20px;
  background: linear-gradient(150deg, #eff6ff, #dbeafe);
  box-shadow:
    inset 0 2px 4px rgba(255, 255, 255, 0.8),
    inset 0 -6px 12px rgba(37, 99, 235, 0.12),
    0 10px 24px rgba(37, 99, 235, 0.14);
}
.empty-icon span {
  position: relative;
  width: 24px;
  height: 18px;
  border-radius: 4px;
  border: 2px solid #2563eb;
  background: #fff;
}
.empty-icon span::before,
.empty-icon span::after {
  content: '';
  position: absolute;
  top: 3px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  border: 2px solid #2563eb;
  background: #fff;
}
.empty-icon span::before { left: -8px; }
.empty-icon span::after { right: -8px; }
.empty-state strong {
  font-size: 1.08rem;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.01em;
}
.empty-state p {
  max-width: 360px;
  margin: 0;
  color: #667085;
  font-size: 0.9rem;
  line-height: 1.55;
}
.empty-cta {
  margin-top: 16px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 22px;
  border-radius: 999px;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  color: #fff;
  font-weight: 600;
  font-size: 0.9rem;
  text-decoration: none;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.3), 0 8px 18px rgba(37, 99, 235, 0.3);
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}
.empty-cta::after { content: '→'; font-size: 1rem; }
.empty-cta:hover {
  transform: translateY(-3px) scale(1.03);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.35), 0 14px 30px rgba(37, 99, 235, 0.46);
}
.empty-cta:active { transform: translateY(-1px) scale(0.99); }

/* ---- Dark theme surfaces ---- */
:global(html.dark) .listings-card {
  background: var(--surface);
  border-color: var(--border-color);
}
:global(html.dark .listings-tabs .el-tabs__item) { color: var(--text-muted); }
:global(html.dark .listings-tabs .el-tabs__item.is-active) { color: #93c5fd; }
:global(html.dark .listings-tabs .el-tabs__item:hover) { color: #93c5fd; }
:global(html.dark) .summary-bar {
  background: linear-gradient(90deg, var(--surface-2), var(--surface-muted));
  border-color: var(--border-soft);
}
:global(html.dark) .summary-item { color: var(--text-muted); }
:global(html.dark) .summary-item strong:not(.summary-total) { color: var(--text-strong); }
:global(html.dark) .summary-item + .summary-item { border-left-color: var(--border-soft); }
:global(html.dark) .listings-table {
  --el-table-border-color: var(--border-color);
  --el-table-header-bg-color: var(--surface-2);
  --el-table-header-text-color: var(--text-muted);
  --el-table-row-hover-bg-color: rgba(37, 99, 235, 0.12);
  --el-table-bg-color: var(--surface);
  --el-table-tr-bg-color: var(--surface);
  --el-table-text-color: var(--text-body);
}
:global(html.dark .listings-table .el-table__header th) { color: var(--text-muted); }
:global(html.dark) .event-info strong { color: var(--text-strong); }
:global(html.dark) .event-info small { color: var(--text-soft); }
:global(html.dark) .price { color: var(--text-strong); }
:global(html.dark) .thumb { background: linear-gradient(135deg, var(--surface-2), var(--surface-muted)); }
:global(html.dark) .status-badge.is-active { color: #86efac; background: rgba(34, 197, 94, 0.18); }
:global(html.dark) .status-badge.is-sold { color: #93c5fd; background: rgba(37, 99, 235, 0.2); }
:global(html.dark) .status-badge.is-expired { color: var(--text-muted); background: var(--surface-muted); }
:global(html.dark) .empty-icon {
  background: linear-gradient(150deg, var(--surface-muted), var(--surface-2));
}
:global(html.dark) .empty-icon span,
:global(html.dark) .empty-icon span::before,
:global(html.dark) .empty-icon span::after { background: var(--surface); }
:global(html.dark) .empty-state strong { color: var(--text-strong); }
:global(html.dark) .empty-state p { color: var(--text-muted); }
</style>
