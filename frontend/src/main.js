import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import './style.css'
import App from './App.vue'
import router from './router'
import { t, applyInitialLanguage } from './i18n'
import { initTheme } from './composables/theme'

initTheme()
applyInitialLanguage()

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// Makes `$t('Some text')` available in every component template.
app.config.globalProperties.$t = t

app.mount('#app')
