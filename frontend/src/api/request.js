import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'https://glasses-erik-kai-rich.trycloudflare.com/api',
  timeout: 65000
})

request.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

// Unwrap the { code, message, data } envelope automatically
request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const status = error.response?.status
    const body = error.response?.data

    // Token expired / invalid -> clear session and send to login.
    if (status === 401) {
      const auth = useAuthStore()
      auth.logout()
      ElMessage.warning('Your session has expired. Please log in again.')
      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
      }
    }

    return Promise.reject(body || error)
  }
)

export default request
