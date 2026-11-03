import { ref } from 'vue'

const STORAGE_KEY = 'levticket-theme'

function detectInitialTheme() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved === 'dark' || saved === 'light') return saved
  } catch {
    /* localStorage unavailable — fall through to system preference */
  }
  if (typeof window !== 'undefined' && window.matchMedia) {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
  }
  return 'light'
}

export const theme = ref(detectInitialTheme())

function applyTheme(mode) {
  if (typeof document === 'undefined') return
  const root = document.documentElement
  // Element Plus keys its dark theme off the `dark` class on <html>.
  root.classList.toggle('dark', mode === 'dark')
  root.setAttribute('data-theme', mode)
  root.style.colorScheme = mode
}

export function setTheme(mode) {
  theme.value = mode === 'dark' ? 'dark' : 'light'
  try {
    localStorage.setItem(STORAGE_KEY, theme.value)
  } catch {
    /* ignore persistence failures */
  }
  applyTheme(theme.value)
}

export function toggleTheme() {
  setTheme(theme.value === 'dark' ? 'light' : 'dark')
}

export function initTheme() {
  applyTheme(theme.value)
}

export function useTheme() {
  return { theme, setTheme, toggleTheme }
}
