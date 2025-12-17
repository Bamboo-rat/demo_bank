import { createAuthenticatedAxios } from '~/config/authenticatedAxios'

const axiosNotification = createAuthenticatedAxios({
  baseURL: import.meta.env.VITE_API_BASE_NOTIFICATION_URL || 'http://localhost:8085/api',
  publicEndpoints: []
})

export default axiosNotification
