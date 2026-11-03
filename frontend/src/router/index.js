import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue') },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue') },
  { path: '/auction/:id', name: 'AuctionDetail', component: () => import('@/views/AuctionDetail.vue') },
  { path: '/profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { requiresAuth: true } },
  { path: '/profile/:id', name: 'PublicProfile', component: () => import('@/views/PublicProfile.vue') },
  { path: '/post-ticket', name: 'PostTicket', component: () => import('@/views/PostTicket.vue'), meta: { requiresAuth: true } },
  { path: '/search', name: 'SearchResults', component: () => import('@/views/SearchResults.vue') },
  { path: '/my-listings', name: 'MyListings', component: () => import('@/views/MyListings.vue'), meta: { requiresAuth: true } },
  { path: '/orders', name: 'OrderHistory', component: () => import('@/views/OrderHistory.vue'), meta: { requiresAuth: true } },
  { path: '/saved', name: 'SavedTickets', component: () => import('@/views/SavedTickets.vue'), meta: { requiresAuth: true } },
  { path: '/messages', name: 'Messages', component: () => import('@/views/Messages.vue'), meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !localStorage.getItem('token')) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }
})

export default router
