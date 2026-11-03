<template>
  <div class="post-ticket-page">
    <!-- 3D canvas sits behind the form; CSS gradient below stays as fallback -->
    <ThreeBackdrop class="sell-bg" />
    <el-card class="sell-card">
      <div class="sell-head">
        <span class="sell-eyebrow">{{ $t('Sell on LevTicket') }}</span>
        <h2>{{ $t('Post a Ticket') }}</h2>
        <p class="sell-subtitle">{{ $t('List your ticket in a few steps and reach thousands of buyers.') }}</p>
      </div>
      <el-form :model="form" label-position="top" @submit.prevent="handleSubmit">
        <el-form-item :label="$t('Event Name')">
          <el-input v-model="form.eventName" />
        </el-form-item>
        <el-form-item :label="$t('Event Date')">
          <el-date-picker v-model="form.eventDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
        </el-form-item>
        <el-form-item :label="$t('Event Location')">
          <div class="location-row">
            <el-select
              v-model="country"
              filterable
              clearable
              :placeholder="$t('Select country')"
              style="flex: 0 0 42%;"
            >
              <el-option v-for="c in countries" :key="c" :label="c" :value="c" />
            </el-select>
            <el-input v-model="city" :placeholder="$t('City / Venue')" style="flex: 1;" />
          </div>
        </el-form-item>
        <el-form-item :label="$t('Seat Info')">
          <el-input v-model="form.seatInfo" />
        </el-form-item>
        <el-form-item :label="$t('Description')">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item :label="$t('Category')">
          <el-select v-model="form.categoryId" style="width: 100%;" filterable clearable :placeholder="$t('Choose a category')">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('Tags (comma separated)')">
          <el-input v-model="form.tags" :placeholder="$t('VIP, last minute')" />
        </el-form-item>
        <div class="field-row">
          <el-form-item :label="$t('Start Price')" style="flex: 1;">
            <el-input-number v-model="form.startPrice" :min="0" style="width: 100%;" />
          </el-form-item>
          <el-form-item :label="$t('Duration (hours)')" style="flex: 1;">
            <el-input-number v-model="form.auctionDurationHours" :min="1" style="width: 100%;" />
          </el-form-item>
        </div>
        <el-form-item :label="$t('Photos')">
          <div class="media-row">
            <div class="media-cell">
              <el-upload
                v-model:file-list="ticketFile"
                action=""
                :auto-upload="false"
                list-type="picture-card"
                :limit="1"
                accept="image/*"
                class="sell-uploader ticket-uploader"
              >
                <el-icon><Plus /></el-icon>
                <span class="upload-caption">{{ $t('Ticket') }}</span>
              </el-upload>
              <span class="media-note">{{ $t('Photo of the actual ticket') }}</span>
            </div>

            <div class="media-cell">
              <el-upload
                v-model:file-list="bgFile"
                action=""
                :auto-upload="false"
                list-type="picture-card"
                :limit="1"
                accept="image/*"
                class="sell-uploader bg-uploader"
              >
                <el-icon><Plus /></el-icon>
                <span class="upload-caption">{{ $t('Background') }}</span>
              </el-upload>
              <span class="media-note">{{ $t('Cover picture shown on Home') }}</span>
            </div>
          </div>

          <el-button
            type="warning"
            plain
            style="margin-top: 6px; width: 100%;"
            :loading="extracting"
            :disabled="ticketFile.length === 0"
            @click="autoFillFromImage"
          >
            {{ $t('✨ Auto-fill with AI') }}
          </el-button>
          <p class="ai-hint">{{ $t('LevTicket AI reads the ticket photo and fills the form for you.') }}</p>
        </el-form-item>
        <el-button type="primary" native-type="submit" class="submit-btn" style="width: 100%;">{{ $t('Post Ticket') }}</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { createTicket } from '@/api/tickets'
import { getCategories } from '@/api/categories'
import { extractTicketFromImage } from '@/api/ai'
import { useAuthStore } from '@/stores/auth'
import ThreeBackdrop from '@/components/ThreeBackdrop.vue'
import { ElMessage, ElLoading } from 'element-plus'
import { t } from '@/i18n'

const router = useRouter()
const auth = useAuthStore()
const categories = ref([])
const ticketFile = ref([])
const bgFile = ref([])
const extracting = ref(false)

const countries = [
  'United States', 'Canada', 'United Kingdom', 'Ireland', 'Australia', 'New Zealand',
  'Germany', 'France', 'Spain', 'Italy', 'Portugal', 'Netherlands', 'Belgium', 'Switzerland',
  'Austria', 'Sweden', 'Norway', 'Denmark', 'Finland', 'Poland', 'Czech Republic', 'Greece',
  'Turkey', 'United Arab Emirates', 'Saudi Arabia', 'Qatar', 'Israel', 'Egypt', 'South Africa',
  'Nigeria', 'Kenya', 'Morocco', 'India', 'Pakistan', 'Bangladesh', 'Sri Lanka', 'Nepal',
  'Singapore', 'Malaysia', 'Indonesia', 'Thailand', 'Vietnam', 'Philippines', 'China',
  'Japan', 'South Korea', 'Hong Kong', 'Taiwan', 'Mexico', 'Brazil', 'Argentina', 'Chile',
  'Colombia', 'Peru', 'Russia', 'Ukraine', 'Hungary', 'Romania', 'Croatia', 'Serbia',
  'Bulgaria', 'Slovakia', 'Slovenia', 'Luxembourg', 'Malta', 'Estonia', 'Latvia', 'Lithuania'
]

const country = ref('')
const city = ref('')

const form = reactive({
  eventName: '',
  eventDate: '',
  eventLocation: '',
  seatInfo: '',
  description: '',
  categoryId: null,
  tags: '',
  startPrice: 0,
  auctionDurationHours: 48
})

onMounted(async () => {
  const res = await getCategories()
  categories.value = res.data
})

function parseDate(dateStr) {
  if (!dateStr) return ''
  const match = dateStr.match(/(\d{1,2})\/(\d{1,2})\/(\d{4})/)
  if (!match) return ''
  const [, d, m, y] = match
  return `${y}-${m.padStart(2, '0')}-${d.padStart(2, '0')}`
}

function parsePrice(priceStr) {
  if (priceStr == null) return null
  const num = Number(String(priceStr).replace(/[^\d.]/g, ''))
  return Number.isFinite(num) && num > 0 ? num : null
}

async function autoFillFromImage() {
  if (ticketFile.value.length === 0) {
    ElMessage.warning(t('Please upload a ticket photo first'))
    return
  }

  const file = ticketFile.value[0].raw
  if (!file) {
    ElMessage.warning(t('No image file found'))
    return
  }

  extracting.value = true
  const loading = ElLoading.service({
    lock: true,
    text: t('Reading ticket with AI...'),
    background: 'rgba(0, 0, 0, 0.7)'
  })

  try {
    const data = await extractTicketFromImage(file)

    if (!data || typeof data !== 'object') {
      throw new Error(t('Invalid response from AI server'))
    }

    form.eventName = data.eventName || form.eventName
    form.eventDate = parseDate(data.eventDate) || form.eventDate
    city.value = data.venue || city.value
    form.seatInfo = data.seatNumber || form.seatInfo

    const suggested = parsePrice(data.suggestedSellPrice)
    if (suggested != null) {
      form.startPrice = suggested
    }

    const extraLines = []
    if (data.ticketType) extraLines.push(t('Ticket type: {value}', { value: data.ticketType }))
    if (data.originalPrice) extraLines.push(t('Original price: {value}', { value: `$${data.originalPrice}` }))
    if (data.suggestedSellPrice && suggested == null) {
      extraLines.push(t('Suggested sell price: {value}', { value: data.suggestedSellPrice }))
    }
    if (data.listingTips) extraLines.push(t('Tips: {value}', { value: data.listingTips }))

    if (extraLines.length > 0) {
      form.description = extraLines.join('\n') + (form.description ? '\n\n' + form.description : '')
    }

    ElMessage.success(t('Form auto-filled from ticket image!'))
  } catch (err) {
    console.error('AI extract error:', err)
    const msg = err?.message || err?.data?.message || t('Failed to read ticket image. Please fill manually.')
    ElMessage.error(String(msg))
  } finally {
    extracting.value = false
    loading.close()
  }
}

async function handleSubmit() {
  if (!auth.token) {
    ElMessage.warning(t('Please login first to post a ticket.'))
    router.push('/login')
    return
  }

  form.eventLocation = [city.value, country.value].filter(Boolean).join(', ')

  const formData = new FormData()
  Object.entries(form).forEach(([key, value]) => formData.append(key, value))
  // Public cover shown in listings (Home card background)
  if (bgFile.value.length > 0) formData.append('images', bgFile.value[0].raw)
  // Private ticket photo - hidden until the seller closes the auction
  if (ticketFile.value.length > 0) formData.append('ticketImages', ticketFile.value[0].raw)

  try {
    const res = await createTicket(formData)
    if (res.code === 401 || res.message === 'Unauthorized') {
      ElMessage.warning(t('Please login first to post a ticket.'))
      router.push('/login')
      return
    }
    if (res.code !== 200 && res.code !== 0) {
      throw new Error(res.message || t('Failed to post ticket'))
    }
    ElMessage.success(t('Ticket posted!'))
    router.push(`/auction/${res.data.ticketId}`)
  } catch (err) {
    const msg = err?.message || err?.data?.message || t('Failed to post ticket')
    ElMessage.error(msg)
  }
}
</script>

<style scoped>
.post-ticket-page {
  position: relative;
  /* own stacking context so the canvas / glows / card layer predictably */
  isolation: isolate;
  display: flex;
  justify-content: center;
  min-height: calc(100vh - 88px);
  padding: 32px 16px 48px;
  overflow: hidden;
  background:
    radial-gradient(1000px 520px at 82% -10%, rgba(37, 99, 235, 0.38), transparent 60%),
    radial-gradient(900px 600px at -8% 108%, rgba(124, 58, 237, 0.3), transparent 55%),
    radial-gradient(700px 500px at 50% 120%, rgba(6, 182, 212, 0.12), transparent 60%),
    linear-gradient(165deg, #0a0f26 0%, #0d1736 46%, #171040 100%);
}

/* the Three.js canvas, painted above the gradient but below the glows */
.sell-bg {
  z-index: 0;
}

/* soft glow spots layered over the 3D scene, still behind the card */
.post-ticket-page::before,
.post-ticket-page::after {
  content: '';
  position: absolute;
  z-index: 1;
  border-radius: 50%;
  filter: blur(90px);
  pointer-events: none;
}

.post-ticket-page::before {
  width: 360px;
  height: 360px;
  background: rgba(59, 130, 246, 0.4);
  top: -120px;
  left: 6%;
}

.post-ticket-page::after {
  width: 420px;
  height: 420px;
  background: rgba(139, 92, 246, 0.28);
  bottom: -170px;
  right: 2%;
}

.sell-card {
  width: min(100%, 600px);
  margin: 0 auto;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-top: 3px solid #3b82f6;
  background: #ffffff;
  box-shadow: 0 28px 70px rgba(2, 6, 23, 0.55);
  position: relative;
  z-index: 2;
  animation: cardEnter 0.5s ease both;
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.sell-head {
  text-align: center;
  margin: 4px 0 24px;
}

.sell-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: 0.68rem;
  font-weight: 800;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #2563eb;
}

.sell-eyebrow::before,
.sell-eyebrow::after {
  content: '';
  width: 20px;
  height: 1.5px;
  background: linear-gradient(90deg, transparent, #2563eb);
}

.sell-eyebrow::after {
  background: linear-gradient(90deg, #2563eb, transparent);
}

.sell-card h2 {
  margin: 4px 0 6px;
  font-size: 1.8rem;
  font-weight: 800;
  letter-spacing: -0.015em;
  color: #0b1220;
}

.sell-subtitle {
  margin: 0;
  color: #64748B;
  font-size: 0.92rem;
}

.field-row {
  display: flex;
  gap: 14px;
}

@media (max-width: 560px) {
  .field-row {
    flex-direction: column;
    gap: 0;
  }
}

.location-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

@media (max-width: 480px) {
  .location-row {
    flex-direction: column;
  }
  .location-row :deep(.el-select) {
    width: 100% !important;
  }
}

.media-row {
  display: flex;
  gap: 18px;
  flex-wrap: wrap;
}

.media-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
}

.upload-caption {
  font-size: 0.76rem;
  font-weight: 700;
  color: #475467;
}

.media-note,
.ai-hint {
  font-size: 0.74rem;
  color: #8a94a6;
}

.ai-hint {
  margin: 6px 0 0;
}

.sell-uploader :deep(.el-upload--picture-card) {
  display: inline-flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 6px;
  border-radius: 14px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.sell-uploader :deep(.el-upload--picture-card:hover) {
  border-color: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.15);
}

.sell-uploader :deep(.el-upload--picture-card .el-icon) {
  font-size: 22px;
  color: #2563eb;
}

.sell-uploader :deep(.el-upload-list--picture-card .el-upload-list__item) {
  border-radius: 14px;
}

.ticket-uploader :deep(.el-upload--picture-card),
.ticket-uploader :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 150px;
  height: 150px;
}

.bg-uploader :deep(.el-upload--picture-card),
.bg-uploader :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 248px;
  height: 132px;
}

/* ---------- Form typography & field styling ---------- */
:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-form-item__label) {
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: #24314f;
  line-height: 1.4;
  padding-bottom: 8px;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  min-height: 44px;
  padding: 1px 14px;
  border-radius: 10px;
  background: #fbfcfe;
  box-shadow: 0 0 0 1px #d8dee9 inset;
  transition: box-shadow 0.2s ease, background 0.2s ease;
}

:deep(.el-textarea__inner) {
  border-radius: 10px;
  padding: 11px 14px;
  font-size: 0.95rem;
  line-height: 1.5;
  color: #0f172a;
  background: #fbfcfe;
  box-shadow: 0 0 0 1px #d8dee9 inset;
  transition: box-shadow 0.2s ease, background 0.2s ease;
}

:deep(.el-input__inner) {
  font-size: 0.95rem;
  font-weight: 500;
  color: #0f172a;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover),
:deep(.el-textarea__inner:hover) {
  background: #ffffff;
  box-shadow: 0 0 0 1px #9db6d8 inset;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focused),
:deep(.el-textarea__inner:focus) {
  background: #ffffff;
  box-shadow: 0 0 0 1px #2563eb inset, 0 0 0 4px rgba(37, 99, 235, 0.14);
}

:deep(.el-input__inner::placeholder),
:deep(.el-textarea__inner::placeholder) {
  font-weight: 400;
  color: #9aa7bd;
}

/* Submit button micro-interaction */
.submit-btn {
  height: 46px;
  margin-top: 6px;
  border: 0;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 700;
  letter-spacing: 0.02em;
  background-image: linear-gradient(90deg, #2563eb 0%, #3b82f6 100%);
  transition: transform 0.15s ease, box-shadow 0.15s ease, filter 0.15s ease;
}

.submit-btn:hover {
  transform: translateY(-1px);
  filter: brightness(1.05);
  box-shadow: 0 10px 22px rgba(37, 99, 235, 0.35);
}

.submit-btn:active {
  transform: translateY(0);
}

/* ---- Dark theme surfaces ---- */
:global(html.dark) .sell-card {
  background: var(--surface);
  border-color: var(--border-color);
}
:global(html.dark) .sell-card h2 { color: var(--text-strong); }
:global(html.dark) .sell-subtitle { color: var(--text-muted); }
:global(html.dark) .upload-caption { color: var(--text-body); }
:global(html.dark) .media-note,
:global(html.dark) .ai-hint { color: var(--text-soft); }
:global(html.dark .sell-card .el-form-item__label) { color: var(--text-body); }
:global(html.dark .sell-card .el-input__wrapper),
:global(html.dark .sell-card .el-select__wrapper) {
  background: var(--surface-2);
  box-shadow: 0 0 0 1px var(--border-color) inset;
}
:global(html.dark .sell-card .el-textarea__inner) {
  background: var(--surface-2);
  color: var(--text-strong);
  box-shadow: 0 0 0 1px var(--border-color) inset;
}
:global(html.dark .sell-card .el-input__inner) { color: var(--text-strong); }
:global(html.dark .sell-card .el-input__wrapper:hover),
:global(html.dark .sell-card .el-select__wrapper:hover),
:global(html.dark .sell-card .el-textarea__inner:hover) {
  background: var(--surface-muted);
  box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.5) inset;
}
:global(html.dark .sell-card .el-input__wrapper.is-focus),
:global(html.dark .sell-card .el-select__wrapper.is-focused),
:global(html.dark .sell-card .el-textarea__inner:focus) {
  background: var(--surface);
  box-shadow: 0 0 0 1px #2563eb inset, 0 0 0 4px rgba(37, 99, 235, 0.22);
}
</style>