<template>
  <div class="order-page">
    <!-- Hero header -->
    <header class="order-hero">
      <div class="order-hero-inner">
        <span class="section-kicker">{{ $t('Order history') }}</span>
        <h1>{{ $t('My Orders') }}</h1>
        <p>{{ $t("Keep track of every ticket you've bought and sold in one place.") }}</p>
      </div>
    </header>

    <div class="order-card">
      <el-tabs v-model="type" class="order-tabs" @tab-change="loadOrders">
        <el-tab-pane :label="$t('Bought')" name="bought" />
        <el-tab-pane :label="$t('Sold')" name="sold" />
      </el-tabs>

      <el-table
        :data="orders"
        class="order-table"
        v-loading="loading"
        :empty-text="$t('No orders yet')"
        row-key="id"
      >
        <template #empty>
          <div class="empty-state">
            <i class="deco deco-a"></i>
            <i class="deco deco-b"></i>
            <div class="empty-icon">
              <span></span>
            </div>
            <strong>{{ $t('No orders yet') }}</strong>
            <p>{{ $t('When you buy or sell a ticket, your orders will show up here.') }}</p>
            <router-link to="/search" class="empty-cta">{{ $t('Browse events') }}</router-link>
          </div>
        </template>

        <el-table-column :label="$t('Event')" min-width="220">
          <template #default="{ row }">
            <div class="event-cell">
              <strong>{{ row.ticketEventName || $t('Auction #{id}', { id: row.auctionId }) }}</strong>
              <small v-if="row.sellerUsername">
                {{ $t('Seller') }}:
                <router-link :to="`/profile/${row.sellerId}`">{{ row.sellerUsername }}</router-link>
              </small>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="$t('Final Price')" width="140">
          <template #default="{ row }">
            <span class="price">{{ formatPrice(row.finalPrice) }}</span>
          </template>
        </el-table-column>

        <el-table-column :label="$t('Status')" width="140">
          <template #default="{ row }">
            <span class="status-badge" :class="statusMeta(row.status).cls">
              <i></i>{{ $t(statusMeta(row.status).label) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column :label="$t('Date')" width="170">
          <template #default="{ row }">
            <span class="date">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column :label="$t('Action')" width="230">
          <template #default="{ row }">
            <div class="actions">
              <el-button
                v-if="type === 'bought' && (row.status === 'pending' || row.status === 'confirmed')"
                size="small"
                class="act-btn"
                @click="showTicket(row)"
              >
                {{ $t('View ticket') }}
              </el-button>
              <el-button
                v-if="type === 'bought' && row.status === 'pending'"
                size="small"
                type="success"
                class="act-btn"
                @click="confirmReceipt(row)"
              >
                {{ $t('Confirm ticket valid') }}
              </el-button>
              <el-button
                v-if="!row.reviewLeft && (row.status === 'confirmed' || row.status === 'completed')"
                size="small"
                type="primary"
                class="act-btn"
                @click="openReview(row)"
              >
                {{ $t('Leave Review') }}
              </el-button>
              <el-button
                size="small"
                class="act-btn chat-btn"
                :title="$t('Message')"
                @click="openChatWith(row)"
              >
                {{ $t('Message') }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <ReviewModal v-model="reviewModalOpen" :order="selectedOrder" @submitted="loadOrders" />

    <el-dialog v-model="ticketDialogOpen" :title="$t('Your ticket')" width="520px">
      <div v-if="ticketImages.length" class="ticket-images">
        <img v-for="image in ticketImages" :key="image" :src="getImageUrl(image)" :alt="$t('Purchased ticket')">
      </div>
      <el-empty v-else :description="$t('The seller has not uploaded the ticket photo yet')" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrders, updateOrderStatus } from '@/api/orders'
import { getTicketPhoto, getImageUrl } from '@/api/tickets'
import ReviewModal from '@/components/ReviewModal.vue'
import { ElMessage } from 'element-plus'
import { useChatStore } from '@/stores/chat'
import { t } from '@/i18n'

const chat = useChatStore()

const type = ref('bought')
const orders = ref([])
const loading = ref(false)
const reviewModalOpen = ref(false)
const selectedOrder = ref(null)
const ticketDialogOpen = ref(false)
const ticketImages = ref([])

const STATUS_META = {
  pending:   { label: 'Pending',   cls: 'is-pending' },
  confirmed: { label: 'Confirmed', cls: 'is-confirmed' },
  completed: { label: 'Completed', cls: 'is-completed' },
  disputed:  { label: 'Disputed',  cls: 'is-disputed' },
  cancelled: { label: 'Cancelled', cls: 'is-cancelled' },
  canceled:  { label: 'Cancelled', cls: 'is-cancelled' }
}

function statusMeta(status) {
  return STATUS_META[status] || { label: status || 'Unknown', cls: 'is-muted' }
}

/** Bought tab writes to the seller, sold tab to the buyer. */
function openChatWith(order) {
  const bought = type.value === 'bought'
  const partnerId = bought ? order.sellerId : order.buyerId
  if (!partnerId) return
  chat.openWith({
    id: partnerId,
    username: bought ? order.sellerUsername : '',
    ticketId: order.ticketId,
    ticketLabel: order.ticketEventName
  })
}

function formatPrice(value) {
  const num = Number(value)
  return Number.isFinite(num) ? `$${num.toFixed(2)}` : '—'
}

function formatDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })
}

async function loadOrders() {
  loading.value = true
  try {
    const res = await getOrders(type.value)
    orders.value = res.data
  } finally {
    loading.value = false
  }
}

function openReview(order) {
  selectedOrder.value = order
  reviewModalOpen.value = true
}

async function confirmReceipt(order) {
  try {
    await updateOrderStatus(order.id, 'confirmed')
    ElMessage.success(t('Ticket confirmed as valid. You can now review the seller.'))
    await loadOrders()
  } catch (error) {
    ElMessage.error(error?.message || t('Could not confirm receipt'))
  }
}

async function showTicket(order) {
  try {
    const response = await getTicketPhoto(order.ticketId)
    ticketImages.value = response.data?.imageUrls || []
    ticketDialogOpen.value = true
  } catch (error) {
    ElMessage.error(error?.message || t('Could not load the ticket'))
  }
}

onMounted(loadOrders)
</script>

<style scoped>
.order-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

/* ---------- Hero header (3D panel) ---------- */
.order-hero {
  position: relative;
  padding: 34px 38px;
  border-radius: 24px;
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
  color: #fff;
  overflow: hidden;
}
.order-hero::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(105deg, transparent 20%, rgba(255, 255, 255, 0.28) 45%, transparent 70%);
  transform: translateX(-120%);
  animation: hero-sheen 6s ease-in-out infinite;
}
@keyframes hero-sheen {
  0% { transform: translateX(-120%); }
  55%, 100% { transform: translateX(120%); }
}
.order-hero::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 34%;
  background: linear-gradient(180deg, transparent, rgba(15, 23, 42, 0.32));
  pointer-events: none;
}
.order-hero-inner { position: relative; z-index: 2; }
.section-kicker {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 6px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.28);
  backdrop-filter: blur(4px);
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #fff;
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
.order-hero h1 {
  margin-top: 10px;
  font-size: clamp(1.8rem, 3.5vw, 2.5rem);
  letter-spacing: -0.04em;
  line-height: 1.1;
  text-shadow: 0 2px 8px rgba(15, 23, 42, 0.35);
}
.order-hero p {
  margin-top: 10px;
  max-width: 560px;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.55;
  text-shadow: 0 1px 4px rgba(15, 23, 42, 0.3);
}

/* ---------- Card container ---------- */
.order-card {
  border: 1px solid rgba(15, 23, 42, 0.07);
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 12px 32px rgba(15, 23, 42, 0.06);
  overflow: hidden;
  padding: 4px 22px 20px;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}
.order-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 3px 8px rgba(15, 23, 42, 0.05), 0 24px 50px rgba(37, 99, 235, 0.14);
}

/* ---------- Tabs ---------- */
.order-tabs :deep(.el-tabs__header) {
  margin: 0 0 6px;
}
.order-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(37, 99, 235, 0.16), transparent);
}
.order-tabs :deep(.el-tabs__item) {
  position: relative;
  height: 46px;
  font-size: 0.95rem;
  font-weight: 600;
  color: #667085;
  transition: color 0.2s ease, transform 0.2s ease;
}
.order-tabs :deep(.el-tabs__item:hover) {
  color: #1d4ed8;
  transform: translateY(-1px);
}
.order-tabs :deep(.el-tabs__item.is-active) {
  color: #1d4ed8;
  font-weight: 700;
}
.order-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 999px;
  background: linear-gradient(90deg, #2563eb, #60a5fa);
  box-shadow: 0 2px 6px rgba(37, 99, 235, 0.3);
}

/* ---------- Table ---------- */
.order-table {
  --el-table-border-color: #eef2f7;
  --el-table-header-bg-color: #f7f9fc;
  --el-table-header-text-color: #475467;
  --el-table-row-hover-bg-color: #f5f8ff;
  font-size: 0.92rem;
  width: 100%;
}
.order-table :deep(.el-table__header th) {
  height: 50px;
  font-weight: 700;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #64748b;
}
.order-table :deep(.el-table__row td) {
  padding: 15px 0;
}

.event-cell { display: grid; gap: 5px; transition: transform 0.2s ease; }
.event-cell strong {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #0f172a;
  font-size: 0.98rem;
  letter-spacing: -0.01em;
  transition: color 0.2s ease;
}
.event-cell strong::after {
  content: '→';
  font-size: 0.85rem;
  font-weight: 700;
  color: #2563eb;
  opacity: 0;
  transform: translateX(-4px);
  transition: opacity 0.2s ease, transform 0.2s ease;
}
/* row hover: nudge event title, reveal arrow, tint links */
.order-table :deep(.el-table__row:hover) .event-cell strong { color: #1d4ed8; }
.order-table :deep(.el-table__row:hover) .event-cell strong::after {
  opacity: 1;
  transform: translateX(0);
}
.event-cell small { color: #667085; font-size: 0.82rem; }
.event-cell small a { color: #2563eb; text-decoration: none; font-weight: 600; }
.event-cell small a:hover { text-decoration: underline; }

.price { color: #0f172a; font-weight: 700; font-size: 0.98rem; }
.date { color: #667085; font-size: 0.88rem; }

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
.status-badge.is-pending   { color: #b45309; background: #fef3c7; }
.status-badge.is-confirmed { color: #0369a1; background: #e0f2fe; }
.status-badge.is-completed { color: #047857; background: #d1fae5; }
.status-badge.is-disputed  { color: #be185d; background: #fce7f3; }
.status-badge.is-cancelled { color: #64748b; background: #f1f5f9; }
.status-badge.is-muted     { color: #64748b; background: #f1f5f9; }

.actions { display: flex; flex-wrap: wrap; gap: 8px; }
.act-btn {
  border-radius: 9px;
  font-weight: 600;
  transition: transform 0.18s ease, box-shadow 0.18s ease, filter 0.18s ease;
}
.act-btn:hover { transform: translateY(-1px); filter: brightness(1.04); }
.act-btn:active { transform: translateY(0); }

/* ---------- Empty state ---------- */
.empty-state {
  --glow: rgba(37, 99, 235, 0.28);
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 56px 24px 60px;
  text-align: center;
  overflow: hidden;
  background:
    radial-gradient(60% 80% at 50% 0%, rgba(37, 99, 235, 0.06), transparent 60%),
    radial-gradient(40% 50% at 8% 90%, rgba(59, 130, 246, 0.05), transparent 60%),
    radial-gradient(40% 50% at 92% 90%, rgba(37, 99, 235, 0.05), transparent 60%);
}
.empty-state strong,
.empty-state p,
.empty-icon,
.empty-cta { position: relative; z-index: 1; }

/* floating soft blobs */
.deco {
  position: absolute;
  border-radius: 50%;
  filter: blur(2px);
  opacity: 0.9;
  pointer-events: none;
  z-index: 0;
}
.deco-a {
  width: 160px;
  height: 160px;
  top: 34px;
  left: 12%;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.14), transparent 65%);
  animation: floaty 7s ease-in-out infinite;
}
.deco-b {
  width: 200px;
  height: 200px;
  bottom: 8px;
  right: 10%;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.14), transparent 65%);
  animation: floaty 9s ease-in-out infinite reverse;
}
@keyframes floaty {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(14px, -16px) scale(1.06); }
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
  transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.35s ease;
}
.empty-icon span {
  position: relative;
  width: 26px;
  height: 18px;
  border-radius: 4px;
  border: 2px solid #2563eb;
  background: #fff;
  transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.empty-icon span::before,
.empty-icon span::after {
  content: '';
  position: absolute;
  top: 4px;
  width: 5px;
  height: 6px;
  border-radius: 2px;
  border: 2px solid #2563eb;
  background: #fff;
  transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.empty-icon span::before { left: -7px; }
.empty-icon span::after { right: -7px; }

/* hover the whole empty block: icon pops with a glow ring + ticket wiggles */
.empty-state:hover .empty-icon {
  transform: translateY(-4px) scale(1.06) rotate(-3deg);
  box-shadow:
    inset 0 2px 4px rgba(255, 255, 255, 0.85),
    inset 0 -6px 12px rgba(37, 99, 235, 0.14),
    0 0 0 6px rgba(37, 99, 235, 0.1),
    0 18px 38px rgba(37, 99, 235, 0.24);
}
.empty-state:hover .empty-icon span {
  transform: rotate(6deg) scale(1.08);
  animation: wiggle 0.5s ease;
}
@keyframes wiggle {
  0%, 100% { transform: rotate(0) scale(1); }
  30% { transform: rotate(8deg) scale(1.08); }
  60% { transform: rotate(-6deg) scale(1.02); }
}

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
  transition: transform 0.22s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.22s ease, filter 0.22s ease;
}
.empty-cta::after { content: '→'; font-size: 1rem; transition: transform 0.22s ease; }
.empty-cta:hover {
  transform: translateY(-3px) scale(1.03);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.35), 0 14px 30px rgba(37, 99, 235, 0.46);
  filter: saturate(1.08);
}
.empty-cta:hover::after { transform: translateX(4px); }
.empty-cta:active { transform: translateY(-1px) scale(0.99); }

/* ---------- Ticket images dialog ---------- */
.ticket-images { display: grid; gap: 14px; }
.ticket-images img {
  width: 100%;
  max-height: 420px;
  object-fit: contain;
  border-radius: 12px;
  background: #f2f4f7;
  border: 1px solid rgba(15, 23, 42, 0.06);
}

/* ---- Dark theme surfaces ---- */
:global(html.dark) .order-card {
  background: var(--surface);
  border-color: var(--border-color);
}
:global(html.dark .order-tabs .el-tabs__item) { color: var(--text-muted); }
:global(html.dark .order-tabs .el-tabs__item.is-active) { color: #93c5fd; }
:global(html.dark .order-tabs .el-tabs__item:hover) { color: #93c5fd; }
:global(html.dark) .order-table {
  --el-table-border-color: var(--border-color);
  --el-table-header-bg-color: var(--surface-2);
  --el-table-header-text-color: var(--text-muted);
  --el-table-row-hover-bg-color: rgba(37, 99, 235, 0.12);
  --el-table-bg-color: var(--surface);
  --el-table-tr-bg-color: var(--surface);
  --el-table-text-color: var(--text-body);
}
:global(html.dark .order-table .el-table__header th) { color: var(--text-muted); }
:global(html.dark) .event-cell strong { color: var(--text-strong); }
:global(html.dark) .event-cell small { color: var(--text-muted); }
:global(html.dark) .price { color: var(--text-strong); }
:global(html.dark) .date { color: var(--text-muted); }
:global(html.dark) .status-badge.is-pending { color: #fcd34d; background: rgba(245, 158, 11, 0.18); }
:global(html.dark) .status-badge.is-confirmed { color: #7dd3fc; background: rgba(14, 165, 233, 0.18); }
:global(html.dark) .status-badge.is-completed { color: #6ee7b7; background: rgba(16, 185, 129, 0.18); }
:global(html.dark) .status-badge.is-disputed { color: #f9a8d4; background: rgba(236, 72, 153, 0.18); }
:global(html.dark) .status-badge.is-cancelled,
:global(html.dark) .status-badge.is-muted { color: var(--text-muted); background: var(--surface-muted); }
:global(html.dark) .empty-icon { background: linear-gradient(150deg, var(--surface-muted), var(--surface-2)); }
:global(html.dark) .empty-icon span,
:global(html.dark) .empty-icon span::before,
:global(html.dark) .empty-icon span::after { background: var(--surface); }
:global(html.dark) .empty-state strong { color: var(--text-strong); }
:global(html.dark) .empty-state p { color: var(--text-muted); }
:global(html.dark) .ticket-images img {
  background: var(--surface-muted);
  border-color: var(--border-color);
}
</style>
