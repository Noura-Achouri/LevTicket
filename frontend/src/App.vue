<template>
  <div class="app-shell">
    <div class="site-header">
    <header class="topbar">
      <div class="nav-brand">
        <router-link to="/" class="brand">
          <span class="brand-icon"><img src="/ai-dragon.png" alt="LevTicket" /></span>
          <span>LevTicket</span>
        </router-link>
      </div>

      <nav class="main-nav" :aria-label="$t('Main navigation')">
        <router-link v-if="!auth.token" to="/" class="nav-link">{{ $t('Home') }}</router-link>
        <router-link to="/search" class="nav-link">{{ $t('Browse') }}</router-link>
        <template v-if="auth.token">
          <router-link to="/post-ticket" class="nav-link">{{ $t('Sell') }}</router-link>
          <router-link to="/my-listings" class="nav-link">{{ $t('Listings') }}</router-link>
          <router-link to="/orders" class="nav-link">{{ $t('Orders') }}</router-link>
          <router-link to="/saved" class="nav-link saved-link">
            {{ $t('Saved') }}
            <b v-if="watchlist.count" class="nav-count">{{ watchlist.count }}</b>
          </router-link>
          <router-link to="/messages" class="nav-link saved-link">
            {{ $t('Messages') }}
            <b v-if="chat.unreadCount" class="nav-count">{{ chat.unreadCount }}</b>
          </router-link>
        </template>
      </nav>

      <div class="nav-actions">
        <button
          type="button"
          class="control-button"
          :title="theme === 'dark' ? $t('Toggle light mode') : $t('Toggle dark mode')"
          :aria-label="theme === 'dark' ? $t('Toggle light mode') : $t('Toggle dark mode')"
          @click="toggleTheme"
        >
          <el-icon aria-hidden="true"><Sunny v-if="theme === 'dark'" /><Moon v-else /></el-icon>
        </button>

        <el-dropdown trigger="click" @command="onSelectLocale">
          <button type="button" class="control-button lang-button" :title="$t('Language')" :aria-label="$t('Language')">
            <el-icon aria-hidden="true"><svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9" /><path d="M3 12h18M12 3c2.6 2.5 2.6 15.5 0 18M12 3c-2.6 2.5-2.6 15.5 0 18" /></svg></el-icon>
            <span class="lang-current">{{ currentLocale.short }}</span>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="l in locales"
                :key="l.code"
                :command="l.code"
                :disabled="l.code === locale"
              >
                {{ l.label }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <template v-if="auth.token">
          <el-popover placement="bottom-end" :width="320" trigger="click">
            <template #reference>
              <button type="button" class="notification-button" :title="$t('Notifications')">
                <el-icon aria-hidden="true"><Bell /></el-icon><b v-if="unreadNotifications">{{ unreadNotifications }}</b>
              </button>
            </template>
            <div class="notification-heading"><strong>{{ $t('Notifications') }}</strong><button type="button" @click="markAllRead">{{ $t('Mark all read') }}</button></div>
            <div v-if="notifications.length" class="notification-list">
              <button v-for="notification in notifications" :key="notification.id" type="button" :class="['notification-item', { unread: !notification.read }]" @click="readNotification(notification)">
                <strong>{{ notification.title }}</strong><span>{{ notification.message }}</span>
              </button>
            </div>
            <el-empty v-else :description="$t('No notifications')" />
          </el-popover>
          <router-link to="/profile" class="nav-button secondary">{{ $t('Profile') }}</router-link>
          <button type="button" class="nav-button primary" @click="logout">{{ $t('Logout') }}</button>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-button secondary">{{ $t('Login') }}</router-link>
          <router-link to="/register" class="nav-button primary">{{ $t('Sign up') }}</router-link>
        </template>
      </div>
    </header>
    </div>

    <main class="page-container">
      <router-view />
    </main>

    <button
      v-if="!chatOpen"
      type="button"
      class="ai-fab"
      :title="$t('Chat with LevTicket AI')"
      @click="chatRef?.openChat()"
    >
      <img src="/ai-dragon.png" class="ai-fab-icon" alt="LevTicket AI" />
      <span class="ai-fab-label">AI</span>
    </button>

    <CustomerServiceChat ref="chatRef" @open-change="chatOpen = $event" />

    <!-- Floating buyer <-> seller chat, opened from "Message seller" anywhere -->
    <ChatWindow />
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import CustomerServiceChat from '@/components/CustomerServiceChat.vue'
import ChatWindow from '@/components/ChatWindow.vue'
import { Bell, ChatLineRound, Moon, Sunny } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useWatchlistStore } from '@/stores/watchlist'
import { useChatStore } from '@/stores/chat'
import { getNotifications, markAllNotificationsRead, markNotificationRead } from '@/api/notifications'
import { useTheme } from '@/composables/theme'
import { useI18n } from '@/i18n'

const router = useRouter()
const auth = useAuthStore()
const watchlist = useWatchlistStore()
const chat = useChatStore()
const { theme, toggleTheme } = useTheme()
const { locale, setLocale, locales } = useI18n()
const currentLocale = computed(() => locales.find((l) => l.code === locale.value) || locales[0])

function onSelectLocale(code) {
  setLocale(code)
}

const chatRef = ref(null)
const chatOpen = ref(false)
const notifications = ref([])
const unreadNotifications = ref(0)

async function loadNotifications() {
  if (!auth.token) return
  try {
    const response = await getNotifications()
    notifications.value = response.data?.notifications || []
    unreadNotifications.value = response.data?.unreadCount || 0
  } catch {
    notifications.value = []
  }
}

async function readNotification(notification) {
  if (!notification.read) await markNotificationRead(notification.id)
  notification.read = true
  unreadNotifications.value = Math.max(0, unreadNotifications.value - 1)

  // A chat ping carries the sender id, so jump straight into that thread.
  if (notification.type === 'message' && notification.relatedId) {
    router.push({ path: '/messages', query: { with: notification.relatedId } })
  }
}

async function markAllRead() {
  await markAllNotificationsRead()
  notifications.value.forEach((notification) => { notification.read = true })
  unreadNotifications.value = 0
}

function logout() {
  auth.logout()
  notifications.value = []
  unreadNotifications.value = 0
  watchlist.reset()
  chat.reset()
}

watch(() => auth.token, (token) => {
  if (token) {
    loadNotifications()
    watchlist.load()
    chat.loadUnread()
    chat.startPolling()
  } else {
    notifications.value = []
    unreadNotifications.value = 0
    watchlist.reset()
    chat.reset()
  }
}, { immediate: true })
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: var(--app-bg);
  transition: background 0.3s ease;
}

.site-header {
  position: sticky;
  top: 0;
  z-index: 20;
  /* Frosted glass container with a soft blue 3D depth */
  background:
    radial-gradient(130% 200% at 50% -60%, rgba(59, 130, 246, 0.14), transparent 55%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.97) 0%, rgba(240, 246, 255, 0.94) 100%);
  backdrop-filter: blur(18px) saturate(1.35);
  -webkit-backdrop-filter: blur(18px) saturate(1.35);
  border-bottom: 1px solid rgba(148, 163, 184, 0.22);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.95),
    inset 0 -14px 28px rgba(37, 99, 235, 0.06),
    0 1px 2px rgba(15, 23, 42, 0.05),
    0 12px 32px rgba(30, 64, 175, 0.1);
  transition: box-shadow 0.25s ease, background 0.25s ease;
}

/* Blue shimmer hairline along the bottom edge */
.site-header::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1px;
  height: 1px;
  background: linear-gradient(90deg, transparent 4%, rgba(37, 99, 235, 0.28) 20%, rgba(96, 165, 250, 0.65) 50%, rgba(37, 99, 235, 0.28) 80%, transparent 96%);
  pointer-events: none;
}

/* Glow deepens while interacting anywhere on the bar */
.site-header:hover {
  background:
    radial-gradient(130% 200% at 50% -60%, rgba(59, 130, 246, 0.18), transparent 55%),
    linear-gradient(180deg, #ffffff 0%, rgba(239, 246, 255, 0.97) 100%);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 1),
    inset 0 -14px 28px rgba(37, 99, 235, 0.08),
    0 1px 2px rgba(15, 23, 42, 0.05),
    0 16px 40px rgba(30, 64, 175, 0.16);
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  max-width: 1280px;
  margin: 0 auto;
  padding: 12px 28px;
}

.nav-brand {
  flex-shrink: 0;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 11px;
  padding: 7px 16px 7px 10px;
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 999px;
  background: #ffffff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.1);
  color: #101828;
  font-size: 1.4rem;
  font-weight: 800;
  letter-spacing: -0.04em;
  text-decoration: none;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}
.brand:hover { transform: translateY(-1px); box-shadow: 0 12px 28px rgba(15, 23, 42, 0.16); }

.brand-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  width: 42px;
  height: 42px;
  /* Circular blue 3D orb behind the dragon */
  border-radius: 50%;
  background:
    radial-gradient(circle at 30% 22%, rgba(255, 255, 255, 0.65), rgba(255, 255, 255, 0.16) 34%, transparent 52%),
    radial-gradient(circle at 70% 100%, rgba(59, 130, 246, 0.5), transparent 55%),
    linear-gradient(150deg, #60a5fa 0%, #2563eb 55%, #1e40af 100%);
  border: 1px solid rgba(255, 255, 255, 0.45);
  box-shadow:
    inset 0 2px 4px rgba(255, 255, 255, 0.4),
    inset 0 -9px 16px rgba(15, 23, 42, 0.35),
    0 10px 22px rgba(37, 99, 235, 0.45),
    0 3px 6px rgba(15, 23, 42, 0.2);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.brand:hover .brand-icon,
.brand:hover .brand-icon img {
  transform: translateY(-1px) scale(1.05);
}
.brand:hover .brand-icon {
  box-shadow:
    inset 0 2px 4px rgba(255, 255, 255, 0.48),
    inset 0 -9px 16px rgba(15, 23, 42, 0.3),
    0 14px 30px rgba(37, 99, 235, 0.58),
    0 4px 8px rgba(15, 23, 42, 0.22);
}
/* Dragon gets a floating 3D depth: offset shadow + soft white glow behind it */
.brand-icon img {
  display: block;
  width: 92%;
  height: 92%;
  object-fit: contain;
  filter:
    drop-shadow(0 1px 0 rgba(255, 255, 255, 0.35))
    drop-shadow(0 3px 5px rgba(15, 23, 42, 0.55))
    drop-shadow(0 6px 12px rgba(30, 64, 175, 0.4));
  transition: transform 0.2s ease;
}

/* 3D embossed gradient name */
.brand > span:last-child {
  display: inline-block;
  background: linear-gradient(180deg, #ffffff 0%, #93c5fd 30%, #2563eb 70%, #1e40af 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: #2563eb;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.35);
  filter:
    drop-shadow(0 1px 0 rgba(255, 255, 255, 0.3))
    drop-shadow(0 3px 6px rgba(30, 64, 175, 0.35));
}

.main-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 5px;
  border: 1px solid rgba(148, 163, 184, 0.35);
  background: #ffffff;
  border-radius: 999px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.1);
  transition: box-shadow 0.25s ease, transform 0.25s ease;
}
.main-nav:hover {
  transform: translateY(-1px);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 12px 30px rgba(30, 64, 175, 0.18);
}

.nav-link {
  padding: 9px 16px;
  border-radius: 999px;
  color: #475467;
  font-size: 0.95rem;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s ease, background 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}

.nav-link:hover {
  color: #111827;
  background: linear-gradient(180deg, #f8fafc, #eef2f7);
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.08);
}

.nav-link:active {
  transform: translateY(0);
}

.nav-link.router-link-active {
  color: #fff;
  background: linear-gradient(90deg, #2563eb, #3b82f6);
  box-shadow: 0 6px 14px rgba(37, 99, 235, 0.28);
}

/* saved-tickets link with a live count badge */
.saved-link {
  display: inline-flex;
  align-items: center;
  gap: 7px;
}
.nav-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 19px;
  height: 19px;
  padding: 0 5px;
  border-radius: 999px;
  background: linear-gradient(135deg, #f43f5e, #e11d48);
  color: #fff;
  font-size: 0.68rem;
  font-weight: 800;
  line-height: 1;
  box-shadow: 0 2px 6px rgba(225, 29, 72, 0.4);
}
.nav-link.router-link-active .nav-count {
  background: rgba(255, 255, 255, 0.95);
  color: #1d4ed8;
  box-shadow: none;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.control-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-width: 42px;
  height: 42px;
  padding: 0 12px;
  border: 1px solid rgba(148, 163, 184, 0.4);
  border-radius: 999px;
  background: linear-gradient(180deg, #ffffff, #f1f5f9);
  color: #475467;
  font-size: 0.85rem;
  font-weight: 700;
  cursor: pointer;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.9), 0 2px 6px rgba(15, 23, 42, 0.08);
  transition: color 0.2s ease, background 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}
.control-button:hover {
  color: #1d4ed8;
  background: linear-gradient(180deg, #ffffff, #eff6ff);
  transform: translateY(-2px);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 1),
    0 10px 20px rgba(37, 99, 235, 0.25);
}
.control-button:active { transform: translateY(0); }
.control-button .el-icon { font-size: 18px; }
.lang-button .lang-current { line-height: 1; }
html.dark .control-button {
  background: linear-gradient(180deg, var(--surface-2), var(--surface-muted));
  color: var(--text-body);
  border-color: var(--border-color);
}
html.dark .control-button:hover {
  color: #93c5fd;
  background: linear-gradient(180deg, var(--surface-2), rgba(37, 99, 235, 0.22));
}

.notification-button {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border: 1px solid rgba(148, 163, 184, 0.4);
  border-radius: 50%;
  background: linear-gradient(180deg, #ffffff, #f1f5f9);
  color: #475467;
  cursor: pointer;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.9), 0 2px 6px rgba(15, 23, 42, 0.08);
  transition: color 0.2s ease, background 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}
.notification-button:hover {
  color: #1d4ed8;
  background: linear-gradient(180deg, #ffffff, #eff6ff);
  transform: translateY(-2px);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 1),
    0 10px 20px rgba(37, 99, 235, 0.25);
}
.notification-button:active { transform: translateY(0); box-shadow: inset 0 1px 0 rgba(255,255,255,0.7), 0 2px 6px rgba(15,23,42,0.08); }
.notification-button b { position: absolute; top: -6px; right: -6px; min-width: 18px; padding: 2px 5px; border-radius: 999px; background: #ef4444; color: #fff; font-size: .68rem; }
.notification-heading { display: flex; justify-content: space-between; align-items: center; gap: 10px; }
.notification-heading button { border: 0; background: transparent; color: #2563eb; cursor: pointer; font-size: .78rem; }
.notification-list { display: grid; gap: 6px; margin-top: 12px; max-height: 280px; overflow-y: auto; }
.notification-item { display: grid; gap: 4px; padding: 10px; border: 0; border-radius: 8px; background: transparent; text-align: left; cursor: pointer; }
.notification-item.unread { background: #eff6ff; }.notification-item span { color: #667085; font-size: .8rem; }

.nav-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  text-decoration: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.nav-button {
  min-width: 96px;
  padding: 10px 18px;
  font-size: 0.92rem;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
}

.nav-button.primary {
  background:
    radial-gradient(120% 160% at 30% 0%, rgba(255, 255, 255, 0.25), transparent 55%),
    linear-gradient(135deg, #1d4ed8 0%, #2563eb 55%, #3b82f6 100%);
  color: #fff;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.3),
    inset 0 -8px 16px rgba(15, 23, 42, 0.25),
    0 12px 24px rgba(37, 99, 235, 0.28);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.nav-button.primary:hover {
  transform: translateY(-2px);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.38),
    inset 0 -8px 16px rgba(15, 23, 42, 0.2),
    0 18px 34px rgba(37, 99, 235, 0.42);
}
.nav-button.primary:active {
  transform: translateY(0);
  box-shadow:
    inset 0 2px 6px rgba(15, 23, 42, 0.3),
    0 6px 14px rgba(37, 99, 235, 0.25);
}

.nav-button.secondary {
  border: 1px solid rgba(148, 163, 184, 0.4);
  background: linear-gradient(180deg, #ffffff, #f1f5f9);
  color: #1f2937;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.9), 0 2px 6px rgba(15, 23, 42, 0.06);
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}
.nav-button.secondary:hover {
  background: linear-gradient(180deg, #ffffff, #eff6ff);
  color: #1d4ed8;
  transform: translateY(-2px);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 1),
    0 12px 24px rgba(37, 99, 235, 0.18);
}
.nav-button.secondary:active {
  transform: translateY(0);
  box-shadow: inset 0 2px 6px rgba(15, 23, 42, 0.08), 0 2px 6px rgba(15, 23, 42, 0.06);
}

.nav-button:hover {
  transform: translateY(-1px);
}

.page-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px 28px 40px;
}

@media (max-width: 980px) {
  .topbar {
    flex-wrap: wrap;
    justify-content: center;
  }

  .main-nav {
    width: 100%;
    flex-wrap: wrap;
  }

  .nav-actions {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 640px) {
  .topbar {
    padding: 16px 18px;
  }

  .page-container {
    padding: 18px 18px 30px;
  }

  .brand span:last-child {
    display: none;
  }

  .nav-button {
    min-width: 0;
    padding: 9px 14px;
  }
}

.ai-fab {
    position: fixed;
    right: 24px;
    bottom: 24px;
    z-index: 3000;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 1px;
    width: 64px;
    height: 64px;
    padding: 8px 6px;
    border: 1px solid rgba(255, 255, 255, 0.5);
    border-radius: 50%;
    cursor: pointer;
    color: #fff;
    /* Layered gradients fake a glossy 3D sphere: top sheen -> brand blue -> deep edge */
    background:
      radial-gradient(circle at 30% 22%, rgba(255, 255, 255, 0.6), rgba(255, 255, 255, 0.14) 30%, transparent 48%),
      radial-gradient(circle at 72% 100%, rgba(59, 130, 246, 0.55), transparent 55%),
      radial-gradient(circle at 50% 50%, #3b82f6 0%, #2563eb 55%, #1e40af 100%);
    box-shadow:
      inset 0 2px 4px rgba(255, 255, 255, 0.35),
      inset 0 -12px 18px rgba(15, 23, 42, 0.38),
      0 16px 32px rgba(37, 99, 235, 0.5),
      0 4px 8px rgba(15, 23, 42, 0.22);
    outline: none;
    transition: transform 0.2s ease, box-shadow 0.25s ease;
  }

  .ai-fab-icon {
    width: 28px;
    height: 28px;
    object-fit: contain;
    filter: drop-shadow(0 2px 5px rgba(15, 23, 42, 0.55));
  }

  .ai-fab-label {
    font-size: 10px;
    font-weight: 800;
    letter-spacing: 0.06em;
    line-height: 1;
    color: #fff;
    opacity: 0.98;
    text-shadow: 0 1px 3px rgba(30, 64, 175, 0.9);
  }

  .ai-fab:hover {
    transform: translateY(-3px) scale(1.04);
    box-shadow:
      inset 0 2px 4px rgba(255, 255, 255, 0.42),
      inset 0 -12px 18px rgba(15, 23, 42, 0.32),
      0 22px 44px rgba(37, 99, 235, 0.55),
      0 6px 12px rgba(15, 23, 42, 0.24);
  }

  .ai-fab:active {
    transform: translateY(-1px) scale(0.97);
  }

  .ai-fab:focus-visible {
    box-shadow:
      inset 0 2px 4px rgba(255, 255, 255, 0.35),
      inset 0 -12px 18px rgba(15, 23, 42, 0.38),
      0 0 0 4px rgba(37, 99, 235, 0.35),
      0 16px 32px rgba(37, 99, 235, 0.5);
  }


</style>