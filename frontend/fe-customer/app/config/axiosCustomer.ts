import { createAuthenticatedAxios } from './authenticatedAxios'

const axiosInstance = createAuthenticatedAxios({
  baseURL: import.meta.env.VITE_API_BASE_CUSTOMERSERVICE_URL || 'http://localhost:8082/api',
  publicEndpoints: [
    '/auth/login',
    '/auth/refresh',
    '/registration/start',
    '/registration/verify',
    '/registration/profile',
    '/registration/identity',
    '/registration/complete'
  ]
})

export default axiosInstance;
