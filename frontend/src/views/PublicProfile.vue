<template>
  <div class="public-profile" v-loading="loading">
    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />
    <template v-if="profile">
      <section class="public-hero">
        <div class="identity">
          <img v-if="profile.avatarUrl" :src="getImageUrl(profile.avatarUrl)" :alt="`${profile.username} profile picture`" class="avatar">
          <!-- no picture yet: fall back to the LevTicket dragon crest -->
          <div v-else class="avatar default-avatar">
            <img src="/ai-dragon.png" alt="Default profile picture">
          </div>
          <div>
            <span class="eyebrow">{{ $t('LevTicket member') }}</span>
            <h1>{{ profile.username }}</h1>
            <p>{{ profile.bio || $t('Ticket buyer and seller on LevTicket.') }}</p>
          </div>
        </div>
        <div class="trust-score">
          <strong>{{ trustScore }}</strong>
          <span>{{ $t('Trust score') }}</span>
        </div>
      </section>

      <section class="summary-row">
        <div><strong>{{ profile.reviews?.length || 0 }}</strong><span>{{ $t('Reviews') }}</span></div>
        <div><strong>{{ profile.trustScore?.successfulAuctions || 0 }}</strong><span>{{ $t('Successful sales') }}</span></div>
        <div><strong>{{ profile.trustScore?.totalAuctions || 0 }}</strong><span>{{ $t('Completed auctions') }}</span></div>
      </section>

      <section class="panel">
        <div class="section-heading"><div><span class="kicker">{{ $t('Community feedback') }}</span><h2>{{ $t('Reviews from buyers') }}</h2></div></div>
        <div v-if="profile.reviews?.length" class="review-list">
          <article v-for="review in profile.reviews" :key="review.id" class="review">
            <div class="review-top"><strong>{{ review.reviewerUsername || $t('Buyer') }}</strong><el-rate :model-value="review.starRating" disabled /></div>
            <p>{{ review.comment || $t('No written comment.') }}</p>
            <span class="date">{{ formatDate(review.createTime) }}</span>
          </article>
        </div>
        <el-empty v-else :description="$t('No reviews yet')" />
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getUserProfile, getUserReviews } from '@/api/users'
import { getImageUrl } from '@/api/tickets'
import { t, locale } from '@/i18n'

const route = useRoute()
const profile = ref(null)
const loading = ref(true)
const error = ref('')
const trustScore = computed(() => Math.round(profile.value?.trustScore?.score || 0))

function formatDate(value) {
  if (!value) return ''
  return new Intl.DateTimeFormat(locale.value, { month: 'short', day: 'numeric', year: 'numeric' }).format(new Date(value))
}

onMounted(async () => {
  try {
    const [response, reviewsResponse] = await Promise.all([
      getUserProfile(route.params.id),
      getUserReviews(route.params.id)
    ])
    const profileData = response.data ?? response
    const fetchedReviews = reviewsResponse.data ?? reviewsResponse
    profile.value = {
      ...profileData,
      reviews: fetchedReviews.length ? fetchedReviews : profileData.reviews || []
    }
  } catch (requestError) {
    error.value = requestError?.message || t('Could not load this profile')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.public-profile { display: grid; gap: 22px; max-width: 920px; margin: 0 auto; }
.public-hero, .panel, .summary-row { border: 1px solid rgba(148,163,184,.24); box-shadow: var(--shadow-soft); }
.public-hero { display: flex; justify-content: space-between; align-items: center; gap: 24px; padding: 34px; border-radius: 26px; background: linear-gradient(120deg, #0f172a, #1d4ed8); color: #fff; }
.identity { display: flex; align-items: center; gap: 20px; }.avatar { width: 86px; height: 86px; border-radius: 25px; object-fit: cover; }.default-avatar { display: grid; place-items: center; background: radial-gradient(circle at 30% 22%, rgba(255,255,255,.55), rgba(255,255,255,.14) 34%, transparent 52%), linear-gradient(150deg, #60a5fa 0%, #2563eb 55%, #1e40af 100%); box-shadow: inset 0 2px 4px rgba(255,255,255,.4), inset 0 -7px 14px rgba(15,23,42,.35), 0 10px 20px rgba(2,6,23,.32); }.default-avatar img { display: block; width: 80%; height: 80%; object-fit: contain; filter: drop-shadow(0 1px 0 rgba(255,255,255,.3)) drop-shadow(0 3px 5px rgba(15,23,42,.55)); }.eyebrow, .kicker { color: #93c5fd; font-size: .72rem; font-weight: 800; letter-spacing: .1em; text-transform: uppercase; }.public-hero h1 { margin-top: 7px; font-size: 2.5rem; }.public-hero p { margin-top: 8px; color: rgba(255,255,255,.75); }.trust-score { display: grid; gap: 4px; min-width: 110px; text-align: center; }.trust-score strong { font-size: 2.4rem; }.trust-score span { color: rgba(255,255,255,.7); font-size: .78rem; }.summary-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1px; overflow: hidden; border-radius: 18px; background: #e4e7ec; }.summary-row div { display: grid; gap: 5px; padding: 18px; background: rgba(255,255,255,.8); text-align: center; }.summary-row strong { color: #0f172a; font-size: 1.5rem; }.summary-row span, .date { color: #667085; font-size: .8rem; }.panel { padding: 26px; border-radius: 22px; background: rgba(255,255,255,.8); }.section-heading h2 { margin-top: 5px; color: #0f172a; }.review-list { margin-top: 18px; }.review { padding: 18px 0; border-top: 1px solid #eef2f6; }.review-top { display: flex; justify-content: space-between; gap: 12px; }.review p { margin: 9px 0 7px; color: #475467; line-height: 1.55; }.review .date { display: block; }
@media (max-width: 600px) { .public-hero { align-items: flex-start; flex-direction: column; padding: 24px; }.public-hero h1 { font-size: 2rem; }.trust-score { text-align: left; }.summary-row div { padding: 14px 8px; }.panel { padding: 20px; }.review-top { align-items: flex-start; flex-direction: column; } }

/* ---- Dark theme surfaces ---- */
:global(html.dark) .summary-row { background: var(--border-color); }
:global(html.dark) .summary-row div { background: var(--surface); }
:global(html.dark) .summary-row strong { color: var(--text-strong); }
:global(html.dark) .panel { background: var(--surface); }
:global(html.dark) .section-heading h2 { color: var(--text-strong); }
:global(html.dark) .review { border-top-color: var(--divider); }
:global(html.dark) .review p { color: var(--text-body); }
</style>
