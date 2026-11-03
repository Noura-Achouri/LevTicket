import { ref } from 'vue'
import en from './en'
import fr from './fr'
import zh from './zh'

const STORAGE_KEY = 'levticket-locale'

// Locales offered in the language switcher.
export const SUPPORTED_LOCALES = [
  { code: 'en', label: 'English', short: 'EN' },
  { code: 'fr', label: 'Français', short: 'FR' },
  { code: 'zh', label: '中文', short: '中文' }
]

const messages = { en, fr, zh }

function detectInitialLocale() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved && messages[saved]) return saved
  } catch {
    /* localStorage unavailable — fall through to navigator */
  }
  const nav = (typeof navigator !== 'undefined' ? navigator.language : 'en') || 'en'
  const code = nav.toLowerCase()
  if (code.startsWith('fr')) return 'fr'
  if (code.startsWith('zh')) return 'zh'
  return 'en'
}

export const locale = ref(detectInitialLocale())

function interpolate(text, params) {
  if (!params) return text
  return text.replace(/\{(\w+)\}/g, (match, key) =>
    params[key] != null ? String(params[key]) : match
  )
}

/**
 * Translate a key. English source text is used as the key, so a missing
 * translation simply falls back to the original English string.
 * Supports `{placeholder}` interpolation, e.g. t('Hi {name}', { name: 'Sam' }).
 */
export function t(key, params) {
  if (key == null) return ''
  const table = messages[locale.value] || {}
  const value =
    (table[key] != null && table[key] !== '') ? table[key]
      : (en[key] != null && en[key] !== '') ? en[key]
        : key
  return interpolate(value, params)
}

export function setLocale(code) {
  if (!messages[code]) return
  locale.value = code
  try {
    localStorage.setItem(STORAGE_KEY, code)
  } catch {
    /* ignore persistence failures */
  }
  if (typeof document !== 'undefined') {
    document.documentElement.setAttribute('lang', code)
  }
}

export function applyInitialLanguage() {
  if (typeof document !== 'undefined') {
    document.documentElement.setAttribute('lang', locale.value)
  }
}

export function useI18n() {
  return { t, locale, setLocale, locales: SUPPORTED_LOCALES }
}
