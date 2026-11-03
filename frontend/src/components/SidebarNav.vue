<template>
  <aside class="sidebar">
    <div class="brand">
      <div class="brand-text">
        <span class="brand-name">LEVIATHAN</span>
        <span class="brand-tag">Buy. Sell. Bid. Tickets.</span>
      </div>
    </div>

    <button class="post-ad-btn" @click="$router.push('/post-ticket')">
      <el-icon><Plus /></el-icon> Post an Ad
    </button>

    <nav class="nav-group">
      <router-link to="/" class="nav-item active">
        <el-icon><HomeFilled /></el-icon> Home
      </router-link>
      <router-link to="/search" class="nav-item">
        <el-icon><Tickets /></el-icon> Browse Events
      </router-link>
      <a class="nav-item"><el-icon><TrendCharts /></el-icon> Trending</a>
      <a class="nav-item"><el-icon><LocationInformation /></el-icon> Nearby</a>
      <a class="nav-item"><el-icon><Star /></el-icon> Favorites</a>
      <router-link to="/my-listings" class="nav-item">
        <el-icon><List /></el-icon> My Tickets
      </router-link>
      <a class="nav-item"><el-icon><ChatDotRound /></el-icon> Messages</a>
      <a class="nav-item"><el-icon><StarFilled /></el-icon> Watchlist</a>
    </nav>

    <div class="nav-label">CATEGORIES</div>
    <nav class="nav-group">
      <a class="nav-item">🎵 Concerts</a>
      <a class="nav-item">⚽ Sports</a>
      <a class="nav-item">🎭 Theater</a>
      <a class="nav-item">✨ Festivals</a>
      <a class="nav-item">😄 Comedy</a>
      <a class="nav-item">👨‍👩‍👧 Family</a>
      <a class="nav-item more">
        <el-icon><MoreFilled /></el-icon> More <el-icon class="chevron"><ArrowRight /></el-icon>
      </a>
    </nav>

    <div class="nav-label">HELP & INFO</div>
    <nav class="nav-group small">
      <a class="nav-item">How it Works</a>
      <a class="nav-item">Safety Tips</a>
      <a class="nav-item">Fees & Policies</a>
      <a class="nav-item">Contact Support</a>
    </nav>

    <div class="sell-promo">
      <p class="sell-title">Want to sell tickets?</p>
      <p class="sell-sub">List your tickets in minutes and reach thousands of buyers.</p>
      <button class="sell-btn" @click="$router.push('/post-ticket')">Sell Tickets Now</button>
    </div>

    <div class="safety-box">
      <p class="safety-title">The safest way to buy & sell</p>
      <p class="safety-item"><el-icon><CircleCheck /></el-icon> Secure Payments</p>
      <p class="safety-item"><el-icon><CircleCheck /></el-icon> Buyer Protection</p>
      <p class="safety-item"><el-icon><CircleCheck /></el-icon> Verified Listings</p>
    </div>
  </aside>
</template>

<script setup>
import {
  HomeFilled, Tickets, TrendCharts, LocationInformation, Star, StarFilled,
  List, ChatDotRound, MoreFilled, ArrowRight, CircleCheck, Plus
} from '@element-plus/icons-vue'
</script>

<style scoped>
.sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #ECEDF3;
  padding: 24px 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  height: 100vh;
  overflow-y: auto;
  position: sticky;
  top: 0;
}
.brand { padding: 0 8px 20px; }
.brand-name { display: block; font-size: 18px; font-weight: 800; color: #5B4FE0; letter-spacing: 0.5px; }
.brand-tag { display: block; font-size: 11px; color: #9296A6; margin-top: 2px; }

.post-ad-btn {
  background: #5B4FE0;
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 12px 16px;
  font-weight: 600;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  margin-bottom: 16px;
  transition: background 0.15s;
}
.post-ad-btn:hover { background: #4B3FD0; }

.nav-group { display: flex; flex-direction: column; gap: 2px; margin-bottom: 16px; }
.nav-group.small .nav-item { font-size: 13px; color: #6B7080; padding: 6px 8px; }

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 10px;
  border-radius: 8px;
  font-size: 14px;
  color: #4A4E5C;
  text-decoration: none;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}
.nav-item:hover { background: #F5F6FA; }
.nav-item.active { background: #EEEBFF; color: #5B4FE0; font-weight: 600; }
.nav-item.more { justify-content: space-between; }
.chevron { font-size: 12px; }

.nav-label {
  font-size: 11px;
  font-weight: 700;
  color: #A5A9B8;
  letter-spacing: 0.5px;
  padding: 8px 10px 4px;
}

.sell-promo {
  background: #F5F6FA;
  border-radius: 12px;
  padding: 16px;
  margin: 12px 0;
}
.sell-title { font-weight: 700; font-size: 14px; margin: 0 0 4px; }
.sell-sub { font-size: 12px; color: #6B7080; margin: 0 0 12px; line-height: 1.4; }
.sell-btn {
  background: #fff;
  border: 1px solid #DDDFEA;
  border-radius: 8px;
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  width: 100%;
}

.safety-box { padding: 12px; }
.safety-title { font-size: 12px; font-weight: 700; color: #4A4E5C; margin: 0 0 8px; }
.safety-item { font-size: 12px; color: #6B7080; display: flex; align-items: center; gap: 6px; margin: 0 0 6px; }
.safety-item .el-icon { color: #22B573; }
</style>